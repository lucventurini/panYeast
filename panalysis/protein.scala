package panalysis {

case class Protein(taxa:String, pid:Int) {

  override def toString = "%s%s%d".format(taxa, Protein.delim, pid)

  def ==(that: Protein) = {
    (this.taxa == that.taxa) & (this.pid == that.pid)
  }

}

object Protein {

  var delim = "|g"

  def apply(str:String): Protein = {
    val values = str.split('|')
    Protein(values(0), values(1).drop(1).toInt)
  }

  def <(that: Protein) = {
   this.toString < that.toString
  }

  def >(that: Protein) = {
    this.toString > that.toString
  }

}


}
