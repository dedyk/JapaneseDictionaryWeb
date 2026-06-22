#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Niepoprawna liczba argumentow"

    exit 1
fi

./01_generate-sitemap-lastmod.sh
./02_generate-db.sh $1
./03_generate-dictionary-index.sh $1
./04_generate-sitemap.sh $1
