package utils

import datatypes.{CostSet}

object totalCost {
  def apply(s: List[CostSet]): Int = s.map(_.cost).sum
}
