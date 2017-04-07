package panalysis {

import scala.io.Source
import java.io.{BufferedWriter, OutputStreamWriter, FileOutputStream}
import scala.collection.JavaConversions._

object CmpClust extends ActionObject {

  override val description = "Compare two clusterings with the F-measure"

  override def main(args: Array[String]) = {

    Utils.setParallelismGlobally(5)

    val actions = Map( "clust"     -> actionClust _,
                       "paraClust" -> actionParaClust _,
                       "tree"      -> actionTree _).map{ case (x,y) => (x.toLowerCase, y)}

    if (args.length < 1 || args(0).toLowerCase == "help" || !(actions contains args(0).toLowerCase)) {
      usage
    } else {
      actions(args(0))(args.slice(1, args.length))
    }

  }

  def actionClust(args: Array[String]) = {

    val clusteringFile1 = args(0)
    val protMapFile1    = args(1)
    val clusteringFile2 = args(2)
    val protMapFile2    = if (args.length > 3) args(3) else ""

    val protMap1     = ProtMap(protMapFile1)
    val protMap2     = if (protMapFile2 == "") protMap1 else ProtMap(protMapFile2, protMap1.taxa)
    val intClusters1 = MCIReader.readClustering(clusteringFile1)._3
    val intClusters2 = MCIReader.readClustering(clusteringFile2)._3
    val clustering1  = Clustering(intClusters1, protMap1)
    val clustering2  = Clustering(intClusters2, protMap2)

    val f = clustering1.cmpClust(clustering2)
    println("F:" + f)
  }

  def actionParaClust(args: Array[String]) = { 

    val clusteringFile1 = args(0)
    val protMapFile1    = args(1)
    val clusteringFile2 = args(2)
    val protMapFile2    = if (args.length > 3) args(3) else ""

    val protMap1     = ProtMap(protMapFile1)
    val protMap2     = if (protMapFile2 == "") protMap1 else ProtMap(protMapFile2, protMap1.taxa)
    val intClusters1 = MCIReader.readClustering(clusteringFile1)._3
    val intClusters2 = MCIReader.readClustering(clusteringFile2)._3
    val clustering1  = Clustering(intClusters1, protMap1)
    val clustering2  = Clustering(intClusters2, protMap2)

    val f = clustering1.cmpParaClust(clustering2)
    println("F:" + f )
  }

  def actionTree(args: Array[String]) = {

    println("Not implemented yet:0.0")

  }

  override def usage = {

    println("cmpClust <action> <options>")

    println(" action: clust|paraclust|tree")
    println("")
    println(" clust <clusteringFile1> <protMapFile1> <clusteringFile2> [protMapFile2]")
    println(" paraclust <clusteringFile1> <protMapFile1> <clusteringFile2> [protMapFile2]")
    println(" tree <treeFile> <clusteringFile1> <protMapFile1> <clusteringFile2> [protMapFile2]")

    println("")
    println(" clusteringFile1: Output from MCL")
    println(" protMapFile1: Protein map for IDs in MCL")
    println(" clusteringFile2: Second clustering")
    println(" protMapFile2: If different from ProtMapFile1")
    println(" treeFile: A newick tree formatted file")
    println("")


  }


}

}
