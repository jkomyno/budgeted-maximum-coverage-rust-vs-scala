package csv

import datatypes.{WeightedItem}

object WeightedItemCSVReader {
  def readFromCSV(path: String): List[WeightedItem] = {
    // id,weight
    io.Source.fromFile(path, enc="utf-8")
      .getLines()
      .drop(1)
      .map(line => {
        val Array(id, weight) = line.split(",").map(_.trim)
        WeightedItem(id, weight.toInt)
      })
      .toList
  }
}
