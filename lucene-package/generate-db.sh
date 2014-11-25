#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Niepoprawna liczba argumentow"

    exit 1
fi

DIR=${0%/*}

for jar in $DIR/lib/*.jar; do
    CLASSPATH=$CLASSPATH:$jar
done

echo $CLASSPATH

java -cp $CLASSPATH pl.idedyk.japanese.dictionary.lucene.LuceneDBGenerator db/word.csv db/sentences.csv db/sentences_groups.csv db/kanji.csv db/radical.csv db/names.csv true true db-lucene

mkdir $1

cp db/kana.csv db/radical.csv db/transitive_intransitive_pairs.csv db/kanji_recognizer.model.db $1
cp -r db-lucene $1/db-lucene/

rm -rf db-lucene
