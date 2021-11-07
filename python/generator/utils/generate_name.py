import string
import itertools
from typing import Iterator


def generate_name(length: int) -> Iterator[str]:
    '''
    Generate `52^length` unique strings of size `length`.
    E.g., with length=3, 'aaa', 'aab', ..., 'zzz'
    '''

    # lower case and upper case, 52 characters
    chars = string.ascii_letters
    # return map(lambda item: ''.join(item), itertools.product(chars, repeat=length))

    for item in itertools.product(chars, repeat=length):
        yield ''.join(item)


generate_name.alphabet_size = 52
