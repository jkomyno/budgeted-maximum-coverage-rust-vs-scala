import math
import numpy as np
import pandas as pd
from typing import List, Tuple
from .generate_name import generate_name
from ...datatypes import WeightedItem


class WeightedItemsHelper(object):
    @staticmethod
    def to_df(weighted_items: List[WeightedItem]):
        ids = [wi.id for wi in weighted_items]
        weights = [wi.weight for wi in weighted_items]

        return pd.DataFrame({
            'id': ids,
            'weight': weights,
        })

    @staticmethod
    def from_df(df: pd.DataFrame) -> List[WeightedItem]:
        ids = df.id.to_list()
        weights = df.weight.to_list()

        return [WeightedItem(id=id, weight=weight) for id, weight in zip(ids, weights)]

    @staticmethod
    def generate(rng: np.random.Generator,
                 n: int,
                 weight_range: Tuple[int, int]) -> List[WeightedItem]:
        alphabet_size: int = generate_name.alphabet_size
        name_length = math.ceil(math.log(n, alphabet_size))

        return [
            WeightedItem(id=name, weight=rng.integers(*weight_range, endpoint=True))
            for _, name in zip(range(n), generate_name(length=name_length))
        ]
