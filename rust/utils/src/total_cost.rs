use datatypes::CostSet;

/// Returns the total cost of the sets in the list.
pub fn total_cost(s: &[CostSet]) -> u64 {
    s.iter().map(|si| si.cost).sum()
}
