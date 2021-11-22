use serial_algorithms::shuffle_greedy;
use structopt::StructOpt;

#[derive(StructOpt)]
/// Weighted Set Cover - (Serial version)
struct Cli {
    /// number of local computing nodes
    #[structopt(short, default_value = "4")]
    k: usize,

    /// weighted items in CSV format
    #[structopt(short = "v", long)]
    weighted_items_csv: String,

    /// cost sets in CSV format
    #[structopt(short = "s", long)]
    cost_sets_csv: String,

    /// budget multiplier
    #[structopt(short = "b", long = "--budget", default_value = "1")]
    budget_multiplier: u64,

    /// number of iterations
    #[structopt(short = "t", long = "--iterations", default_value = "8")]
    t: u64,
}

fn main() {
    let args = Cli::from_args();

    let v = match csv_reader::weighted_items::read_from_csv(&args.weighted_items_csv) {
        Ok(v) => v,
        Err(err) => panic!("Cannot read weighted items: {}", err),
    };

    let s = match csv_reader::cost_sets::read_from_csv(&args.cost_sets_csv) {
        Ok(s) => s,
        Err(err) => panic!("Cannot read cost sets: {}", err),
    };

    // best possible objective reachable by the maximizer algorithm
    let total_weight = utils::total_weight(&v);
    println!("Weight of v: {:?}", total_weight);

    // total cost of the sets s
    let total_cost = utils::total_cost(&s);
    println!("Cost of s: {:?}", total_cost);

    // budget available
    let budget: u64 = total_weight * args.budget_multiplier;
    println!("Budget: {:?}", budget);

    // The goal of the problem is to find a collection of sets S' \subseteq S such that the
    // total cost of the elements is at most the given budget, and the total weight of
    // the elements covered by S' is maximized.
    let sol = shuffle_greedy(s, budget, args.k, args.t);
    println!("Objective: {:?}; Cost: {:?}; Cardinality: {:?}", sol.objective, sol.cost, sol.g.len());
}
