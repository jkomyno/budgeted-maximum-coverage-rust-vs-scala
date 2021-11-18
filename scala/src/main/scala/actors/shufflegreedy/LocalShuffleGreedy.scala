package actors.shufflegreedy

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

import LocalShuffleGreedy._
import datatypes.{CostSet}

object LocalShuffleGreedy {
  /** Model input messages for the local actor, i.e., commands */
  sealed trait LocalCommand

  /** Randomly shuffle the collection of sets in the local node */
  case object Shuffle extends LocalCommand

  /** Perform one iteration
   *
   * @param t the current iteration
   * @param localBudget the budget available for the current node
   * @param replyTo a reference to the actor that will receive the reply
   */
  final case class RunIteration(t: Int, localBudget: Int, replyTo: ActorRef[IterationSolution]) extends LocalCommand

  /** Model output messages for the local actor, i.e., responses */
  sealed trait LocalResponse

  /** Solution of a single iteration
   * 
   * @param localSolution the collection of sets selected after the iteration
   * @param spareBudget the budget left after the iteration
   * @param weight the weight of the solution s
   * @param cost the cost of the solution s
   */
  final case class IterationSolution(localSolution: Set[Int], spareBudget: Int, weight: Int, cost: Int) extends LocalResponse

  /**
   * Build the companion class for the actor
   * 
   * @param localSRaw the collection of sets available to the local node
   */
  def apply(localSRaw: List[(CostSet, Int)]): Behavior[LocalCommand] = Behaviors.setup { context =>
    new LocalShuffleGreedy(context, localSRaw)
  }
}

/** Local actor for the ShuffleGreedy algorithm.
 * 
 * @param context the typed actor context
 * @param localSRaw the collection of sets available to the local node
 */
class LocalShuffleGreedy(context: ActorContext[LocalCommand], localSRaw: List[(CostSet, Int)])
  extends AbstractBehavior[LocalCommand](context) {

  context.log.info("LocalShuffleGreedy started")

  /** The local partition of s available to this actor */
  var localS: ListBuffer[(CostSet, Int)] = ListBuffer.from(localSRaw.sorted)

  override def onMessage(msg: LocalCommand): Behavior[LocalCommand] = msg match {
    case Shuffle =>
      context.log.info(s"  ->LocalShuffleGreedy::Shuffle")
      localS = scala.util.Random.shuffle(localS)
      Behaviors.same

    case RunIteration(t, localBudget, replyTo) =>
      context.log.info(s"  ->LocalShuffleGreedy::RunIteration")

      val localSolution = mutable.HashSet[Int]()
      var spareBudget = localBudget
      var weight = 0
      var cost = 0

      for ((s, j) <- this.localS; if spareBudget > 0) {
        if (s.cost <= spareBudget) {
          localSolution.add(j)
          spareBudget -= s.cost
          cost += s.cost
          weight += s.weight
        }
      }
      
      /** Send a message with the current iteration's solution to the actor referenced by replyTo */
      val iterationSolution = IterationSolution(localSolution.toSet, spareBudget, weight, cost)
      replyTo ! iterationSolution

      Behaviors.same
  }
}