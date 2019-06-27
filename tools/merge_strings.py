#!/usr/bin/env python

import os
from os.path import join
from lxml import etree

CROWDIN = '../crowdin'
BASE = '../common/src/main/res'
STRINGS = 'strings.xml'

setup_names = set([
"setup_welcome_title",
"setup_welcome_additional_description",
"setup_start_action",
"setup_next_action",
"setup_steps_title",
"setup_step1_title",
"setup_step1_instruction",
"setup_step1_finished_instruction",
"setup_step1_action",
"setup_step2_title",
"setup_step2_instruction",
"setup_step2_action",
"setup_step3_title",
"setup_step3_instruction",
"setup_step3_action",
"setup_finish_action",
"show_setup_wizard_icon",
"show_setup_wizard_icon_summary"
])

lang_words = {}

def process_lang(lang, words, path):
    tree = etree.parse(path)
    for child in tree.getroot():
        if child.tag == 'string':
            name = child.get('name')
            value = child.text
            crowdin_value = words.get(name, None)
            if name not in setup_names and crowdin_value and crowdin_value != value:
                #print lang, name, value, crowdin_value
                child.text = crowdin_value
        new_xml = etree.tostring(tree.getroot(), xml_declaration=True, encoding='utf-8')
    print "write ", path
    open(path, "w").write(new_xml)


lang_map = {
    'values-id': 'values-in',
    'values-he': 'values-iw'
}

for root, dirs, files in os.walk(CROWDIN):
    if STRINGS in files:
        lang = root.split('/')[-1].split('-')
        lang = 'values-' + '-r'.join(lang)
        if lang in lang_map:
            lang = lang_map[lang]
        words = lang_words[lang] = {}
        #print lang
        path = join(root, STRINGS)
        tree = etree.parse(path)
        for child in tree.getroot():
            if child.tag == 'string':
                name = child.get('name')
                value = child.text
                words[name] = value

print lang_words.keys()

for root, dirs, files in os.walk(BASE):
    if STRINGS in files:
        path = join(root, STRINGS)
        lang = root.split('/')[-1]
        if lang not in lang_words:
            print lang + ' not translated'
        else:
            process_lang(lang, lang_words[lang], path)
