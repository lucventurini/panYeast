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

    val scores = clustering.intClusters.map{ c =>
      val nGenes = c.cluster.length.toFloat
      val annotatedGenes = c.cluster.filter( annots.ia contains _)
      val clusterAnnotations = annotatedGenes.map(annots.ia).flatten.groupBy(identity).mapValues(_.size)
      Debug.message("Cluster %d: %d".format(c.id, c.cluster.length))
      Debug.message(clusterAnnotations.map{ case (k,v) => "%s: %d".format(k.toString, v)}.mkString("\n"))
      val totalAnnots = clusterAnnotations.size.toFloat
      val score = clusterAnnotations.map{ case (annot,counts) =>
         (counts.toFloat / nGenes)
      }.foldLeft(0.0)(math.max(_, _))

      (c.id, nGenes.toInt, annotatedGenes.size, c.toProtein(protMap).cluster.map(p => p.taxa).distinct.length, score)
    }

    val outfd = if(outFile == "-") new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"))

    outfd.write("#ClusterID\tnGenes\tnAnnotatedgenes\tnSpecies\tscore\n")
    scores.foreach{ case (id, ng, nag, ns, s) =>
      outfd.write("%d\t%d\t%d\t%d\t%f".format(id, ng, nag, ns, s))
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



