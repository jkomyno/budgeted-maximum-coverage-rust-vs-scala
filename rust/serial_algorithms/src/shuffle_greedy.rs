use algorithms::ShuffleMasterSolution;
use datatypes::{CostSet, IndexCostSet};
use local_nodes::{LocalNode, ShuffleGreedyNode, ShuffleSolution};
use libs::rand_xoshiro::Xoshiro256PlusPlus;

pub fn shuffle_greedy(
    s: Vec<CostSet>,
    budget: u64,
    k: usize,
    max_t: u64,
) -> ShuffleMasterSolution {
    // each set in s is assigned a distinct index
    let s_index: Vec<IndexCostSet> = s
        .into_iter()
        .enumerate()
        .map(|(i, cs)| IndexCostSet(cs, i))
        .collect();

    // serial initialization of k local nodes
    let initializer = |s_index: &[IndexCostSet], rng: &Xoshiro256PlusPlus| -> Vec<ShuffleGreedyNode> {
        utils::partition(&s_index, k)
            .map(|indexed_cs| ShuffleGreedyNode::new(indexed_cs.to_vec(), &rng))
            .collect()
    };

    // serial maximizer function
    fn maximizer(
        local_nodes: &[ShuffleGreedyNode],
        local_budgets: &[u64],
    ) -> Vec<ShuffleSolution> {
        local_nodes
            .iter()
            .zip(local_budgets)
            .map(|(node, local_budget)| node.maximize(*local_budget))
            .collect()
    }

    // serial shuffler function
    fn shuffler(local_nodes: &mut [ShuffleGreedyNode]) -> () {
        local_nodes.iter_mut().for_each(|node| node.shuffle())
    }

    algorithms::shuffle_greedy(s_index, budget, k, max_t, initializer, maximizer, shuffler)
}
