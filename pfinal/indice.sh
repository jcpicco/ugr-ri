#!/bin/bash

if [[ $1 = 'c' ]]
then
    javac -cp ./lib/commons-beanutils-1.9.4.jar:./lib/commons-collections4-4.4.jar:./lib/commons-lang3-3.12.0.jar:./lib/commons-text-1.9.jar:./lib/opencsv-5.5.2.jar:./lucene-8.10.1/core/lucene-core-8.10.1.jar:./lucene-8.10.1/analysis/common/lucene-analyzers-common-8.10.1.jar:./lucene-8.10.1/facet/lucene-facet-8.10.1.jar indiceSimple.java
elif [[ $1 = 'e' ]]
then
    java -cp .:./lib/commons-beanutils-1.9.4.jar:./lib/commons-collections4-4.4.jar:./lib/commons-lang3-3.12.0.jar:./lib/commons-text-1.9.jar:./lib/opencsv-5.5.2.jar:./lucene-8.10.1/core/lucene-core-8.10.1.jar:./lucene-8.10.1/analysis/common/lucene-analyzers-common-8.10.1.jar:./lucene-8.10.1/facet/lucene-facet-8.10.1.jar indiceSimple.java
else
    echo "Especifica un argumento válido (c, e)\n"
fi