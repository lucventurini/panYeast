package panalysis {

case class Protein(taxa:String, pid:String, uniqueID:Int) {

  override def toString = "%s%s%s".format(taxa, Protein.delim, pid)

  def ==(that: Protein) = {
    (this.taxa == that.taxa) & (this.pid == that.pid)
  }

  def equals(that: Protein) = { this == that }

  override def hashCode = this.toString.hashCode

}

object Protein {

  var delim = "|"
  def delim_regex = java.util.regex.Pattern.quote(this.delim)

  /////////////////////////////////////////////////////////////////////////////

  def apply(str:String): Protein = {
    val values = str.split(this.delim_regex)
    Protein(values(0), values(1), -1)
  }

  /////////////////////////////////////////////////////////////////////////////

  def apply(str:String, uniqueID:Int): Protein = {
    val values = str.split(this.delim_regex)
    Protein(values(0), values(1), uniqueID)
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
    val strsplit = str.split(this.delim_regex)
    strsplit.length == 2
  }

}


}
