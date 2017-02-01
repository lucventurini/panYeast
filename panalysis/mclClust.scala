package panalysis {

class mclClust(mciFile: String, protMap:Array[Protein]) {
  val (dim_gene, dim_cluster, clusters) = mciReader.readMCLClusters(mciFile, protMap)
  val paraClusters = clusters.map(clusterParalogs)
  val panGenome    = sortByClusterSizes
  val taxa         = Utils.protMapTaxa(protMap)

  ///////////////////////////////////////////////////////////////////////////

  def clusterParalogsID(clusterID:Int) = {
    clusterParalogs(clusters(clusterID))
  }

  ///////////////////////////////////////////////////////////////////////////

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

  def sortByClusterSizes() = {
    (0 to paraClusters.length-1).sortWith{ (c1, c2) => paraClusters(c1).length < paraClusters(c2).length }
  }

  ///////////////////////////////////////////////////////////////////////////

  def getCore() = {
    val nTaxa = taxa.length
    panGenome.filter( index => (paraClusters(index).length == nTaxa) )
  }

  ///////////////////////////////////////////////////////////////////////////

  def getCoreLabels() = {
    val nTaxa = taxa.length
    println(nTaxa)
    println(taxa)
    paraClusters.map{ c =>
      if (c.length == nTaxa)  1 else 0
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  def getCountLabels() = {
    paraClusters.map( x => x.length)
  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneMatrixBinary() = {
    tsneMatrix( (t,clusterTaxaCounts) => if (clusterTaxaCounts contains t) 1 else 0)
  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneMatrixParalogCounts() = {
    tsneMatrix( (t,clusterTaxaCounts) => if (clusterTaxaCounts contains t) clusterTaxaCounts(t) else 0)
  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneMatrix(fn : (String, Map[String,Int]) => Int) = {
    println("%d,%d".format(taxa.length, clusters.length))
    var matrix = Array.ofDim[Double](clusters.length,taxa.length)
    clusters.zipWithIndex.foreach{ case (cluster,i) =>
      val clusterTaxaCounts = cluster.map(p => p.taxa).groupBy(identity).mapValues(_.size)
      taxa.zipWithIndex.foreach{ case (t,j) => matrix(i)(j) = fn(t, clusterTaxaCounts)}
    }
    matrix
  }

  ///////////////////////////////////////////////////////////////////////////


  ///////////////////////////////////////////////////////////////////////////
}

}
