from ..rng import rng
import math
import os
import hydra
from omegaconf import DictConfig
from pathlib import Path
from operator import attrgetter
from .. import common
from ..datatypes import CostSet, WeightedItem
from .utils import CostSetHelper, WeightedItemsHelper
from typing import Tuple, List


def create_S(m: int,
             V: List[WeightedItem],
             S_cardinality_bounds: Tuple[int, int]) -> List[CostSet]:
    """
    :param m: number of priced sets containing elements of n, forming set S
    :param V: list of weighted items
    :param S_cardinality_bounds: lower and upper bounds of the cardinality of each subset of S,
           expressed in per-mil values of n.
           E.g., if n=10000 and S_cardinality_bounds=(5, 25), the smallest subset of S will have
           at least 50 = (n * 0.005) elements, whereas the greatest subset of S will have at most
           250 = (n * 0.0025) elements.
    """
    lower_bound, upper_bound = S_cardinality_bounds

    if lower_bound < 1 or lower_bound > 1000 \
            or upper_bound < 1 or upper_bound > 1000:
        raise AssertionError(f'the bounds of cardinality_permillage should be between 1 and 1000')

    if lower_bound > upper_bound:
        raise AssertionError(f'the bounds of cardinality_permillage should be monotone')

    n = len(V)
    cost_range = (1, 100)

    cardinality_range_min = math.ceil(n * lower_bound / 1000)
    cardinality_range_max = math.ceil(n * upper_bound / 1000)
    cardinality_range = (cardinality_range_min, cardinality_range_max)

    # maximum_allowed_m is the size of the powerset of the ground set restricted to the given cardinality bounds
    maximum_allowed_m = sum((math.comb(n, k) for k in range(cardinality_range_min, cardinality_range_max + 1)))

    if m > maximum_allowed_m:
        raise AssertionError(f'The maximum allowed number of sets is {maximum_allowed_m}')

    S = CostSetHelper.generate(rng, m, V,
                               cardinality_range=cardinality_range,
                               cost_range=cost_range)

    return S


def generate_V(n: int) -> List[WeightedItem]:
    weight_range = (1, 100)
    V = WeightedItemsHelper.generate(rng, n,
                                     weight_range=weight_range)
    
    return V


@hydra.main(config_path="../conf", config_name="config")
def generator(cfg: DictConfig) -> None:
    # boolean switch for verbose messages
    is_verbose = bool(cfg.selected.verbose)

    # basedir w.r.t. main.py
    basedir = os.path.join(hydra.utils.get_original_cwd(), Path(__file__).parent.parent.parent)

    # output directory
    out_dir = f'{basedir}/data'
    n = cfg.size.n

    ################
    #  generate V  #
    ################

    V = generate_V(n=n)
    df_V = WeightedItemsHelper.to_df(V)
    V_filename = f'V-n_{n}'       

    common.to_csv(f'{out_dir}/weighted-items', df=df_V, name=V_filename, create_path=True)

    ################
    #  generate S  #
    ################

    destructure = attrgetter('m', 'S_cardinality_bounds')    
    for size in cfg.size.sizes:
        m, S_cardinality_bounds = destructure(size)
        S_cardinality_bounds = tuple(S_cardinality_bounds)
        
        S = create_S(m, V, S_cardinality_bounds)
        df_S = CostSetHelper.to_df(S)
        lower_bound, upper_bound = S_cardinality_bounds
        S_filename = f'S-n_{n}-m_{m}-k_{lower_bound}_{upper_bound}'

        common.to_csv(f'{out_dir}/cost-sets', df=df_S, name=S_filename, create_path=True)
