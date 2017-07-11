package panalysis {

import scala.io.Source
import java.io.{BufferedWriter, OutputStreamWriter, FileOutputStream}
import scala.collection.JavaConversions._

object PanCoreEnrichments extends ActionObject {

  override val description = "For each node in a tree, check if the specific gene clusters are enriched for some functions"

  override def main(args: Array[String]) = {
    val protMapFile    = args(0)
    val clusteringFile = args(1)
    val annotFile      = args(2)
    val treeFile       = args(3)
    val outFile        = args(4)

    val protMap     = ProtMap(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)
    val annots      = Annotations(annotFile, protMap)
    val tree        = Newick.readFile(treeFile)(0)

      // ClusterIDs of clusters with annotations
    val backgroundClusters = clustering.clusters.zipWithIndex.filter{ case (c, cid) =>
      c.cluster.filter( annots.pa contains _.toString).map( p=> annots.pa(p.toString)).flatten.length > 0
    }.map(_._2).toSet

      // Map[AnnotationEntry,Array[ClusterIDs]]
    val clustersPerAnnotation = clustering.clusters.zipWithIndex.map{ case (c, cid) =>
      c.cluster.map(_.toString).filter( annots.pa contains _).map(annots.pa).flatten.distinct.map(a => (cid, a))
    }.flatten.groupBy(_._2).mapValues( v => v.map(_._1).toSet)

    val groups = tree.getNodes.indices.map{ nodeID =>
      val characterization = clustering.getTaxaSubsetCoreAccSpecific(tree.getNodeLeafNames(nodeID))
      val core     = characterization.filter{case (id, isc, isa, iss) => isc}.map(_._1)
      val acc      = characterization.filter{case (id, isc, isa, iss) => isa}.map(_._1)
      val specific = characterization.filter{case (id, isc, isa, iss) => iss}.map(_._1)

      Array((nodeID, "core", core), (nodeID, "acc", acc), (nodeID, "specific", specific))
    }.flatten

    //val enrichments = clustering.featureSpecificNode(tree).zipWithIndex.groupBy(_._1).map{ case (k, v) => (k,v.map(_._2))}.filter{ case (k,v) => k >= 0}.map{ case (nodeID, clusterIDs) =>

    val enrichments = groups.map{ case (nodeID, groupType, clusterIDs) =>

      val clusterAnnotations = clusterIDs.map(i => clustering.clusters(i).cluster).flatten.filter( annots.pa contains _.toString).map( p=> annots.pa(p.toString)).flatten.distinct
      val foregroundClusters = clusterIDs.toSet

      val tests = clusterAnnotations.map{ a =>
        val annotatedClusters = clustersPerAnnotation(a)
        val stats = Statistics.enrich(backgroundClusters, foregroundClusters, annotatedClusters)
        (a, stats)
      }

      (nodeID,  groupType, tests)
    }.map{ case (nodeID, groupType, tests) => tests.map{ case (annot, stats) => (nodeID, groupType, annot.id, annot.description, stats._1, stats._2, stats._3, stats._4, stats._6) } }.flatten.toArray

    val nodeIDs = enrichments.map(_._1)
    val groupTypes = enrichments.map(_._2)
    val annotIDs = enrichments.map(_._3)
    val annotDescriptions = enrichments.map(_._4)
    val a = enrichments.map(_._5)
    val b = enrichments.map(_._6)
    val c = enrichments.map(_._7)
    val d = enrichments.map(_._8)
    val pvals = enrichments.map(_._9)
    val qvals = Statistics.fdr_bh(pvals)

    val outfd = Utils.openWrite(outFile)
    outfd.write("#nodeID\tnodeName\tgroupType\tannotID\tannotDescription\ta\tb\tc\td\tpval\tqval\n")
    nodeIDs.indices.filter(i => qvals(i) < Statistics.alpha).foreach{ i =>
      outfd.write("%d\t%s\t%s\t%s\t%s\t%d\t%d\t%d\t%d\t%f\t%f\n".format(nodeIDs(i), tree.getNodeName(nodeIDs(i)), groupTypes(i), annotIDs(i), annotDescriptions(i), a(i), b(i), c(i), d(i), pvals(i), qvals(i)))
    }
    outfd.close()
  }

  override def usage = {
    println("PanCoreEnrichments <protMapFile> <clusteringFile> <annotationFile> <treeFile> <outFile>")
    println("")
    println("  protMapFile: Protein map produced e.g. by orthofinder")
    println("  clusteringFile: MCL clustering file")
    println("  annotationFile: A file with Protein annotations")
    println("  treeFile: A tree in Newick format")
    println("  outFile: output file, - for stdout")
    println("")
    println(" P.S. you can modify the columns used in the annotationFile using")
    println("   --annot-idfield, --annot-protfield, and --annot-descfield.")
    println(" See the global options")
  }

}

}
