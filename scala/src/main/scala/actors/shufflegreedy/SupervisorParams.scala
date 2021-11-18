package actors.shufflegreedy

import datatypes.{WeightedItem, CostSet}

/** Params for the Supervisor object.
 * 
 * @param v the ground set of weighted items
 * @param s the collection of priced sets of weighted items
 * @param budget the budget available for the maximum budgeted coverage problem
 * @param k the number of nodes to spawn
 * @param t number of iterations
 */
final case class SupervisorParams(v: List[WeightedItem],
                                  s: List[CostSet],
                                  budget: Int,
                                  k: Int,
                                  t: Int)
