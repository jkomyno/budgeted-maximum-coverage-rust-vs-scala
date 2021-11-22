use datatypes::IndexCostSet;
use libs::rayon;
use libs::rayon::prelude::*;
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

    let thread_pool = rayon::ThreadPoolBuilder::new()
        .num_threads(k)
        .build()
        .unwrap();

    // each set in s is assigned a distinct index
    let s_index: Vec<IndexCostSet> = s
        .into_par_iter()
        .enumerate()
        .map(|(i, cs)| IndexCostSet(cs, i))
        .collect();
    
    let initializer = |s_index: &[IndexCostSet], rng: &Xoshiro256PlusPlus| -> Vec<ShuffleGreedyNode> {
        utils::partition(&s_index, k)
            .map(|indexed_cs| ShuffleGreedyNode::new(indexed_cs.to_vec(), &rng))
            .collect()
    };

    // concurrent initialization of k local nodes (significantly slower than the sequential one)
    // let initializer = |s_index: &[IndexCostSet], rng: &Xoshiro256PlusPlus| -> Vec<ShuffleGreedyNode> {
    //     thread_pool.install(|| {
    //         utils::partition(&s_index, k)
    //             .par_bridge()
    //             .map(|indexed_cs| ShuffleGreedyNode::new(indexed_cs.to_vec(), &rng))
    //             .collect()
    //     })
    // };

    let maximizer = |local_nodes: &[ShuffleGreedyNode], local_budgets: &[u64]| -> Vec<ShuffleSolution> {
        thread_pool.install(|| {
            local_nodes
                .par_iter()
                .zip(local_budgets)
                .map(|(node, local_budget)| {
                    node.maximize(*local_budget)
                })
                .collect::<Vec<_>>()
        })
    };

    // serial shuffler function
    // let shuffler = |local_nodes: &mut [ShuffleGreedyNode]| -> () {
    //     local_nodes.iter_mut().for_each(|node| {
    //         node.shuffle();
    //     });
    // };

    // concurrent shuffler function
    let shuffler = |local_nodes: &mut [ShuffleGreedyNode]| -> () {
        thread_pool.install(|| {
            local_nodes.par_iter_mut().for_each(|node| {
                node.shuffle();
            });
        });
    };

    algorithms::shuffle_greedy(s_index, budget, k, max_t, initializer, maximizer, shuffler)
}
