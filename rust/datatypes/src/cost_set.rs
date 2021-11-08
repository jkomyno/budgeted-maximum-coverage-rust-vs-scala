use crate::weighted_item::WeightedItem;
use std::{cmp::Ordering, fmt};

#[derive(Clone, Debug)]
pub struct RawCostSet<'a> {
    pub s: Vec<&'a WeightedItem>,
    pub cost: u64,
}

#[derive(Eq, Clone, Hash)]
/// CostSet is a subset of the ground set equipped with a cost
pub struct CostSet {
    /// subset of the ground set
    pub s: Vec<WeightedItem>,

    /// cost of the subset s
    pub cost: u64,

    /// total weight of the items in s. It is a computed field
    _weight: u64,
}

impl CostSet {
    /// Constructor of a CostSet object
    pub fn new(s: Vec<WeightedItem>, cost: u64) -> Self {
        let _weight = s.iter().map(|s| s.weight).sum();

        return CostSet { s, cost, _weight };
    }

    pub fn weight(&self) -> u64 {
        self._weight
    }
}

impl fmt::Debug for CostSet {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("CS")
            // .field("s", &self.s)
            .field("c", &self.cost)
            .field("w", &self._weight)
            .finish()
    }
}

impl PartialEq for CostSet {
    fn eq(&self, other: &Self) -> bool {
        self.cost == other.cost && self.weight() == other.weight() && self.s == other.s
    }
}

impl PartialOrd for CostSet {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for CostSet {
    fn cmp(&self, other: &Self) -> Ordering {
        if self.weight() == other.weight() {
            self.cost.cmp(&other.cost)
        } else if self.cost == other.cost {
            other.weight().cmp(&self.weight())
        } else {
            other
                .weight()
                .cmp(&self.weight())
                .then_with(|| self.cost.cmp(&other.cost))
        }
    }
}
