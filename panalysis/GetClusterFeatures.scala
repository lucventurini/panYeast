package panalysis {

import java.io.BufferedWriter

object GetClusterFeatures extends ActionObject {

  override val description = "Get a feature for each cluster"

  val features = Map( "isCore"   -> featureIsCoreW _,
                      "isSingleCopy" -> featureIsSingleCopyW _,
                      "nGenes"   -> featureNGenesW _,
                      "nSpecies" -> featureNSpeciesW _,
                      "annotScores" -> featureAnnotScoresW _,
                      "nFunctions" -> featureNFunctionsW _,
                      "nAnnotGenes" -> featureNAnnotGenesW _,
                      "coreNode"    -> featureCoreNodeW _,
                      "specificNode" -> featureSpecificNodeW _
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

  def featureIsCoreW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = clustering.featureIsCore.foreach(v => outfd.write("%s\n".format( if (v == 1) "Core" else "Accessory")))

  def featureIsSingleCopyW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = clustering.featureIsSingleCopy.foreach(v => outfd.write("%s\n".format(if (v == 1) "SC" else "Not SC")))


  def featureNGenesW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = clustering.featureNGenes.foreach(v => outfd.write("%d\n".format(v)))


  def featureNSpeciesW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = clustering.featureNSpecies.foreach(v => outfd.write("%d\n".format(v)))


  def featureAnnotScoresW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = clustering.featureAnnotScores(Annotations(args(0), clustering.protMap)).foreach(v => outfd.write("%f\n".format(v)))


  def featureNFunctionsW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = clustering.featureNFunctions(Annotations(args(0), clustering.protMap)).foreach(v => outfd.write("%d\n".format(v)))

  def featureNAnnotGenesW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = clustering.featureNAnnotGenes(Annotations(args(0), clustering.protMap)).foreach(v => outfd.write("%d\n".format(v)))

  def featureCoreNodeW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = {
    val tree = Newick.readFile(args(0))(0)
    clustering.featureCoreNode(tree).foreach(v => outfd.write("%s\n".format(tree.getNodeName(v))))
  }

  def featureSpecificNodeW(args: Array[String], clustering: Clustering, outfd: BufferedWriter) = {
    val tree = Newick.readFile(args(0))(0)
    clustering.featureSpecificNode(tree).foreach(v => outfd.write("%s\n".format( if (v > -1) tree.getNodeName(v) else "NotSpecific")))
  }


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
