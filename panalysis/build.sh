#!/bin/bash

scala_classpath="./:./lib"

scalac="`which scalac` -classpath $scala_classpath"
scala="`which scala` -classpath $scala_classpath"

echo "Building panalysis..."
mkdir -p panalysis
$scalac -d ./ mainClass.scala mciReader.scala mclClust.scala protein.scala Fasta.scala utils.scala

echo "Constructing jar..."
cat > panalysis.mf << EOF
Main-Class: panalysis.mainClass
Class-Path: $( echo $scala_classpath | cut --complement -d: -f1 | tr ':' '\n' | sed -e 's/^.*$/ & /')
 lib/scala-library.jar 
EOF

jar -cmf panalysis.mf panalysis.jar $(find panalysis | grep -e '.*class$')
