package panalysis {

object TSNE extends ActionObject {

  override val description = "Perform a TSNE for each gene cluster"

  override def main(args: Array[String]) = {
    val action         = args(0)
    val protMapFile    = args(1)
    val clusteringFile = args(2)
    val outFile        = args(3)
    val outDims        = if (args.length > 4) args(4).toInt else 2

    val protMap     = ProtMap(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)

    val matrix = action.toLowerCase match {
      case "binary"        => clustering.tsneMatrixBinary
      case "count"         => clustering.tsneMatrixParalogCounts
      case "speciesbinary" => clustering.tsneMatrixBinary.transpose
      case "speciesCount"  => clustering.tsneMatrixParalogCounts.transpose
    }

    val result = TSNEUtils.run(matrix, outDims)

    Utils.doubleMatrixToFile(result, outFile, "\t")
    
  }

  /////////////////////////////////////////////////////////////////////////////

  override def usage = {
    println("tsne <action> <protMapFile> <clusteringFile> <outFile> [outDims]")
    println("")
    println("  action : binary | count | speciesBinary | speciesCount")
    println("  protMapFile: Protein map produced e.g. by orthofinder")
    println("  clusteringFile: MCL clustering file")
    println("  outFile: output file, - for stdout")
    println("  outDims: The number of dimensions to output (Default: 2)")
  
   
  }
}

}
