package actors.aggregator

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

/** 
 * A response aggregator is useful when subscribing to an actor that
 * will send a many responses back. 
 */
object Aggregator {
  sealed trait AggregatorCommand

  private case object ReceiveTimeout extends AggregatorCommand
  private case class WrappedResponse[R](response: R) extends AggregatorCommand

  def apply[Response: ClassTag, Aggregate](
    sendRequests: ActorRef[Response] => Unit,
    k: Int,
    replyTo: ActorRef[Aggregate],
    aggregateResponses: immutable.IndexedSeq[Response] => Aggregate,
    timeout: FiniteDuration
  ): Behavior[AggregatorCommand] = {

    Behaviors.setup { context =>
      context.log.info("Aggregator started")

      /** When timeout is elapsed, send a ReceiveTimeout message to itself */
      context.setReceiveTimeout(timeout, ReceiveTimeout)

      /**
       * Tell the caller that we are ready to start aggregating responses.
       * Each response will be wrapped in a WrappedResponse message that
       * will be sent to this actor.
       */
      val responseAdapter = context.messageAdapter[Response](WrappedResponse(_))
      sendRequests(responseAdapter)

      /**
       * Defines the logic of how to handle the AggregatorCommands receive.
       * collecting will be called recursively until k responses are collected (or the given timeout is elapsed).
       * Then, the aggregated responses are returned.
       */
      def collecting(responses: immutable.IndexedSeq[Response]): Behavior[AggregatorCommand] = Behaviors.receiveMessage {
        case WrappedResponse(response) =>
          context.log.info("  ->Aggregator::WrappedResponse")

          /**
           * We aggregate the new response with the existing ones, creating a new
           * immutable list.
           */
          val newResponses = responses :+ response.asInstanceOf[Response]

          if (newResponses.size == k) {
            /**
             * We have exhausted the queue of response messages,
             * we can communicate the aggregated result and kill the actor.
             */
            val result = aggregateResponses(newResponses)
            replyTo ! result
            Behaviors.stopped
          } else {
            /** We're not done yet, we wait for other responses recursively */
            collecting(newResponses)
          }

        case ReceiveTimeout =>
          context.log.info("  ->Aggregator::ReceiveTimeout")

          val aggregatedResponses = aggregateResponses(responses)

          /**
           * Send the aggregated responses to the actor referenced by replyTo,
           * then kill the actor
           */
          replyTo ! aggregatedResponses
          Behaviors.stopped
      }

      /** Start collecting k responses */
      collecting(Vector.empty)
    }
  }
}
