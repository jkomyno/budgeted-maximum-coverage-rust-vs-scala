/// Split the given budget into |multipliers| parts such that the sum of the parts is equal to
/// budget, budget[i] >= budget[j] when multipliers[i] >= multipliers[j], and, for all i,
/// budget[i] >= 1.
pub fn split_budget(budget: u64, multipliers: &[u64]) -> Vec<u64> {
    let k = multipliers.len();
  
    let multipliers_sum: u64 = multipliers.iter().sum();

    let mut diff = 0.0;
    let mut local_budgets: Vec<_> = vec![0; k];

    for i in 0..k {
      let multiplier = multipliers[i];
      let v: f64 = (budget as f64) * (multiplier as f64) / (multipliers_sum as f64);
      let mut value: i64 = v.ceil() as i64;
      diff += v - (value as f64);

      if diff >= 0.5 {
        value += 1;
        diff -= 1.0;
      } else if diff <= -0.5 {
        value -= 1;
        diff += 1.0;
      }

      local_budgets[i] = value as u64;
    };

    local_budgets
}

#[cfg(test)]
mod test {
    use super::*;
    
    #[test]
    fn it_should_split() {
      let budget: u64 = 50718;
      let k: usize = 4;
      let multiplies = vec![1; k];

      let res = split_budget(budget, &multiplies);

      assert_eq!(vec![12679, 12680, 12679, 12680], res);
    }
}
