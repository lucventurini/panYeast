#!/bin/bash

scala_classpath="./:./lib:./lib/tsne-2.3.0.jar:./lib/breeze-0.13.jar:./lib/scala-parser-combinators_2.12-1.0.4.jar:./lib/jebl.jar"

scalac="`which scalac` -classpath $scala_classpath -deprecation"
scala="`which scala` -classpath $scala_classpath"

echo "Building PANalysis..."
mkdir -p panalysis
$scalac -d ./ ActionObject.scala Clustering.scala ClusterTypes.scala ProtMap.scala ProtMapCheck.scala Fasta.scala GetClusterFastas.scala mainClass.scala MCINetwork.scala MCIReader.scala Protein.scala ResolveParalogs.scala Test.scala TSNE.scala Utils.scala ActionTemplate.scala

echo "Constructing jar..."
cat > panalysis.mf << EOF
Main-Class: panalysis.mainClass
Class-Path: $( echo $scala_classpath | cut --complement -d: -f1 | tr ':' '\n' | sed -e 's/^.*$/ & /')
 lib/scala-library.jar 
EOF

jar -cmf panalysis.mf panalysis.jar $(find panalysis | grep -e '.*class$')
