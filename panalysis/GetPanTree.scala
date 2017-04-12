package panalysis {

import scala.io.Source
import java.io.{BufferedWriter, OutputStreamWriter, FileOutputStream}
import scala.collection.JavaConversions._

object GetPanTree extends ActionObject {

  override val description = "Given a tree, find the clusters that associate to that node"

  ///////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val treeFile       = args(0)
    val protMapFile    = args(1)
    val clusteringFile = args(2)
    val outFile        = args(3)

    val inStream    = if(treeFile == "-"){ Source.stdin } else {  Source.fromFile(treeFile) }
    var trees       = inStream.getLines.mkString("").split(';').filter(x => x.length > 0).map(t => Newick.Tree.fromString(t + ';'))
    val protMap     = ProtMap(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)
    val taxaMap     = protMap.taxa.zipWithIndex.map{ case (t,i) => t -> i}.toMap

    val outfd = if(outFile == "-") new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"))
    outfd.write("#tree\tnodeID\tnodeName\tcoreClusterIDs\taccessoryClusterIDs\tspecificClusterIDs\n")

    trees.indices.foreach{ treeID =>
      trees(treeID).getNodes.indices.filter(nodeID => !trees(treeID).getNode(nodeID).isLeaf).foreach{ nodeID =>
        Utils.message("Processing node: %d/%s(%d)".format(treeID+1, trees(treeID).getNodeName(nodeID), nodeID))
        val characterization = clustering.getTaxaSubsetCoreAccSpecific(trees(treeID).getNode(nodeID).leaves.map(l => trees(treeID).getNodeName(l)))
        val core    : Array[Int] = characterization.filter{case (id, isc, isa, iss) => isc}.map( c => c._1)
        val acc     : Array[Int] = characterization.filter{case (id, isc, isa, iss) => isa}.map( c => c._1)
        val specific: Array[Int] = characterization.filter{case (id, isc, isa, iss) => iss}.map( c => c._1)
        outfd.write("%d\t%d\t%s\t%s\t%s\t%s\n".format(treeID, nodeID, trees(treeID).getNodeName(nodeID), core.mkString(","), acc.mkString(","), specific.mkString(",")))
      }
    }

    outfd.close()

  }

  ///////////////////////////////////////////////////////////////////////////

  override def usage() = {
    println("getPanTree <treeFile> <protMapFile> <clusteringFile> <outFile>")
    println("")
    println("  treeFile: A tree in Newick format")
    println("  protMapFile: Protein map produced e.g. by orthofinder")
    println("  clusteringFile: MCL clustering file")
    println("  outFile: output file, - for stdout")
    println("")
  }

}

}
