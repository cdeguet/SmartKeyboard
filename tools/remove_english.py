#!/usr/bin/env python

import os
from os.path import join
from lxml import etree

BASE = '../app/src/main/res'
STRINGS = 'strings.xml'


def read_english(path):
    words = {}
    tree = etree.parse(path)
    for child in tree.getroot():
        if child.tag == 'string':
            name = child.get('name')
            value = child.text
            words[name] = value
    return words


def process_lang(path, en_words):
    print "process", path
    tree = etree.parse(path)
    for child in tree.getroot():
        if child.tag == 'string':
            name = child.get('name')
            value = child.text
            if en_words[name] == value:
                child.getparent().remove(child)
    new_xml = etree.tostring(tree.getroot(), xml_declaration=True, encoding='utf-8')
    open(path, "w").write(new_xml)


if __name__=="__main__":
    en_path = join(BASE, 'values', STRINGS)
    en_words = read_english(en_path)
    for root, dirs, files in os.walk(BASE):
        if STRINGS in files:
            path = join(root, STRINGS)
            lang = root.split('/')[-1]
            if lang != 'values':
                process_lang(path, en_words)
