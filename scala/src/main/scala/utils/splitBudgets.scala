package utils

object splitBudgets {
  def apply(budget: Int, multipliers: List[Int]): List[Int] = {
    val k = multipliers.size
    val multipliersSum = multipliers.sum
    var diff: Double = 0.0
    
    val localBudgets = for (i <- 0 until k) yield {
      val multiplier = multipliers(i)
      val v: Double = budget * (multiplier.toDouble) / multipliersSum
      var value = v.ceil.toInt
      diff += v - value
      
      if (diff >= 0.5) {
        value += 1;
        diff -= 1.0;
      } else if (diff <= -0.5) {
        value -= 1;
        diff += 1.0;
      }
      
      value
    }

    localBudgets.toList
  }
}
