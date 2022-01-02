#!/bin/bash

BASE_DIR=lucene-package

mkdir $BASE_DIR/lib

export JAPANESE_DICTIONARY_API_VERSION=1.0-SNAPSHOT
export JAPANESE_DICTIONARY_LUCENE_COMMON_VERSION=1.0-SNAPSHOT
export LUCENE_VERSION=4.7.2
export GSON_VERSION=2.8.5
export LOG4J_VERSION=2.17.1

cp $HOME/.m2/repository/pl/idedyk/japanese/JapaneseDictionaryAPI/$JAPANESE_DICTIONARY_API_VERSION/JapaneseDictionaryAPI-$JAPANESE_DICTIONARY_API_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/pl/idedyk/japanese/JapaneseDictionaryLuceneCommon/$JAPANESE_DICTIONARY_LUCENE_COMMON_VERSION/JapaneseDictionaryLuceneCommon-$JAPANESE_DICTIONARY_LUCENE_COMMON_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/net/sourceforge/javacsv/javacsv/2.1/javacsv-2.1.jar $BASE_DIR/lib

cp $HOME/.m2/repository/org/apache/lucene/lucene-core/$LUCENE_VERSION/lucene-core-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/lucene/lucene-analyzers-common/$LUCENE_VERSION/lucene-analyzers-common-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/lucene/lucene-queries/$LUCENE_VERSION/lucene-queries-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/lucene/lucene-queryparser/$LUCENE_VERSION/lucene-queryparser-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/lucene/lucene-sandbox/$LUCENE_VERSION/lucene-sandbox-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/lucene/lucene-suggest/$LUCENE_VERSION/lucene-suggest-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/com/google/code/gson/gson/$GSON_VERSION/gson-$GSON_VERSION.jar $BASE_DIR/lib

cp $HOME/.m2/repository/org/apache/logging/log4j/log4j-core/$LOG4J_VERSION/log4j-core-$LOG4J_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/logging/log4j/log4j-api/$LOG4J_VERSION/log4j-api-$LOG4J_VERSION.jar $BASE_DIR/lib

cp -r db $BASE_DIR/db
