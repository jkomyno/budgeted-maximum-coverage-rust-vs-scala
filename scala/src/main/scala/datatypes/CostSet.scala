package datatypes

final case class CostSet(s: List[WeightedItem], cost: Int) extends Ordered[CostSet] {
  lazy val weight: Int = s.map(_.weight).sum

  override def toString: String = s"CS(s=$s, c=$cost, w=$weight)"
  
  def compare(that: CostSet): Int = {
    if (this.weight == that.weight) {
      this.cost compare that.cost
    } else if (this.cost == that.cost) {
      that.weight compare this.weight
    } else {
      val tmp = that.weight compare this.weight
      if (tmp == 0) {
        this.cost compare that.cost
      } else {
        tmp
      }
    }
  }
}