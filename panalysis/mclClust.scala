package panalysis {

class mclClust(mciFile: String, protMap:Map[Int,Protein]) {
  val (dim_gene, dim_cluster, clusters) = mciReader.readMCLClusters(mciFile, protMap)
  val paraClusters = clusters.map(clusterParalogs)

  ///////////////////////////////////////////////////////////////////////////

  def clusterParalogsID(clusterID:Int) = {
    clusterParalogs(clusters(clusterID))
  }

  def clusterParalogs(clust: Array[Protein]) = {
    clust.foldLeft(Array[Array[Protein]]()){
      (arr, currProt) => {
        if (arr.length == 0) {
          Array(Array(currProt))
        } else {
          val lastProt = arr.last.last
          if (lastProt.taxa == currProt.taxa) {
            arr.dropRight(1) :+ (arr.last :+ currProt)
          } else {
            arr :+ Array(currProt)
          }
        }
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////

}

}
