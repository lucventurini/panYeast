package panalysis {

import java.io._
import scala.Console
import scala.io.Source
import java.io.{BufferedWriter, OutputStreamWriter, FileOutputStream}
import scala.collection.JavaConversions._

object Utils {

  ///////////////////////////////////////////////////////////////////////////

  case class NumberRange(min:Double, max:Double) {

    def isBetween(value: Double) = {
      (value >= min) && (value <= max)
    }

  }

  def doubleMatrixToFile(mat: Array[Array[Double]], outFile:String, sep:String) = {
    val bw = openWrite(outFile)
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

  ///////////////////////////////////////////////////////////////////////////

  var messagesEnabled = true

  def enableMessages  = { this.messagesEnabled = true }
  def disableMessages = { this.messagesEnabled = false }

  def message(msg: String, messageType: String = "", ln: Boolean = true, or: Boolean = false) = { if (this.messagesEnabled || or) msg.split("\n").foreach(l => Console.err.print("%s%s%s".format(messageType, l, if (ln) "\n" else ""))) }
  def warning(msg: String, ln: Boolean =true) = message(msg, "WARNING: ", ln)
  def error(msg: String, ln: Boolean =true) = message(msg, "ERROR: ", ln)

  ///////////////////////////////////////////////////////////////////////////

  def openWrite(file: String) = if(file == "-") new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))
  def openRead(file: String)  = if(file == "-"){ Source.stdin } else {  Source.fromFile(file) }

}

}
