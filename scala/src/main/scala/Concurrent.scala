import org.rogach.scallop._

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.util.Timeout
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

import csv.{WeightedItemCSVReader, CostSetCSVReader}
import actors.shufflegreedy.{Supervisor}
import actors.shufflegreedy.{SupervisorParams}
import actors.shufflegreedy.Supervisor.{RunShuffleGreedy}
import actors.shufflegreedy.MasterShuffleGreedy.{MasterResponse, BMCSolution}
import utils._

object Serial {
  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    // number of local computing nodes
    val k = opt[Int](default=Some(4), validate=(_ > 0), descr="number of local computing nodes")

    // weighted items in CSV format
    val weightedItemsCSV = opt[String](required=true, short='v', descr="weighted items in CSV format")

    // cost sets in CSV format
    val costSetsCSV = opt[String](required=true, short='s', descr="cost sets in CSV format")

    // budget multiplier
    val budgetMultiplier = opt[Int](default=Some(1), short='b', validate=(_ > 0), descr="budget multiplier")

    // number of iterations
    val iterations = opt[Int](default=Some(8), short='t', validate=(_ > 0), descr="number of iterations")
    verify()
  }

  @main def main(args: String*) = {
    val conf = new Conf(args)

    val v = WeightedItemCSVReader.readFromCSV(conf.weightedItemsCSV())
    val s = CostSetCSVReader.readFromCSV(conf.costSetsCSV())

    // best possible objective reachable by the maximizer algorithm
    val totalWeight = utils.totalWeight(v)
    println(s"Weight of v: ${totalWeight}")

    // total cost of s
    val totalCost = utils.totalCost(s)
    println(s"Cost of s: ${totalCost}")

    // budget available
    val budget = totalWeight * conf.budgetMultiplier()
    println(s"Budget: ${budget}")

    // The goal of the problem is to find a collection of sets S' \subseteq S such that the
    // total cost of the elements is at most the given budget, and the total weight of
    // the elements covered by S' is maximized.
    val params = SupervisorParams(
      v=v,
      s=s,
      budget=budget,
      k=conf.k(),
      t=conf.iterations(),
    )

    // start supervisor of the actor system
    val system = ActorSystem(Supervisor(), "main-supervisor")

    /**
     * The ask pattern allows two actors to interact in such a way that there’s a 1 to 1 mapping between the request
     * and the response. Since there’s no guarantee that the responding actor will in fact ever respond, the ask pattern
     * requires to define a timeout after which the interaction is considered to fail.
     * `ask` is non-blocking. The Future result will be realized by an asynchronous concurrent thread.
     */
    implicit val timeout: Timeout = 10.seconds
    implicit val scheduler: Scheduler = system.scheduler
    implicit val ec: ExecutionContextExecutor = system.executionContext

    val result: Future[MasterResponse] = system.ask(RunShuffleGreedy(params, _))

    result.andThen {
      _ => {
        // stop actors
        system.terminate()
      }
    }.onComplete {
      case Success(
        BMCSolution(chosenSets, remainingBudget)
      ) => {
        println(s"chosenSets=${chosenSets.size}; remainingBudget=$remainingBudget")
      }

      case Failure(err) => {
        println(s"Future error:\n${err.getMessage}")
      }
    }
  }
}
