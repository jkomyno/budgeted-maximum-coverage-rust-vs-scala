from dataclasses import dataclass
from typing import FrozenSet
from .WeightedItem import WeightedItem


@dataclass(frozen=True)
class CostSet:
    """
    A CostSet is a set of WeightedItems characterized by a cost.
    The weight of a CostSet is the sum of the weights of the WeightedItems it contains.
    """

    s: FrozenSet[WeightedItem]
    cost: int
    weight: int = 0

    def __post_init__(self):
        object.__setattr__(self, 'weight', sum(map(lambda x: x.weight, self.s)))

    def __repr__(self):
        return f'CS: (s={self.s}, cost={self.cost}, weight={self.weight})'
