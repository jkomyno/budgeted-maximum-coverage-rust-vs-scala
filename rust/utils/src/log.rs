/// Logarithm function for a generic base
pub fn log(x: f64, base: f64) -> f64 {
    f64::ln(x) / f64::ln(base)
}
