#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Niepoprawna liczba argumentow"

    exit 1
fi

DIR=${0%/*}

for jar in $DIR/lib/*.jar; do
    CLASSPATH=$CLASSPATH:$jar
done

CLASSPATH=$CLASSPATH:../target/classes
CLASSPATH=$CLASSPATH:$HOME/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar

echo $CLASSPATH

java -cp $CLASSPATH pl.idedyk.japanese.dictionary.web.sitemap.SitemapGenerator $1
