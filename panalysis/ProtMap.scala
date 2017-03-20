package panalysis {

object ProtMap {

  ///////////////////////////////////////////////////////////////////////////

  def read(protMapFile: String) = {
    io.Source.fromFile(protMapFile).getLines.map(line => line.stripLineEnd.split('\t')).map{case Array(id: String, prot: String) => Protein(prot, id.toInt)}.toArray.sortWith( (p1,p2) => p1.uniqueID < p2.uniqueID)
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
