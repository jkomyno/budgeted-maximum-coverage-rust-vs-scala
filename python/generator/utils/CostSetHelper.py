import numpy as np
import pandas as pd
from typing import List, Tuple, Set, FrozenSet
from ...datatypes import CostSet, WeightedItem


class CostSetHelper(object):
    @staticmethod
    def to_df(cost_sets: List[CostSet]):
        data = [
            {
                'i': j,
                'id': wi.id,
                'weight': wi.weight,
                'cost': cs.cost
            }
            for j, cs in enumerate(cost_sets)
            for wi in cs.s
        ]
        return pd.DataFrame(data)

    @staticmethod
    def generate(rng: np.random.Generator,
                 m: int,
                 V: List[WeightedItem],
                 cardinality_range: Tuple[int, int],
                 cost_range: Tuple[int, int]) -> List[CostSet]:
        S: Set[FrozenSet[WeightedItem]] = set()

        while len(S) < m:
            cardinality = rng.integers(*cardinality_range, endpoint=True)
            subset = rng.choice(V, cardinality, replace=False)
            S.add(frozenset(subset))

        return [
            CostSet(s=weighted_items, cost=rng.integers(*cost_range, endpoint=True))
            for weighted_items in S
        ]
