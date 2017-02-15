package panalysis {

object GetClusterFastas extends ActionObject {

  override val description = "Given a fasta file and a cluster file, extract one multiFasta file for each cluster"

  override def main(args: Array[String]) = {
    val action      = args(0)
    val fastaFile   = args(1)
    val protMapFile = args(2)
    val clustFile   = args(3)
    val outPrefix   = args(4)

    println(action)
    println(fastaFile)
    println(protMapFile)
    println(protMapFile)
    println(clustFile)
    println(outPrefix)

    println(args.zipWithIndex.map{ case (a, i) => "%d: %s".format(i, a)}.mkString("\n"))

    val fastaMap   = Fasta.readMap(fastaFile)
    val protMap    = Utils.readProtMapFile(protMapFile)
    val clusters   = MCIReader.readClustering(clustFile)._3
    val clustering = new Clustering(clusters, protMap)

    def selectAction(action: String): Array[ClusterTypes.ProteinParaCluster] = action.toLowerCase match {
      case "core"           => clustering.taxaParaClusters.filter( pc => pc.isCore(clustering.taxa.length) )
      case "singlecopy"     => clustering.taxaParaClusters.filter( pc => pc.isSingleCopy )
      case "singlecopycore" => clustering.taxaParaClusters.filter( pc => pc.isSingleCopy & pc.isCore(clustering.taxa.length) )
    }

    selectAction(action).map{ pc =>
      (pc.id, pc.cluster.flatten.map(p => fastaMap(p.toString)))
    }.foreach{ case (id, farray) =>
      Fasta.write(farray.toList, "%s%d.fasta".format(outPrefix,id))
    }

  }

  /////////////////////////////////////////////////////////////////////////////

  override def usage = {
    println("clusterFastas.scala")
  }

}

}
