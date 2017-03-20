package panalysis {

import java.io._

object GetClusterFastas extends ActionObject {

  override val description = "Given a fasta file and a cluster file, extract one multiFasta file for each cluster"

  override def main(args: Array[String]) = {
    val action      = args(0)
    val fastaFile   = args(1)
    val protMapFile = args(2)
    val clustFile   = args(3)
    val outPrefix   = args(4)

    val fastaMap   = Fasta.readMap(fastaFile)
    val protMap    = ProtMap.read(protMapFile)
    val clusters   = MCIReader.readClustering(clustFile)._3
    val clustering = new Clustering(clusters, protMap)

    def selectAction(action: String): Array[ClusterTypes.ProteinParaCluster] = action.toLowerCase match {
      case "core"           => clustering.taxaParaClusters.filter( pc => pc.isCore(clustering.taxa.length) )
      case "singlecopy"     => clustering.taxaParaClusters.filter( pc => pc.isSingleCopy )
      case "singlecopycore" => clustering.taxaParaClusters.filter( pc => pc.isSingleCopy & pc.isCore(clustering.taxa.length) )
    }

    val listfd = new PrintWriter(new FileWriter("%s.list.tsv".format(outPrefix), false))
    
    selectAction(action).map{ pc =>
      (pc.id, pc.cluster.flatten.map(p => fastaMap(p.toString)))
    }.foreach{ case (id, farray) =>
      val fastaOutName = "%s.%d.fasta".format(outPrefix,id)
      listfd.write("%d\t%d\t%s\n".format(id, farray.length, fastaOutName))
      Fasta.write(farray.toList, fastaOutName)
    }
    listfd.close()

  }

  /////////////////////////////////////////////////////////////////////////////

  override def usage = {
    println("clusterFastas.scala: <action> <fastaFile> <protmapFile> <clustfile> <outprefix>")
    println("")
    println("  action:     core -> Returns the FASTA sequences for only the core genes (present in all genomes")
    println("              singleCopy -> Returns single copy genes in FASTA format")
    println("              singleCopyCore -> Intesection of above")
    println(" fastaFile:   FASTA file format of all sequences in set")
    println(" protMapFile: Protein map")
    println(" clustFile:   Output from MCL")
    println(" outPrefix:   Prefix of output, returns <outPrefix>.list.tsv with list of all files")
  }

}

}
