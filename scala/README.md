# Budgeted Maximum Coverage (Scala)

This project contains serial and parallel implementations of the Budgeted Maximum Coverage problem
written in Scala 3.1.0.

## Initialize project

Install third-party dependencies and compile:

```bash
sbt compile
```

```bash
sbt "run -v ../data/weighted-items/V-n_1000.csv -s ../data/cost-sets/S-n_1000-m_20000-k_5_25.csv -t 8 -k 4"
```
