package panalysis {

object ProtMapCheck extends ActionObject {

  override val description = "Check if a proteinMap file has the right format"

  ///////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val protMapFile = args(0)

    if (ProtMap.check(protMapFile)) {
      println("Valid ProtMap File")
    } else {
      println("Invalid ProtMap File")
   }
  }

  ///////////////////////////////////////////////////////////////////////////

  override def usage() = {
    println("ProtMapCheck <protMapFile>")
    println("")
    println("  protMapFile: A tsv file with the following format")
    println("")
    println("  0\tspecies1|g1")
    println("  1\tspecies1|g2")
    println("  2\tspecies1|g3")
    println("  3\tspecies2|g1")
    println("  4\tspecies2|g2")
    println("  5\tspecies2|g3")
    println("  6\tspecies3|g1")
    println("  7\tspecies3|g2")
    println("  8\tspecies3|g3")
    println("  ...")
    println("")
  }

}

}
