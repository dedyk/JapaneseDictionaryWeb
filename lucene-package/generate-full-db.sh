#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Niepoprawna liczba argumentow"

    exit 1
fi

./generate-db.sh $1
./generate-sitemap.sh $1
