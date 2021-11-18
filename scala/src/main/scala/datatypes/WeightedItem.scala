package datatypes

final case class WeightedItem(id: String, weight: Int) {
  override def toString: String = s"WI($id, w=$weight)"
}
