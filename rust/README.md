# Budgeted Maximum Coverage (Rust)

This project contains serial and parallel implementations of the Budgeted Maximum Coverage problem
written in Rust 1.56.

## Initialize project

Install third-party dependencies and compile:

```bash
cargo build --release
```

## Run concurrent example

```bash
./target/release/concurrent --help
```

```bash
./target/release/concurrent --weighted-items-csv ../data/weighted-items/V-n_1000.csv \
  --cost-sets-csv ../data/cost-sets/S-n_1000-m_20000-k_5_25.csv \
  -k 8 \
  -t 128 \
  -b 10
```

## Run serial example

```bash
./target/release/serial --help
```

```bash
./target/release/serial --weighted-items-csv ../data/weighted-items/V-n_1000.csv \
  --cost-sets-csv ../data/cost-sets/S-n_1000-m_20000-k_5_25.csv \
  -k 8 \
  -t 128 \
  -b 10
```
