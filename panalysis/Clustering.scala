package panalysis {

class Clustering(val intClusters: Array[ClusterTypes.IntCluster],
                 val protMap: ProtMap) {

  val taxa             = protMap.taxa
  val clusters         = intClusters.map(c => c.toProtein(protMap))
  val paraClusters     = clusters.map(_.toParaCluster)
  val taxaParaClusters = paraClusters.map(_.indexByTaxa(taxa))
  val panGenome        = sortByClusterSizes
  val taxaMap          = taxa.zipWithIndex.map{ case (t,i) => t -> i}.toMap
  val protClustMap     = clusters.indices.map{ c => clusters(c).cluster.map{ p => p -> c}}.flatten.toMap
  val nprots           = intClusters.map(c => c.cluster.length).foldLeft(0){case (a,b) => a+b}

  ///////////////////////////////////////////////////////////////////////////

  def sortByClusterSizes() = {
    (0 to paraClusters.length-1).sortWith{ (c1, c2) => paraClusters(c1).cluster.length < paraClusters(c2).cluster.length }
  }

  ///////////////////////////////////////////////////////////////////////////

  def getCore() = {
    val nTaxa = taxa.length
    this.taxaParaClusters.filter(_.isCore(nTaxa))
  }

  ///////////////////////////////////////////////////////////////////////////

  def getTaxaSubsetCoreAccSpecific(taxaSubset: Array[String]): Array[(Int,Boolean,Boolean,Boolean)] = {

    val taxaIndices    = taxaSubset.map(this.taxaMap)
    val notTaxaIndices = this.taxa.indices.filter(i => !(taxaIndices contains i)).toArray
    
    this.taxaParaClusters.map{ pc => 
      val percentTaxa    = pc.nSubsetTaxa(taxaIndices).toDouble / taxaIndices.length.toDouble
      val percentNotTaxa = pc.nSubsetTaxa(notTaxaIndices).toDouble / notTaxaIndices.length.toDouble
      (pc.id, Clustering.coreRange.isBetween(percentTaxa), Clustering.accRange.isBetween(percentTaxa), percentTaxa >= Clustering.specific && percentNotTaxa <= (1.0 - Clustering.specific))
    }

  }

  ///////////////////////////////////////////////////////////////////////////
  //
  def getSingleCopyLabels() = {
    taxaParaClusters.map(_.isSingleCopy).map ( if(_) 1 else 0)
  }

  ///////////////////////////////////////////////////////////////////////////

  def getCoreLabels() = {
    val nTaxa = taxa.length
    taxaParaClusters.map(x => if (x.isCore(nTaxa)) 1.0 else 0.0)
    
  }

  ///////////////////////////////////////////////////////////////////////////

  def getCountLabels() = {
    taxaParaClusters.map( c => c.cluster.length)
  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneMatrixBinary() = {
    tsneMatrix( (pc) => if (pc.length > 0) 1 else 0)
  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneMatrixParalogCounts() = {
    tsneMatrix( (pc) => pc.length)
  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneMatrix(fn : (Array[Protein]) => Int) = {
    Debug.message("%d,%d".format(taxa.length, clusters.length))
    var matrix = Array.ofDim[Double](clusters.length,taxa.length)
    taxaParaClusters.foreach{ pc =>
      pc.cluster.zipWithIndex.foreach{ case (c, i) =>
        matrix(pc.id)(i) = fn(c)
      }
    }
    matrix
  }

  ///////////////////////////////////////////////////////////////////////////

  def cmpClust(c2: Clustering) = {

    this.clusters.map{ c =>
      val possibleClusters = c.cluster.map( p => c2.protClustMap(p)).distinct map c2.clusters
      possibleClusters.map( aC => c.fMeasureComponent(aC)).foldLeft(-1.0){case (a,b) => math.max(a,b)} *  c.cluster.length.toDouble
    }.foldLeft(0.toDouble){case (a,b) => a+b} / this.nprots.toDouble

  }

  ///////////////////////////////////////////////////////////////////////////

  def cmpParaClust(c2: Clustering) = {
    this.taxaParaClusters.zipWithIndex.map{case (c,i) =>
      val possibleClusters = this.clusters(i).cluster.map(p => c2.protClustMap(p)).distinct map c2.taxaParaClusters
      possibleClusters.map( aC => c.fMeasureComponent(aC)).foldLeft(-1.0){case (a,b) => math.max(a,b)} *  this.clusters(i).cluster.length
    }.foldLeft(0.toDouble){case (a,b) => a+b} / this.nprots.toDouble

  }

  ///////////////////////////////////////////////////////////////////////////

  def functionalConsistencyPerCluster(annots: Annotations) = {
    this.clusters.map{ c =>
      val nGenes = c.cluster.length.toFloat
      val annotatedGenes = c.cluster.filter( p => annots.pa contains p.toString)
      val clusterAnnotations = annotatedGenes.map(p =>annots.pa(p.toString)).flatten.groupBy(identity).mapValues(_.size)
      Debug.message("Cluster %d: %d".format(c.id, c.cluster.length))
      Debug.message(clusterAnnotations.map{ case (k,v) => "%s: %d".format(k.toString, v)}.mkString("\n"))
      clusterAnnotations.map{ case (annot,counts) =>
         (c.id, annot, c.cluster.length, c.taxa.length, counts, counts.toFloat / nGenes)
      }
    }.flatten
  }

  ///////////////////////////////////////////////////////////////////////////

  def functionalConsistencyScores(annots: Annotations): Array[(Int,Int,Int,Int,Int,Double)] = {
    this.clusters.map{ c =>
      val nGenes = c.cluster.length.toFloat
      val annotatedGenes = c.cluster.filter( p => annots.pa contains p.toString)
      val clusterAnnotations = annotatedGenes.map(p =>annots.pa(p.toString)).flatten.groupBy(identity).mapValues(_.size)
      Debug.message("Cluster %d: %d".format(c.id, c.cluster.length))
      Debug.message(clusterAnnotations.map{ case (k,v) => "%s: %d".format(k.toString, v)}.mkString("\n"))
      val score = clusterAnnotations.map{ case (annot,counts) =>
         (counts.toFloat / nGenes)
      }.foldLeft(0.0)(math.max(_, _))

      (c.id, nGenes.toInt, annotatedGenes.size, c.taxa.length, clusterAnnotations.keys.size, score)
    }
  }

  ///////////////////////////////////////////////////////////////////////////

    // GET FEATURES FOR EACH CLUSTER

  def featureIsCore   = { this.clusters.map(c => if (c.isCore(this.protMap.taxa.length)) 1 else 0 ) }
  def featureIsSingleCopy = { this.paraClusters.map(pc => if(pc.isSingleCopy) 1 else 0 ) }
  def featureNGenes   = { this.intClusters.map(c => c.cluster.length) }
  def featureNSpecies = { this.paraClusters.map(c => c.nNonEmptyTaxa) }


  def featureAnnotScores(annots: Annotations)  = { this.functionalConsistencyScores(annots).map(x => x._6) }
  def featureNFunctions(annots: Annotations)   = { this.functionalConsistencyScores(annots).map(x => x._5) }
  def featureNAnnotGenes(annots: Annotations) = { this.functionalConsistencyScores(annots).map(x => x._4) }

  def featureCoreNode(tree: Newick.Tree) = {
    tree.topologicalSorting.map{ nodeID =>
      this.getTaxaSubsetCoreAccSpecific(tree.getNodeLeafNames(nodeID)).map( _._2).toArray
    }.transpose.map{ c =>
      c.zipWithIndex.find{ case (c,i) => c } match {
        case Some((c,i)) => i
        case None => -1
      }
    }
  }

  def featureSpecificNode(tree: Newick.Tree) = {
    tree.topologicalSorting.map{ nodeID =>
      this.getTaxaSubsetCoreAccSpecific(tree.getNodeLeafNames(nodeID)).map( _._4).toArray
    }.transpose.map{ c =>
      c.zipWithIndex.find{ case (c,i) => c } match {
        case Some((c,i)) => i
        case None => -1
      }
    }
  }


  ///////////////////////////////////////////////////////////////////////////
}


object Clustering{

  var coreRange: Utils.NumberRange = Utils.NumberRange(0.90, 1.0)
  var accRange: Utils.NumberRange = Utils.NumberRange(0.05, 0.90)
  var specific: Double = 1.0

  ///////////////////////////////////////////////////////////////////////////

  def setCoreRange(lower: Double, upper: Double) = {
    this.coreRange = Utils.NumberRange(lower, upper)
  }

  def setAccRange(lower: Double, upper: Double) = {
    this.accRange = Utils.NumberRange(lower, upper)
  }

  def setSpecific(sp: Double) = {
    this.specific = sp
  }
  

  ///////////////////////////////////////////////////////////////////////////

  def apply(intClusters: Array[ClusterTypes.IntCluster], protMap: ProtMap) = {
    new Clustering(intClusters, protMap)
  }

}

}
