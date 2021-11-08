use std::io;

use datatypes::WeightedItem;

use crate::csv_reader;

pub fn read_from_csv(path: &str) -> io::Result<Vec<WeightedItem>> {
    csv_reader::BufferedReader::open(path)?
        .skip(1)
        .map(|line| {
            let columns: Vec<String> = line?.split(',').map(|c| c.trim().to_owned()).collect();

            let id: String = columns[0].to_owned();
            let weight: u64 = columns[1].parse().unwrap();
            Ok(WeightedItem { id, weight })
        })
        .collect()
}
