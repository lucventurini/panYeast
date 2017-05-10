package panalysis {

import scala.io.Source

object ResolveParalogs extends ActionObject {

  override val description = "Given a phylogenetic tree for a paralog cluster, resolve the paralog groups in that cluster"

  ///////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val treeFile = args(0)
    val outFile  = args(1)

    val inStream    = if(treeFile == "-"){ Source.stdin } else {  Source.fromFile(treeFile) }
    var trees       = inStream.getLines.mkString("").split(';').filter(x => x.length > 0).map(t => Newick.Tree.fromString(t + ';'))

    trees.map(_.groupParalogs)

  }

  ///////////////////////////////////////////////////////////////////////////

  override def usage() = {
    println("resolveParalogs <treeFile> <outFile>")
    println("")
    println("  treeFile: A tree in newick format, with leaves annotated as protein formatted strings (speciesID|proteinID)")
    println("  outFile: A file with each line containing a cluster, and with comma separated proteins")
    println("")
  }

}

}
