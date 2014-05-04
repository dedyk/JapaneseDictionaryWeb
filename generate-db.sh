#!/bin/bash

mvn compile

export CURDIR=`pwd`

export JAPANESE_DICTIONARY_API_VERSION=1.0-SNAPSHOT
export JAPANESE_DICTIONARY_LUCENE_COMMON_VERSION=1.0-SNAPSHOT
export LUCENE_VERSION=4.7.2

export CLASSPATH=$HOME/.m2/repository/pl/idedyk/japanese/JapaneseDictionaryAPI/$JAPANESE_DICTIONARY_API_VERSION/JapaneseDictionaryAPI-$JAPANESE_DICTIONARY_API_VERSION.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/pl/idedyk/japanese/JapaneseDictionaryLuceneCommon/$JAPANESE_DICTIONARY_LUCENE_COMMON_VERSION/JapaneseDictionaryLuceneCommon-$JAPANESE_DICTIONARY_LUCENE_COMMON_VERSION.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/net/sourceforge/javacsv/javacsv/2.1/javacsv-2.1.jar:$CLASSPATH

export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-core/$LUCENE_VERSION/lucene-core-$LUCENE_VERSION.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-analyzers-common/$LUCENE_VERSION/lucene-analyzers-common-$LUCENE_VERSION.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-queries/$LUCENE_VERSION/lucene-queries-$LUCENE_VERSION.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-queryparser/$LUCENE_VERSION/lucene-queryparser-$LUCENE_VERSION.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-sandbox/$LUCENE_VERSION/lucene-sandbox-$LUCENE_VERSION.jar:$CLASSPATH

export CLASSPATH=target/classes:$CLASSPATH

java -cp $CLASSPATH pl.idedyk.japanese.dictionary.lucene.LuceneDBGenerator db/word.csv db/kanji.csv db/radical.csv true db-lucene

cp db/kana.csv db/radical.csv db/transitive_intransitive_pairs.csv db/kanji_recognizer.model.db src/main/resources/db
cp -r db-lucene src/main/resources/db/db-lucene/

rm -rf bin-db-generator
rm -rf db-lucene
