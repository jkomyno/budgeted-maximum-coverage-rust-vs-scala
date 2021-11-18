package actors.shufflegreedy

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import LocalShuffleGreedy.{IterationSolution, Shuffle}
import MasterShuffleGreedy._
import actors.aggregator.LocalAggregator
import datatypes.{CostSet, WeightedItem}
import utils._

object MasterShuffleGreedy {
  /** Model input messages for the master actor, i.e., commands */
  sealed trait MasterCommand

  /** Initialize the BMC problem */
  case object Init extends MasterCommand

  /**
   * Run the local iterations of the BMC problem
   * @param t number of iterations
   * @param localBudgetMultipliers list of weights that determines the budget allocation for the next iteration
   * @param bestResult best result so far
   */
  final case class RunLocalSteps(t: Int, localBudgetMultipliers: List[Int], bestResult: BestResult) extends MasterCommand

  /**
   * Finds the best result among the local solutions.
   * If such such a result is worse than the previous best result, we discard the local solutions
   * and keep the previous one.
   * @param t number of iterations
   * @param localBudgetMultipliers list of weights that determines the budget allocation for the next iteration
   * @param bestResult best result so far
   */
  final case class FindLocalBest(t: Int, prevBestResult: BestResult, localSolutions: List[IterationSolution]) extends MasterCommand

  /**
   * Returns the final solution the actor that called MasterShuffleGreedy
   */
  final case class RunFinalStep(bestResult: BestResult) extends MasterCommand

  /** Model output messages for the master actor, i.e., responses */
  sealed trait MasterResponse

  /** Returns the solution of the BMC problem
   * @param s the sets chosen for the solution
   * @param remainingBudget the remaining budget
   */
  final case class BMCSolution(s: List[CostSet], remainingBudget: Int) extends MasterResponse

  /**
   * Build the companion class for the actor
   * @param params the parameters to send to the actor
   * @param replyTo a reference to the actor that will receive the reply
   */
  def apply(params: SupervisorParams, replyTo: ActorRef[MasterResponse]): Behavior[MasterCommand] = Behaviors.setup { context =>
    new MasterShuffleGreedy(context, params, replyTo)
  }
}

/**
 * Master actor for the ShuffleGreedy algorithm.
 * An actor instance processes one message at a time, so no concurrency guards are needed.
 * @param context the typed actor context
 * @param params the parameters used by the actor 
 * @param replyTo a reference to the actor that will receive the reply
 */
class MasterShuffleGreedy(context: ActorContext[MasterCommand], params: SupervisorParams, replyTo: ActorRef[MasterResponse])
  extends AbstractBehavior[MasterCommand](context) {
  
  context.log.info("MasterShuffleGreedy started")

  private val actorProps = DispatcherSelector.fromConfig("akka.actor.pinned-dispatcher")
  private lazy val sIndex: List[(CostSet, Int)] = params.s.zipWithIndex
  private lazy val indexToCostSet: Map[Int, CostSet] = (sIndex map { case(cs, i) => (i, cs) }).toMap
  private lazy val sPartitions: IndexedSeq[List[(CostSet, Int)]] = utils.partition(sIndex, params.k)

  private lazy val localNodes = sPartitions.zipWithIndex map {
    case (localS, i) =>
      val node = LocalShuffleGreedy(localS)
      val name = s"local-shuffle-greedy-$i"
      context.spawn(node, name, actorProps)
  }

  private def closeLocalNodes() = {
    println(s"Closing ${params.k} local nodes")
    localNodes foreach { node =>
      context.stop(node)
    }
  }

  override def onMessage(msg: MasterCommand): Behavior[MasterCommand] = msg match {
    case Init =>
      context.log.info("  ->MasterShuffleGreedy::Init")
      
      // initially, each node has the same probability of being the best, so the budget is
      // fairly split between the nodes. Each budget multiplier is thus 1
      val localBudgetMultipliers: List[Int] = (for (_ <- 0 until params.k) yield 1).toList
      
      // this is the first iteration
      val t = 0

      val bestResult = BestResult(
        solution=List(),
        weight=0,
        cost=params.budget + 1,
        t=t
      )

      // Tell this actor to move to execute the RunLocalSteps handler
      context.self ! RunLocalSteps(t, localBudgetMultipliers, bestResult)
      Behaviors.same
    
    case RunLocalSteps(t, localBudgetMultipliers, prevBestResult) =>
      context.log.info("  ->MasterShuffleGreedy::RunLocalSteps")
      val localBudgets = utils.splitBudgets(params.budget, localBudgetMultipliers)
      context.log.info(s"  localBudgets: $localBudgets")

      /** Spawn a child LocalAggregator actor */
      context.spawnAnonymous(
        LocalAggregator(this.localNodes.toList, localBudgets, t, prevBestResult,
                        replyTo=context.self)
      )

      Behaviors.same
  
    // FindLocalBest is called by LocalAggregator
    case FindLocalBest(t, prevBestResult, localSolutions) =>
      context.log.info(s"  ->MasterShuffleGreedy::FindLocalBest (t=${t})")

      val localG = for (sol <- localSolutions) yield sol.localSolution.toList
      val localSpareBudget = for (sol <- localSolutions) yield sol.spareBudget
      val localCost = params.budget - localSpareBudget.sum

      val items: List[List[WeightedItem]] = for (
        sol <- localSolutions;
        j   <- sol.localSolution
      ) yield indexToCostSet(j).s

      val localWeight = items.flatten.map(x => x.weight).sum

      val bestResult = if (localWeight < prevBestResult.weight) prevBestResult else
        BestResult(solution=localG, weight=localWeight, cost=localCost, t=t)

      if (bestResult != prevBestResult) {
        context.log.info(s"New best result at t=$t")
      }

      if (params.t == t + 1) {
        /** We've hit the final iteration, so we move to RunFinalStep */
        context.self ! RunFinalStep(bestResult)
      } else {
        /**
         * We still have some iterations to go, so we:
         * - redistribute the budgets according to the obtained local objectives
         * - tell the local nodes to shuffle their local part of the collection of sets
         * - continue with the next iteration of RunLocalSteps
         */

        // TODO: don't use sol.weight, but sol.localObjective (count each weight only once)
        val localBudgetMultipliers = for (sol <- localSolutions) yield math.max(sol.weight, 1)

        localNodes foreach { node =>
          node ! Shuffle
          println(s"Shuffling local node $node")
        }

        context.self ! RunLocalSteps(t + 1, localBudgetMultipliers, bestResult)
      }

      Behaviors.same

    case RunFinalStep(bestResult) =>
      context.log.info(s"  ->MasterShuffleGreedy::RunFinalStep")

      val chosenSets: List[CostSet] = for (
        s <- bestResult.solution;
        j <- s
      ) yield indexToCostSet(j)

      val remainingBudget = params.budget - bestResult.cost

      // inform the caller that we have a solution
      replyTo ! BMCSolution(chosenSets, remainingBudget)

      // clean the LocalShuffleGreedy nodes
      this.closeLocalNodes()

      // stop MasterShuffleGreedy actor
      Behaviors.stopped
  }
}
