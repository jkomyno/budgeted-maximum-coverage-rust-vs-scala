use std::cmp::Ordering;

use crate::cost_set::CostSet;

#[derive(Eq, PartialEq, Clone, Debug)]
/// IndexCostSet is a CostSet paired with a unique index.
/// It is implemented as a "tuple struct" in order to define a sorting policy that ignores the
/// index and delegates to the CostSet comparator
pub struct IndexCostSet(pub CostSet, pub usize);

impl PartialOrd for IndexCostSet {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for IndexCostSet {
    fn cmp(&self, other: &Self) -> Ordering {
        let IndexCostSet(a, _) = self;
        let IndexCostSet(b, _) = other;

        a.cmp(b)
    }
}
