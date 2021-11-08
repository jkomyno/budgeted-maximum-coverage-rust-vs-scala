/// Partition S into S_1, S_2, ..., S_k, such that |S_i| == |S_j| and
/// S_i \cap S_j == \emptyset for any i \neq j.
/// Each of the k sets has approximately the same number of WeightedItem subsets, but the subsets
/// might have a different number of items
pub fn partition<T>(lst: &[T], k: usize) -> impl Iterator<Item = &[T]> {
    let size = lst.len();
    let n_smaller = size / k;
    let n_bigger = size % k;

    let bigger_increment = n_smaller + 1;
    let bigger_end = bigger_increment * n_bigger;

    let smaller_increment = n_smaller;
    let smaller_end = size;

    let it_1 = (0..bigger_end)
        .step_by(bigger_increment)
        .map(move |i| &lst[i..(i + bigger_increment)]);

    let it_2 = (bigger_end..smaller_end)
        .step_by(smaller_increment)
        .map(move |i| &lst[i..(i + smaller_increment)]);

    it_1.chain(it_2)
}
