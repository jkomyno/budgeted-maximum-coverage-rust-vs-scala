package actors.shufflegreedy

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import MasterShuffleGreedy.{MasterResponse, Init}

/**
 * Supervisor receives the data, spawns the MasterShuffleGreedy actor listens to its response.
 */
object Supervisor {
  sealed trait SupervisorCommand
  final case class RunShuffleGreedy(params: SupervisorParams, replyTo: ActorRef[MasterResponse]) extends SupervisorCommand

  /**
   * Behaviors.setup sets up a behavior that doesn't wait to receive any message before executing.
   * This is useful since Supervisor is the top-level actor in our system.
   */
  def apply(): Behavior[SupervisorCommand] = Behaviors.setup { context =>
    context.log.info("Supervisor started")

    Behaviors.receiveMessage[SupervisorCommand] {
      case RunShuffleGreedy(params, replyTo) =>
        context.log.info(s"Received RunShuffleGreedy(params, replyTo)")

        val masterShuffleGreedy = MasterShuffleGreedy(params, replyTo)
        val masterActor = context.spawn(masterShuffleGreedy, name="master-shuffle-greedy")

        // execute master computation
        masterActor ! Init
        Behaviors.same
    }
  }
}
