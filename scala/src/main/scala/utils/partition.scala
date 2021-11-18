package utils

object partition {
  /**
   * Partition a list into k lists whose sizes are as close as possible
   * to each other's.
   * @param lst the list to partition
   * @param k the number of partitions
   */
  def apply[T](lst: List[T], k: Int): IndexedSeq[List[T]] = {
    val size = lst.size
    assert (size >= k)

    val nSmaller = math.floor(size / k).toInt
    val nBigger = size % k

    val biggerIncrement = nSmaller + 1
    val biggerEnd = biggerIncrement * nBigger

    val smallerIncrement = nSmaller
    val smallerEnd = size

    val res1 = for (i <- 0 until biggerEnd by biggerIncrement)
      yield lst.slice(i, i + biggerIncrement)

    val res2 = for (i <- biggerEnd until smallerEnd by smallerIncrement)
      yield lst.slice(i, i + smallerIncrement)

    res1 ++ res2
  }
}
