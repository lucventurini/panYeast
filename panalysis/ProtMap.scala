package panalysis {

class ProtMap(map: Array[Protein], taxaArray: Array[String]) {

  def apply(i: Int) = {
    this.map(i)
  }

  val inverseMap = map.zipWithIndex.map{case (p,i) => (p.toString, i)}.toMap
  def inverse(p: Protein) = { this.inverseMap(p.toString)}

  def taxa = this.taxaArray
  def getMap = this.map

}

object ProtMap {

  ///////////////////////////////////////////////////////////////////////////

  def read(protMapFile: String) = {
    io.Source.fromFile(protMapFile).getLines.map(line => line.stripLineEnd.split('\t')).filter( _.length == 2).map{case Array(id: String, prot: String) => Protein(prot, id.toInt)}.toArray.sortWith( (p1,p2) => p1.uniqueID < p2.uniqueID)
  }

  ///////////////////////////////////////////////////////////////////////////

  def apply(protMapFile: String) = {
    val protMap = ProtMap.read(protMapFile)
    val taxa    = ProtMap.protMapTaxa(protMap)
    new ProtMap(protMap, taxa)
  }

  ///////////////////////////////////////////////////////////////////////////

  def apply(protMapFile: String, taxa: Array[String]) = {
    val protMap = ProtMap.read(protMapFile)
    new ProtMap(protMap, taxa)
  }
  

  ///////////////////////////////////////////////////////////////////////////

  def check(protMapFile: String) = {
    io.Source.fromFile(protMapFile).getLines.forall{line =>
      val linesplit = line.stripLineEnd.split('\t')
      val lengthOK  = linesplit.length == 2
      val intOK     = lengthOK & (linesplit(0) forall {Character.isDigit _})
      val protOK    = lengthOK & Protein.isProteinString(linesplit(1))

      lengthOK & intOK & protOK
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  def protMapTaxa(protMap: Array[Protein]) = {
    protMap.foldLeft(Array(Protein("fakeSpecies|0"))){ (arr, currProt) =>
      val lastProt = arr.last
      if (lastProt.taxa == currProt.taxa) {
        arr
      } else {
        arr :+ currProt
      }
    }.map(p => p.taxa).drop(1)
  }

  ///////////////////////////////////////////////////////////////////////////

}

}
