pub trait LocalNode<Solution> {
    /// Randomly shuffle the local subsets.
    fn shuffle(&mut self) -> ();

    /// Approximately maximize the objective function in the local node.
    fn maximize(&mut self, local_budget: u64) -> Solution;
}
