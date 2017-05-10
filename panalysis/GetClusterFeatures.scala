package panalysis {

import java.io.BufferedWriter

object GetClusterFeatures extends ActionObject {

  override val description = "Get a feature for each cluster"

  val features = Map( "isCore"   -> featureIsCoreW _,
                      "nGenes"   -> featureNGenesW _,
                      "nSpecies" -> featureNSpeciesW _,
                      "annotScores" -> featureAnnotScoresW _,
                      "nFunctions" -> featureNFunctionsW _,
                      "nAnnotGenes" -> featureNAnnotGenesW _
                    ).map{ case (k,v) => (k.toLowerCase, v) }

  override def main(args: Array[String]) = {
    val protMapFile    = args(0)
    val clusteringFile = args(1)
    val outFile        = args(2)
    val feature        = args(3).toLowerCase

    if (!features.contains(feature)){
      usage
      System.exit(1)
    }

    val protMap     = ProtMap(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)

    val outfd = Utils.openWrite(outFile)
    features(feature)(args.drop(4), clustering, outfd)
    outfd.close()

  }

  /////////////////////////////////////////////////////////////////////////////

  def featureIsCore(args: Array[String], clustering: Clustering)  = { clustering.clusters.map(c => c.isCore(clustering.protMap.taxa.length)) }
  def featureIsCoreW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = featureIsCore(args, clustering).foreach(v => outfd.write("%d\n".format(v)))


  def featureNGenes(args: Array[String], clustering: Clustering) = { clustering.clusters.map(c => c.cluster.length) }
  def featureNGenesW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = featureNGenes(args, clustering).foreach(v => outfd.write("%d\n".format(v)))


  def featureNSpecies(args: Array[String], clustering: Clustering) = { clustering.paraClusters.map(c => c.nNonEmptyTaxa) }
  def featureNSpeciesW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = featureNSpecies(args, clustering).foreach(v => outfd.write("%d\n".format(v)))


  def featureAnnotScores(args: Array[String], clustering: Clustering) = { ValidateClustersWithAnnots.clusterScores(clustering, Annotations(args(0), clustering.protMap)).map(x => x._6) }
  def featureAnnotScoresW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = featureAnnotScores(args, clustering).foreach(v => outfd.write("%f\n".format(v)))


  def featureNFunctions(args: Array[String], clustering: Clustering) = { ValidateClustersWithAnnots.clusterScores(clustering, Annotations(args(0), clustering.protMap)).map(x => x._5) }
  def featureNFunctionsW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = featureNFunctions(args, clustering).foreach(v => outfd.write("%d\n".format(v)))

  def featureNAnnotGenes(args: Array[String], clustering: Clustering) = { ValidateClustersWithAnnots.clusterScores(clustering, Annotations(args(0), clustering.protMap)).map(x => x._4) }
  def featureNAnnotGenesW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = featureNAnnotGenes(args, clustering).foreach(v => outfd.write("%d\n".format(v)))
  

  /////////////////////////////////////////////////////////////////////////////

  override def usage = {
    println("GetClusterFeatures: <protmapFile> <clustfile> <outFile> <feature> [featureOptions]")
    println("")
    println(" protMapFile: Protein map")
    println(" clustFile:   Output from MCL")
    println(" outFile:     Output File - for stdout")
    println(" feature:     isCore -> Is the cluster Core/accessory?")
    println("              nGenes -> Number of genes in cluster")
    println("              nSpecies -> Number of species in cluster")
    println("              coreNode <treeFile> -> Get the label of the node in the tree it is Core at")
    println("              annotScore <annotFile> -> Get the validation score of each cluster based on annotations")
    println("              nAnnotGenes <annotFile> -> Get the number of annotated genes in the cluster")
    println("              nFunctions <annotFile> -> Get the number of functions annotated in the cluster")
    println(" treeFile:    Tree in Newick format")
   
  }


}
}
