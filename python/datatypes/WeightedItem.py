from dataclasses import dataclass


@dataclass(frozen=True)
class WeightedItem:
    """
    A WeightedItem is charcterized by an identifier and a weight.
    """

    __slots__ = ['id', 'weight']

    id: str
    weight: int

    def __repr__(self):
        return f'WI: ({self.id}: {self.weight})'
