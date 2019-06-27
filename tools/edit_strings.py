#!/usr/bin/env python

import os
from os.path import join

BASE = '../common/src/main/res'
STRINGS = 'strings.xml'

def accept(line):
    return 'name="about_msg"' not in line

for root, dirs, files in os.walk(BASE):
    if STRINGS in files:
        path = join(root, STRINGS)
        xml = "" 
        for line in open(path).readlines():
            if accept(line):
                xml += line
        open(path, "w").write(xml)

