#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Niepoprawna liczba argumentow"

    exit 1
fi

#7z a $1.7z $1
tar -cvf $1.tar $1/
