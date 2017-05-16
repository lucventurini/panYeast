package panalysis {

object AddPanToTree extends ActionObject {

  override val description = "Add Pan genome information to a tree"

  ///////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val treeFile       = args(0)
    val protMapFile    = args(1)
    val clusteringFile = args(2)
    val outFile        = args(3)

    var trees       = Newick.readFile(treeFile)
    val protMap     = ProtMap(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)
    val taxaMap     = protMap.taxa.zipWithIndex.map{ case (t,i) => t -> i}.toMap

    trees.indices.foreach{ treeID =>
      val rootLeaves = trees(treeID).leaves
      Debug.message("Processing tree %d".format(treeID))
      trees(treeID).getNodes.indices.foreach{ nodeID =>
        Utils.message("\rProcessing node: %d/%d  ".format(nodeID, trees(treeID).getNodes.length), ln=false)
        val characterization = clustering.getTaxaSubsetCoreAccSpecific(trees(treeID).getNode(nodeID).leaves.map(l => trees(treeID).getNodeName(l)))
        val n_core     = characterization.filter{case (id, isc, isa, iss) => isc}.length
        val n_acc      = characterization.filter{case (id, isc, isa, iss) => isa}.length
        val n_specific = characterization.filter{case (id, isc, isa, iss) => iss}.length
        trees(treeID).addNodeAnnot(nodeID, "core", n_core.toString)
        trees(treeID).addNodeAnnot(nodeID, "acc", n_acc.toString)
        trees(treeID).addNodeAnnot(nodeID, "sp", n_specific.toString)
      }
      Utils.message("")
    }

    val outfd = Utils.openWrite(outFile)

    trees.foreach{ t =>
      outfd.write("\n%s\n".format(t.toNewick))
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
