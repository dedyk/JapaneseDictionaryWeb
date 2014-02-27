#!/bin/bash

export CURDIR=`pwd`

mkdir -p bin-db-generator
cd src-db-generator

export CLASSPATH=$CURDIR/src/main/webapp/WEB-INF/lib/JapaneseDictionaryAPI-20140227.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/net/sourceforge/javacsv/javacsv/2.1/javacsv-2.1.jar:$CLASSPATH
export CLASSPATH=$HOME/.m2/repository/org/xerial/sqlite-jdbc/3.7.2/sqlite-jdbc-3.7.2.jar:$CLASSPATH

javac -classpath $CLASSPATH pl/idedyk/japanese/dictionary/web/dbgenerator/DBGenerator.java -d ../bin-db-generator

cd ..

java -cp bin-db-generator:$CLASSPATH pl.idedyk.japanese.dictionary.web.dbgenerator.DBGenerator

cp db/kana.csv db/radical.csv db/transitive_intransitive_pairs.csv db/dictionary.db src/main/resources/db

rm -f db/dictionary.db
rm -rf bin-db-generator
