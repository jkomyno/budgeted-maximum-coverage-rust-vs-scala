use datatypes::CostSet;

use crate::distinct_items::distinct_items;

pub fn compute_objective<'a>(s: impl Iterator<Item = &'a CostSet>) -> u64 {
    distinct_items(s).iter().map(|wi| wi.weight).sum()
}
