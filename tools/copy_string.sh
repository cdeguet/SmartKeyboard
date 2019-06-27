#!/bin/sh

cd ../crowdin
for lang in *; do
    lang2="values-"`echo $lang | sed s/-/-r/`
    echo $lang2
    cp $lang/arrays.xml $lang/strings.xml ../app/src/main/res/$lang2
done
