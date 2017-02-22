package panalysis {

case class Protein(taxa:String, pid:Int, uniqueID:Int) {

  override def toString = "%s%s%d".format(taxa, Protein.delim, pid)

  def ==(that: Protein) = {
    (this.taxa == that.taxa) & (this.pid == that.pid)
  }

}

object Protein {

  var delim = "|g"
  var regex_delim = "\\|g"

  /////////////////////////////////////////////////////////////////////////////

  def apply(str:String): Protein = {
    val values = str.split(regex_delim)
    Protein(values(0), values(1).toInt, -1)
  }

  /////////////////////////////////////////////////////////////////////////////

  def apply(str:String, uniqueID:Int): Protein = {
    val values = str.split(regex_delim)
    Protein(values(0), values(1).toInt, uniqueID)
  }

  /////////////////////////////////////////////////////////////////////////////

  def <(that: Protein) = {
   this.toString < that.toString
  }

  /////////////////////////////////////////////////////////////////////////////

  def >(that: Protein) = {
    this.toString > that.toString
  }

  /////////////////////////////////////////////////////////////////////////////

  def isProteinString(str: String) = {
    // Maybe need to extend further?
    val strsplit = str.split(regex_delim)
    strsplit.length == 2
  }

}


}