#!/bin/bash

BASE_DIR=lucene-package

mkdir $BASE_DIR/lib

export JAPANESE_DICTIONARY_API_VERSION=1.0-SNAPSHOT
export JAPANESE_DICTIONARY_LUCENE_COMMON_VERSION=1.0-SNAPSHOT
export LUCENE_VERSION=4.7.2

cp $HOME/.m2/repository/pl/idedyk/japanese/JapaneseDictionaryAPI/$JAPANESE_DICTIONARY_API_VERSION/JapaneseDictionaryAPI-$JAPANESE_DICTIONARY_API_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/pl/idedyk/japanese/JapaneseDictionaryLuceneCommon/$JAPANESE_DICTIONARY_LUCENE_COMMON_VERSION/JapaneseDictionaryLuceneCommon-$JAPANESE_DICTIONARY_LUCENE_COMMON_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/net/sourceforge/javacsv/javacsv/2.1/javacsv-2.1.jar $BASE_DIR/lib

cp $HOME/.m2/repository/org/apache/lucene/lucene-core/$LUCENE_VERSION/lucene-core-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/lucene/lucene-analyzers-common/$LUCENE_VERSION/lucene-analyzers-common-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/lucene/lucene-queries/$LUCENE_VERSION/lucene-queries-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/lucene/lucene-queryparser/$LUCENE_VERSION/lucene-queryparser-$LUCENE_VERSION.jar $BASE_DIR/lib
cp $HOME/.m2/repository/org/apache/lucene/lucene-sandbox/$LUCENE_VERSION/lucene-sandbox-$LUCENE_VERSION.jar $BASE_DIR/lib

cp -r db $BASE_DIR/db