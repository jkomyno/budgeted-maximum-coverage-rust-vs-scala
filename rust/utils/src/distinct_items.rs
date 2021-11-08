use std::collections::HashSet;

use datatypes::{CostSet, WeightedItem};

pub fn distinct_items<'a>(s: impl Iterator<Item = &'a CostSet>) -> HashSet<&'a WeightedItem> {
    s.fold(HashSet::new(), |mut acc, cs| {
        acc.extend(cs.s.iter());
        acc
    })
}
