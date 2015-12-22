#!/usr/bin/env bash

if [ -z $1 ] || [ -z $2 ] || [ -z $3 ] || [ -z $4 ]; then
    echo "Usage: $0 in_dir out_dir type suffix"
    exit
fi

for f in $1/*.png; do
    png_name=`basename ${f}`
    svg_name=${png_name%.png}$4.svg
    svg_file=$2/${svg_name}
    if [ ! -e ${svg_file} ]; then
        echo "Convert ${png_name} to ${svg_name}"
        convert ${f} ${svg_file}
    fi
done