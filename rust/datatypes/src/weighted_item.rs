use core::fmt;

#[derive(Eq, PartialEq, Clone, Hash)]
/// WeightedItem models an item in the ground set equipped with a weight
pub struct WeightedItem {
    /// unique identifer of an item in the ground set
    pub id: String,

    /// weight of an item in the ground set
    pub weight: u64,
}

impl fmt::Debug for WeightedItem {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("WI")
            .field("id", &self.id)
            .field("w", &self.weight)
            .finish()
    }
}
