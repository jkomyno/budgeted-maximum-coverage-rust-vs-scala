use datatypes::IndexCostSet;
use libs::rand::seq::SliceRandom;  // provides random mutation and sampling methods
use libs::rand_xoshiro::Xoshiro256PlusPlus;

use crate::local_node::LocalNode;

// TODO: derive derive_getters
pub struct ShuffleSolution {
    pub g: Vec<usize>,
    pub spare_budget: u64,
    pub weight: u64,
    pub cost: u64,
}

pub struct ShuffleGreedyNode {
    local_indexed_s: Vec<IndexCostSet>,
    _rng: Xoshiro256PlusPlus,
}

impl ShuffleGreedyNode {
    pub fn new(indexed_s: Vec<IndexCostSet>, rng: &Xoshiro256PlusPlus) -> Self {
        let mut local_indexed_s: Vec<_> = indexed_s.clone();
        local_indexed_s.sort();

        let _rng = rng.clone();

        ShuffleGreedyNode {
            local_indexed_s,
            _rng,
        }
    }
}

impl LocalNode<ShuffleSolution> for ShuffleGreedyNode {
    /// Randomly shuffle the local subsets.
    fn shuffle(&mut self) -> () {
        self.local_indexed_s.shuffle(&mut self._rng)
    }

    /// Find a collection of sets g \subseteq local_indexed_s such that the total cost of elements
    /// in g does not exceed the given budget local_budget, and that the total weight of elements
    /// covered by g is approximately maximized.
    fn maximize(&mut self, local_budget: u64) -> ShuffleSolution {
        let mut g: Vec<usize> = Vec::new();
        let mut spare_budget = local_budget;
        let mut weight: u64 = 0;
        let mut cost: u64 = 0;

        for IndexCostSet(s, j) in &self.local_indexed_s {
            // if the cost of the current set doesn't exceed the budget, add it to the solution
            // and decrease the budget accordingly

            if s.cost <= spare_budget {
                g.push(*j);
                spare_budget -= s.cost;
                cost += s.cost;
                weight += s.weight();
            }

            if spare_budget == 0 {
                break;
            }
        }

        // println!("budget: {:?}; weight: {:?}; cost: {:?}\n", local_budget, weight, cost);

        ShuffleSolution {
            g,
            spare_budget,
            weight,
            cost,
        }
    }
}
