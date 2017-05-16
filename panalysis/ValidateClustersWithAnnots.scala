package panalysis {

import scala.io.Source
import java.io.{BufferedWriter, OutputStreamWriter, FileOutputStream}
import scala.collection.JavaConversions._

object ValidateClustersWithAnnots extends ActionObject {

  override val description = "For each cluster, check the concordance with functional annotations"

  /////////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val protMapFile    = args(0)
    val clusteringFile = args(1)
    val annotsFile     = args(2)
    val outFile        = args(3)

    val protMap     = ProtMap(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)
    val annots      = Annotations(annotsFile, protMap)

    val scores = clustering.functionalConsistencyScores(annots)

    val outfd = Utils.openWrite(outFile)

    outfd.write("#ClusterID\tnGenes\tnAnnotatedgenes\tnSpecies\tnFunctions\tscore\n")
    scores.foreach{ case (id, ng, nag, ns, nf, s) =>
      outfd.write("%d\t%d\t%d\t%d\t%d\t%f\n".format(id, ng, nag, ns, nf, s))
    } 

  }

  /////////////////////////////////////////////////////////////////////////////

  override def usage = {
    println("ValidateClustersWithAnnots <protMapFile> <clusteringFile> <annotationFile> <outFile>")
    println("")
    println("  protMapFile: Protein map produced e.g. by orthofinder")
    println("  clusteringFile: MCL clustering file")
    println("  annotationFile: A file with Protein annotations")
    println("  outFile: output file, - for stdout")
    println("")
    println(" P.S. you can modify the columns used in the annotationFile using")
    println("   --annot-idfield, --annot-protfield, and --annot-descfield.")
    println(" See the global options")

  }

}

}



