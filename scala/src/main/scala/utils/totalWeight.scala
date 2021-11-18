package utils

import datatypes.{WeightedItem}

object totalWeight {
  def apply(u: List[WeightedItem]): Int = u.map(_.weight).sum
}
