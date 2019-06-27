#!/usr/bin/env python
# -*- coding: utf-8 -*-

SYMBOLS = [
    ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0", None],
    ["\\@", "\\#", "$", "%", "&amp;", "*", "-", "+", "(", ")", None],
    ["!", "&quot;", "'", ":", ";", "/", "\\?", "[", "]", None],
    [None, None, None, None, None, None, None, None, None]
]

BG_BDS = [
u"уеишщксдзцб",
u"ьяаожгтнвмч",
u"юйъэфхпрл"
]

AR_FULL = [
u"Էթփձջրչճժծ",
u"քոեռտըւիօպ",
u"ասդֆգհյկլխ",
u"զղցվբնմշ"
]

COLEMAK = [
"qwfpgjluy;",
"arstdhneio",
"zxcvbkm"
]

def gen_layout(layout):
    out = ""
    for i, row in enumerate(layout):
        out += "\t<Row>\n"
        for j, key in enumerate(row):
            codes = [ord(key)]
            label = key
            alt_label = SYMBOLS[i][j]
            out += '\t\t<Key android:codes="%s" android:keyLabel="%s"' % (
                ",".join(map(str ,codes)), label)
            if alt_label:
                out += ' altLabel="%s" android:popupCharacters="%s"' % (
                    alt_label, alt_label)
            if j == 0:
                out += ' android:keyEdgeFlags="left"'
            if j == len(row) - 1:
                out += ' android:keyEdgeFlags="right"'
            out += "/>\n"
        out += "\t</Row>\n"
    return out


print gen_layout(COLEMAK)