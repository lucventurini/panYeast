package panalysis {

object GetClusterGenes extends ActionObject {

  override val description = "Get the gene IDs of the clusters"

  override def main(args: Array[String]) {
    val protMapFile = args(0)
    val clustFile   = args(1)
    val outFile     = args(2)
    val flatFlag    = if (args.length > 3) true else false

    val protMap    = ProtMap(protMapFile)
    val clusters   = MCIReader.readClustering(clustFile)._3
    val clustering = Clustering(clusters, protMap)

    val outfd = Utils.openWrite(outFile)
    outfd.write("#clusterid\tgene member\n")
    clustering.taxaParaClusters.foreach{ c =>
      if (!flatFlag) {
        outfd.write("%d\t%s\n".format( c.id, c.cluster.filter(_.length > 0).map( sc => sc.mkString(",")).mkString(",")))
      } else {
        outfd.write("%s\n".format( c.cluster.flatten.map(p => "%d\t%s".format(c.id, p.toString)).mkString("\n")))
      }
    }
    outfd.close()
    

  }

  override def usage = {
    println("GetClusterGenes <protMapFile> <clusteringFile> <outFile> [flatFlag]")
    println("")
    println("  protMapFile: Protein map produced e.g. by orthofinder")
    println("  clusteringFile: MCL clustering file")
    println("  outFile: output file, - for stdout")
    println("  flatFlag: If present, print each protein on its own line")
    println("")

  }

}

}
