# Budgeted Maximum Coverage (Scala)

This project contains serial and parallel implementations of the Budgeted Maximum Coverage problem
written in Scala 3.0.2.

## Initialize project

Install third-party dependencies and compile:

```bash
sbt compile
```

## Run concurrent example

```bash
sbt "run -v ../data/weighted-items/V-n_1000.csv \
  -s ../data/cost-sets/S-n_1000-m_20000-k_5_25.csv \
  -t 2 \
  -k 2"
```
