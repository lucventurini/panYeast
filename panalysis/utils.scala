package panalysis {

object Utils {

  def readProtMapFile(protMapFile: String) = {
    io.Source.fromFile(protMapFile).getLines.map(line => line.stripLineEnd.split('\t')).map{case Array(id: String, prot: String) => (id.toInt, Protein(prot))}.toMap
  }

  ///////////////////////////////////////////////////////////////////////////

  def fastLista2Map(f:Iterable[Fasta.Entry]) = {
    f.map( x => Protein(x.description) -> x)
  }

  ///////////////////////////////////////////////////////////////////////////

}

}
