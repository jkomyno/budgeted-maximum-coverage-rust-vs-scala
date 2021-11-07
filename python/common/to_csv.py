from pathlib import Path
import pandas as pd


def to_csv(out_dir: str, df: pd.DataFrame, name: str, create_path: True):
    if create_path:
        Path(out_dir).mkdir(parents=True, exist_ok=True)
    df.to_csv(f'{out_dir}/{name}.csv', mode='w', header=True, index=False)
