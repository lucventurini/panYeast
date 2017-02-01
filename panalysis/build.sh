#!/bin/bash

scala_classpath="./:./lib:./lib/tsne-2.3.0.jar"

scalac="`which scalac` -classpath $scala_classpath"
scala="`which scala` -classpath $scala_classpath"

echo "Building PANalysis..."
mkdir -p panalysis
$scalac -d ./ mainClass.scala mciReader.scala mclClust.scala protein.scala Fasta.scala utils.scala tsne.scala

echo "Constructing jar..."
cat > panalysis.mf << EOF
Main-Class: panalysis.mainClass
Class-Path: $( echo $scala_classpath | cut --complement -d: -f1 | tr ':' '\n' | sed -e 's/^.*$/ & /')
 lib/scala-library.jar 
EOF

jar -cmf panalysis.mf panalysis.jar $(find panalysis | grep -e '.*class$')
