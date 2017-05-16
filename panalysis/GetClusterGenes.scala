package panalysis {

object GetClusterGenes extends ActionObject {

  override val description = "Get the gene IDs of the clusters"

  override def main(args: Array[String]) {
    val protMapFile = args(0)
    val clustFile   = args(1)
    val outFile     = args(2)

    val protMap    = ProtMap(protMapFile)
    val clusters   = MCIReader.readClustering(clustFile)._3
    val clustering = Clustering(clusters, protMap)

    val outfd = Utils.openWrite(outFile)
    outfd.write("#clusterid\tgene members\n")
    clustering.taxaParaClusters.foreach{ c =>
      outfd.write("%d\t%s\n".format( c.id, c.cluster.map( sc => sc.mkString(",")).filter(_.length > 0).mkString(",")))
    }
    outfd.close()
    

  }

  override def usage = {
    println("GetClusterGenes <protMapFile> <clusteringFile> <outFile>")
    println("")
    println("  protMapFile: Protein map produced e.g. by orthofinder")
    println("  clusteringFile: MCL clustering file")
    println("  outFile: output file, - for stdout")
    println("")

  }

}

}
