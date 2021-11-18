package actors.shufflegreedy

final case class BestResult(solution: List[List[Int]],
                            weight: Int,
                            cost: Int,
                            t: Int) {
  override def toString = s"BestResult(s=$solution, w=$weight, c=$cost, t=$t)"
}
