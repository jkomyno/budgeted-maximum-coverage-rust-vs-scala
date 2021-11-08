use crate::csv_reader;
use datatypes::{CostSet, WeightedItem};
use std::io;

struct CSVLine(i64, WeightedItem, u64);

trait Stack<T> {
    fn top(&mut self) -> &mut T;
}

impl<T> Stack<T> for Vec<T> {
    fn top(&mut self) -> &mut T {
        let len = self.len();
        &mut self[len - 1]
    }
}

pub fn read_from_csv(path: &str) -> io::Result<Vec<CostSet>> {
    let csv_lines: io::Result<Vec<CSVLine>> = csv_reader::BufferedReader::open(path)?
        .skip(1)
        .map(|line| {
            let columns: Vec<String> = line?.split(',').map(|c| c.trim().to_owned()).collect();

            let i: i64 = columns[0].parse().unwrap();
            let id: String = columns[1].to_owned();
            let weight: u64 = columns[2].parse().unwrap();
            let cost: u64 = columns[3].parse().unwrap();

            let weighted_item = WeightedItem { id, weight };

            Ok(CSVLine(i, weighted_item, cost))
        })
        .collect();

    let start_value = (
        vec![Vec::<WeightedItem>::new()],
        Vec::<u64>::new(),
        -1 as i64,
    );

    let (weighted_item_groups, costs, _) = csv_lines?.into_iter().fold(start_value, |acc, curr| {
        let (mut weighted_item_groups, mut costs, latest_i) = acc;
        let CSVLine(i, weighted_item, cost) = curr;

        if i == latest_i {
            weighted_item_groups.top().push(weighted_item);
            (weighted_item_groups, costs, i)
        } else {
            costs.push(cost);
            weighted_item_groups.push(vec![weighted_item]);
            (weighted_item_groups, costs, i)
        }
    });

    let result: Vec<CostSet> = weighted_item_groups
        .into_iter()
        .zip(costs)
        .map(|(s, cost)| CostSet::new(s, cost))
        .collect();

    Ok(result)
}
