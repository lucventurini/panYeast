package panalysis {

import com.jujutsu.tsne.barneshut.ParallelBHTsne;
import com.jujutsu.utils.MatrixOps;

object TSNE {

  def run(matrix: Array[Array[Double]]) = {
    val initialDims = matrix(0).length
    val perplexity  = 20.0
    val maxIter     = 1000
    val tsne        = new ParallelBHTsne()

    val result = tsne.tsne(matrix, 2, initialDims, perplexity, maxIter)
    println(MatrixOps.doubleArrayToPrintString(result, ", "))
    result
  }

  /////////////////////////////////////////////////////////////////////////////

}

}
