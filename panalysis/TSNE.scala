package panalysis {

import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.MatrixOps;

object TSNE extends ActionObject {

  override val description = "Perform a TSNE for each gene cluster"

  override def main(args: Array[String]) = {
    val action         = args(0)
    val protMapFile    = args(1)
    val clusteringFile = args(2)
    val outFile        = args(3)

    val protMap     = ProtMap.read(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)

    val matrix = action.toLowerCase match {
      case "binary"        => clustering.tsneMatrixBinary
      case "count"         => clustering.tsneMatrixParalogCounts
      case "speciesbinary" => clustering.tsneMatrixBinary.transpose
      case "speciesCount"  => clustering.tsneMatrixParalogCounts.transpose
    }

    val isCore = clustering.getCoreLabels.map(x => x.toDouble).toArray
    val counts = clustering.getCountLabels.map(x => x.toDouble).toArray
    val labels = Array(isCore, counts).transpose
    val result = run(matrix)

    Utils.doubleMatrixToFile(Utils.hcatMatrix(result,labels), outFile, "\t")
    
  }

  /////////////////////////////////////////////////////////////////////////////

  def run(matrix: Array[Array[Double]]) = {
    val initialDims = matrix(0).length
    val perplexity  = 20.0
    val maxIter     = 1000
    val tsne        = new ParallelBHTsne()

    val result = tsne.tsne(matrix, 2, initialDims, perplexity, maxIter)
    //println(MatrixOps.doubleArrayToPrintString(result, ", "))
    result
  }

  /////////////////////////////////////////////////////////////////////////////

  override def usage = {
    println("tsne <action> <protMapFile> <clusteringFile> <outFile>")
    println("")
    println("  action : binary | count | speciesBinary | speciesCount")
  }
}

}
