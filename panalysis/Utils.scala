package panalysis {

import java.io._
import scala.Console

object Utils {

  ///////////////////////////////////////////////////////////////////////////

  case class NumberRange(min:Double, max:Double) {

    def isBetween(value: Double) = {
      (value >= min) && (value <= max)
    }

  }

  def doubleMatrixToFile(mat: Array[Array[Double]], outFile:String, sep:String) = {
    val bw = new BufferedWriter(new FileWriter(new File(outFile)))
    mat.map( gene => gene.mkString(sep)).foreach( line => bw.write(line + "\n"))
    bw.close()
  }

  ///////////////////////////////////////////////////////////////////////////

  def vcatMatrix(mat1: Array[Array[Double]], mat2: Array[Array[Double]]): Array[Array[Double]] = {
    mat1 ++ mat2
  }

  ///////////////////////////////////////////////////////////////////////////

  def vcatMatrixVector(mat: Array[Array[Double]], vect: Array[Double]): Array[Array[Double]] = {
    mat :+ vect
  }

  ///////////////////////////////////////////////////////////////////////////

  def hcatMatrix(mat1: Array[Array[Double]], mat2: Array[Array[Double]]): Array[Array[Double]] = {
    (0 to mat1.length-1).toArray.map( i => mat1(i) ++ mat2(i))
  }

  ///////////////////////////////////////////////////////////////////////////

  def hcatMatrixVector(mat: Array[Array[Double]], vect: Array[Double]): Array[Array[Double]] = {
    (0 to mat.length-1).toArray.map( i => mat(i) :+ vect(i))
  }

  ///////////////////////////////////////////////////////////////////////////

  def fastLista2Map(f:Traversable[Fasta.Entry]) = {
    f.map( x => Protein(x.description) -> x)
  }

  ///////////////////////////////////////////////////////////////////////////

  def setParallelismGlobally(numThreads: Int): Unit = {
    val parPkgObj = scala.collection.parallel.`package`
    val defaultTaskSupportField = parPkgObj.getClass.getDeclaredFields.find{
      _.getName == "defaultTaskSupport"
    }.get

    defaultTaskSupportField.setAccessible(true)
    defaultTaskSupportField.set(
      parPkgObj, 
      new scala.collection.parallel.ForkJoinTaskSupport(
        new java.util.concurrent.ForkJoinPool(numThreads)
      ) 
    )
  }

  def message(msg: String, messageType: String = "") = msg.split("\n").foreach(l => Console.err.println("%s%s".format(messageType, l)))
  def warning(msg: String) = message(msg, "WARNING: ")
  def error(msg: String) = message(msg, "ERROR: ")

}

}
