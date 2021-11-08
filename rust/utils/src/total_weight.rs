use datatypes::WeightedItem;

/// Returns the total weight of the items in the list.
pub fn total_weight(u: &[WeightedItem]) -> u64 {
    u.iter().map(|wi| wi.weight).sum()
}
