#!/bin/bash

mvn compile

export CURDIR=`pwd`

export CLASSPATH=$CURDIR/src/main/webapp/WEB-INF/lib/JapaneseDictionaryAPI-20140308.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/net/sourceforge/javacsv/javacsv/2.1/javacsv-2.1.jar:$CLASSPATH

export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-core/4.7.0/lucene-core-4.7.0.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-analyzers-common/4.7.0/lucene-analyzers-common-4.7.0.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-queries/4.7.0/lucene-queries-4.7.0.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-queryparser/4.7.0/lucene-queryparser-4.7.0.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/org/apache/lucene/lucene-sandbox/4.7.0/lucene-sandbox-4.7.0.jar:$CLASSPATH

export CLASSPATH=target/classes:$CLASSPATH

java -cp $CLASSPATH pl.idedyk.japanese.dictionary.web.dictionary.lucene.LuceneDBGenerator

cp db/kana.csv db/radical.csv db/transitive_intransitive_pairs.csv src/main/resources/db
cp -r db-lucene src/main/resources/db

rm -rf bin-db-generator
rm -rf db-lucene
