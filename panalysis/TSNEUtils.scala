package panalysis{

import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.MatrixOps;


object TSNEUtils {

  var perplexity = 20.0
  var maxIter    = 1000

  def setPerplexity(value: Double) = {this.perplexity = value}
  def setMaxIter(value: Int) = {this.maxIter = value}

  def run(matrix: Array[Array[Double]], outDims: Int = 2) = {
    val initialDims = matrix(0).length
    val tsne        = new ParallelBHTsne()

    tsne.tsne(matrix, outDims, initialDims, TSNEUtils.perplexity, TSNEUtils.maxIter)
  }

}


}
