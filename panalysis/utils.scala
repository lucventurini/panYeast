package panalysis {

import java.io._

object Utils {

  def readProtMapFile(protMapFile: String) = {
    io.Source.fromFile(protMapFile).getLines.map(line => line.stripLineEnd.split('\t')).map{case Array(id: String, prot: String) => Protein(prot, id.toInt)}.toArray.sortWith( (p1,p2) => p1.uniqueID < p2.uniqueID)
  }

  ///////////////////////////////////////////////////////////////////////////

  def protMapTaxa(protMap: Array[Protein]) = {
    protMap.foldLeft(Array(Protein("fakeSpecies|g0"))){ (arr, currProt) =>
      val lastProt = arr.last
      if (lastProt.taxa == currProt.taxa) {
        arr
      } else {
        arr :+ currProt
      }
    }.map(p => p.taxa).drop(1)
  }

  ///////////////////////////////////////////////////////////////////////////

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

}

}
