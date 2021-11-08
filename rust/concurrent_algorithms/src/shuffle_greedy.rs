use datatypes::IndexCostSet;
use libs::rayon;
use libs::rayon::prelude::*;
use libs::rand_xoshiro::rand_core::SeedableRng;
use libs::rand_xoshiro::Xoshiro256PlusPlus;
use algorithms::ShuffleMasterSolution;
use datatypes::CostSet;
use local_nodes::{LocalNode, ShuffleGreedyNode, ShuffleSolution};


pub fn shuffle_greedy(
    s: Vec<CostSet>,
    budget: u64,
    k: usize,
    max_t: u64,
) -> ShuffleMasterSolution {

    rayon::ThreadPoolBuilder::new()
        .num_threads(k)
        .build_global()
        .unwrap();

    // each set in s is assigned a distinct index
    let s_index: Vec<IndexCostSet> = s
        .into_iter()
        .enumerate()
        .par_bridge()
        .map(|(i, cs)| IndexCostSet(cs, i))
        .collect();


    // serial initialization of k local nodes
    let initializer = |s_index: &[IndexCostSet], rng: &Xoshiro256PlusPlus| -> Vec<ShuffleGreedyNode> {
        utils::partition(&s_index, k)
            .map(|indexed_cs| ShuffleGreedyNode::new(indexed_cs.to_vec(), &rng))
            .collect()
    };

    // parallel initialization of k local nodes
    // let initializer = |s_index: &[IndexCostSet], rng: &Xoshiro256PlusPlus| -> Vec<ShuffleGreedyNode> {
    //     utils::partition(&s_index, k)
    //         .par_bridge()
    //         .map(|indexed_cs| ShuffleGreedyNode::new(indexed_cs.to_vec(), &rng))
    //         .collect()
    // };

    let maximizer = |local_nodes: &mut [ShuffleGreedyNode], local_budgets: &[u64]| -> Vec<ShuffleSolution> {
        local_nodes
            .iter_mut()
            .zip(local_budgets)
            .par_bridge()
            .map(|(node, local_budget)| {
                node.maximize(*local_budget)
            })
            .collect::<Vec<_>>()
    };

    // concurrent shuffler function
    let shuffler = |local_nodes: &mut [ShuffleGreedyNode]| -> () {
        rayon::in_place_scope(|s| {
            local_nodes.iter_mut().for_each(|node| {
                s.spawn(move |_| node.shuffle());
            });
        });
    };

    algorithms::shuffle_greedy(s_index, budget, k, max_t, initializer, maximizer, shuffler)
}
