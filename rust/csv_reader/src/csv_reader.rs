use std::{
    fs::File,
    io::{self, BufRead},
    rc,
};

pub struct BufferedReader {
    reader: io::BufReader<File>,

    // rc::Rc supports multiple ownership in the same thread
    buffer: rc::Rc<String>,
}

fn new_buffer() -> rc::Rc<String> {
    rc::Rc::new(String::with_capacity(1024 * 1024))
}

impl BufferedReader {
    pub fn open(path: impl AsRef<std::path::Path>) -> io::Result<Self> {
        let file = File::open(path)?;
        let reader = io::BufReader::new(file);
        let buffer = new_buffer();

        Ok(Self { reader, buffer })
    }
}

impl Iterator for BufferedReader {
    type Item = io::Result<rc::Rc<String>>;

    fn next(&mut self) -> Option<Self::Item> {
        let buffer = match rc::Rc::get_mut(&mut self.buffer) {
            Some(buffer) => {
                buffer.clear();
                buffer
            }
            None => {
                self.buffer = new_buffer();
                rc::Rc::make_mut(&mut self.buffer)
            }
        };

        self.reader
            .read_line(buffer)
            .map(|n| {
                if n == 0 {
                    None
                } else {
                    Some(rc::Rc::clone(&self.buffer))
                }
            })
            // transposes a Result of an Option into an Option of a Result
            .transpose()
    }
}
