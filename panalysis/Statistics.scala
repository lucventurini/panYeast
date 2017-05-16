package panalysis {

import org.apache.commons.math3.distribution.ChiSquaredDistribution

object Statistics {

  var alpha = 0.05

  def setAlpha(value: Double) = { this.alpha = value }

  /////////////////////////////////////////////////////////////////////////////

  def enrich[T](background: Set[T], foreground: Set[T], annotation: Set[T]) = {

    val bf = background & foreground
    val ba = background & annotation

    val aS = bf & ba
    val bS = bf -- aS
    val cS = ba -- aS
    val dS = background -- (aS ++ bS ++ cS)

    val (a,b,c,d) = (aS.size, bS.size, cS.size, dS.size)

    val chi2    = ((a+b+c+d)*((a*b)-(b*c)) *((a*b)-(b*c))).toDouble / ( (a+b)*(c+d)*(b+d)+(a+c) ).toDouble
    val dist    = new ChiSquaredDistribution(1.0)
    val cumProb = dist.cumulativeProbability(chi2)
    (a, b, c, d, chi2, 1.0-cumProb)

  }

  /////////////////////////////////////////////////////////////////////////////

  def fwer_bonferroni(pvals: Array[Double]) = {
    pvals.map(p => p * pvals.length)
  }

  /////////////////////////////////////////////////////////////////////////////

  def fdr_bh(pvals: Array[Double]) = {
    val sortedIndices = pvals.zipWithIndex.sortBy(_._1).map(_._2)

    Debug.message(sortedIndices.map(pvals).mkString(","))
      // Correction
    //val factor = sortedIndices.indices.map(i => ((i+1).toDouble/pvals.length.toDouble)
    val q = sortedIndices.map(i => pvals(i) / ((i+1).toDouble/pvals.length.toDouble))

    Debug.message(q.mkString(","))

      // Ensure monotonicity
    val qm = q.foldLeft((Array.empty[Double]),q(0)){ case ((arr, prev),next) =>
      val narr = arr :+ math.min(1.0,math.max(prev,next))
      (narr, narr.last)
    }._1
 
      // Not the most efficient way to do this, but how to do functionally? (LOOK INTO)
    qm.zip(sortedIndices).sortBy(_._2).map(_._1)
  }

  /////////////////////////////////////////////////////////////////////////////

}

}
