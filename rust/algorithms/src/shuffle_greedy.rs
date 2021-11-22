use std::collections::HashSet;
use datatypes::{CostSet, IndexCostSet};
use local_nodes::{ShuffleGreedyNode, ShuffleSolution};
use std::{cmp::max, collections::HashMap};
use libs::rand_xoshiro::rand_core::SeedableRng;
use libs::rand_xoshiro::Xoshiro256PlusPlus;

#[derive(Debug)]
pub struct ShuffleMasterSolution {
    pub g: Vec<CostSet>,
    pub objective: u64,
    pub cost: u64,
    pub best_t: u64,
}

#[derive(Debug)]
pub struct BestResult {
    pub solution: Vec<Vec<usize>>,
    pub objective: u64,
    pub cost: u64,
    pub t: u64,
}

pub fn shuffle_greedy(
    s_index: Vec<IndexCostSet>,
    budget: u64,
    k: usize,
    max_t: u64,
    initializer: impl FnOnce(&[IndexCostSet], &Xoshiro256PlusPlus) -> Vec<ShuffleGreedyNode>,
    maximizer: impl Fn(&[ShuffleGreedyNode], &[u64]) -> Vec<ShuffleSolution>,
    shuffler: impl Fn(&mut [ShuffleGreedyNode]) -> (),
) -> ShuffleMasterSolution {
    // HashMap to retrieve the original CostSet from an index in s_index
    let index_to_cost_set: HashMap<usize, &CostSet> =
        s_index.iter().map(|IndexCostSet(cs, j)| (*j, cs)).collect();

    // initially, all k local nodes have the same budget multiplier
    let mut local_budget_multipliers = vec![1; k];

    // initialize k local nodes
    let rng = SeedableRng::seed_from_u64(2022);
    let mut local_nodes = initializer(&s_index, &rng);

    let mut best_result = BestResult {
        solution: Vec::new(),
        objective: 0,
        cost: budget + 1,
        t: 0,
    };

    let mut t = 1;

    while t <= max_t {
        // split the budget in k parts dependent on local_budget_multipliers
        let local_budgets =
            utils::split_budget(budget, &local_budget_multipliers);
        // println!("local_budgets: {:?}", local_budgets);

        // compute k local solutions
        let local_results: Vec<ShuffleSolution> = maximizer(&local_nodes, &local_budgets);

        let local_cost: u64 = local_results.iter().map(|sol| sol.cost).sum();
        // println!("local_cost: {:?}", local_cost);

        let local_objectives = local_results
            .iter()
            .map(|sol| utils::compute_objective(sol.g.iter().map(|j| index_to_cost_set[j])));

        let curr_selection = local_results
            .iter()
            .flat_map(|sol| sol.g.iter().map(|j| index_to_cost_set[j]));

        // sum of weights of the distinct items found in each of the local solutions
        let curr_solution_objective = curr_selection
            .fold(HashSet::new(), |mut acc, cs| {
                acc.extend(cs.s.iter());
                acc
            })
            .iter().map(|wi| wi.weight).sum();

        // println!("curr_solution_objective: {:?}", curr_solution_objective);

        // adjust budgets for the next iteration
        local_budget_multipliers = local_objectives
            .map(|local_objective| max(local_objective, 1))
            .collect();

        // accept the current solution if it's better than the previous best one
        if curr_solution_objective >= best_result.objective {
            let curr_solution: Vec<Vec<usize>> =
                local_results.into_iter().map(move |sol| sol.g).collect();

            best_result = BestResult {
                solution: curr_solution,
                objective: curr_solution_objective,
                cost: local_cost,
                t: t,
            }
        }

        // shuffle sets in the local nodes
        shuffler(&mut local_nodes);

        // increase iterations
        t += 1;
    }

    let selection: Vec<CostSet> = best_result
        .solution
        .iter()
        .flat_map(|sol| sol.iter().map(|j| index_to_cost_set[j].to_owned()))
        .collect();

    ShuffleMasterSolution {
        g: selection,
        objective: best_result.objective,
        cost: best_result.cost,
        best_t: best_result.t,
    }
}
