#!/usr/bin/env python3

import os
import requests

filename = 'res/raw/emojione_shortcodes.map'


def unicode_output_to_string(output):
    return ''.join(map(lambda s: chr(int(s, 16)), output.split("-")))


if not os.path.exists(os.path.dirname(filename)):
    try:
        os.makedirs(os.path.dirname(filename))
    except OSError as exc:  # Guard against race condition
        if exc.errno != errno.EEXIST:
            raise

data = requests.get(
    'https://raw.githubusercontent.com/Ranks/emojione/v3.1.1/emoji_strategy.json').json()

with open(filename, 'w') as f:
    for key in data:
        strategy = data[key]
        output = strategy['unicode_output']
        f.write('%s=%s\n' % (strategy['shortname'], unicode_output_to_string(output)))
