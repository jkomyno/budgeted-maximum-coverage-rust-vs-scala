package actors.aggregator

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import scala.concurrent.duration.DurationInt

import actors.shufflegreedy.BestResult
import actors.shufflegreedy.MasterShuffleGreedy.{FindLocalBest}
import actors.shufflegreedy.LocalShuffleGreedy.{LocalCommand, IterationSolution, RunIteration}

object LocalAggregator {
  sealed trait LocalAggregatorCommand

  // Retrieve aggregated local solutions
  final case class AggregatedLocalSolutions(localSolutions: List[IterationSolution]) extends LocalAggregatorCommand

  def apply(localNodes: List[ActorRef[LocalCommand]],
            localBudgets: List[Int], t: Int,
            bestResult: BestResult, replyTo: ActorRef[FindLocalBest]): Behavior[LocalAggregatorCommand] = {
    Behaviors.setup { context =>
      context.log.info("LocalAggregator started")

      /** Spawn a child Aggregator actor */
      context.spawnAnonymous(
        Aggregator[IterationSolution, AggregatedLocalSolutions](
          sendRequests = { aggregateTo =>
            /** Tell every local node to run an iteration */
            localNodes zip localBudgets foreach { case (node, localBudget) =>
              node ! RunIteration(localBudget, t, aggregateTo)
            }
          },
          k=localNodes.size,
          replyTo=context.self,
          aggregateResponses = responses =>
            AggregatedLocalSolutions(
              responses.toList
            ),
          timeout=5.seconds
        )
      )

      Behaviors.receiveMessage {
        case AggregatedLocalSolutions(localSolutions) =>
          context.log.info(s"  ->LocalAggregator::AggregatedLocalSolutions")
          context.log.info(s"Sending FindLocalBest to $replyTo")
          replyTo ! FindLocalBest(t, bestResult, localSolutions)

          Behaviors.stopped
      }
    }
  }
}
