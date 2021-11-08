use std::cmp;

pub fn choose(n: u64, r: u64) -> u64 {
    let r = cmp::min(r, n - r);

    let num = (n - r..n).rev().fold(1u64, |acc, curr| acc * curr);
    let den = (1..r + 1).fold(1u64, |acc, curr| acc * curr);

    println!(
        "r: {r}, num: {num}, den: {den}",
        r = r,
        num = num,
        den = den
    );

    ((num as f64) / (den as f64)).floor() as u64
}
