package csv

import datatypes.{WeightedItem, CostSet}

import scala.Function.tupled
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object CostSetCSVReader {
  private final case class CSVLine(i: Int, weightedItem: WeightedItem, cost: Int)

  def readFromCSV(path: String): List[CostSet] = {
    // i,id,weight,cost
    val csvLines = io.Source.fromFile(path, enc="utf-8")
      .getLines()
      .drop(1)
      .map(line => {
        val Array(i, id, weight, cost) = line.split(",").map(_.trim)
        CSVLine(i.toInt, WeightedItem(id, weight.toInt), cost.toInt)
      })
      .toList

    val startValue = (
      // weightedItemsGroups
      mutable.ListBuffer[mutable.ListBuffer[WeightedItem]](),

      // costs
      mutable.ListBuffer[Int](),

      // lastI
      -1
    )

    val (weightedItemsGroups, costs, _) = csvLines.foldLeft(startValue) {
      (acc, curr) => {
        val (weightedItemsGroups, costs, lastI) = acc
        
        if (curr.i == lastI) {
          weightedItemsGroups.last.append(curr.weightedItem)
          acc
        } else {
          costs.append(curr.cost)
          weightedItemsGroups.append(ListBuffer.from(List(curr.weightedItem)))
          (weightedItemsGroups, costs, curr.i)
        }
      }
    }

    val result = (weightedItemsGroups zip costs) map tupled {
      (s, cost) => CostSet(s.toList, cost)
    }

    result.toList
  }
}
