package panalysis {

import scala.io.Source

object PrintTree extends ActionObject {

  override val description = "Read and print a Newick formatted tree to see if it is valid"

  ///////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val treeFile = args(0)

    val inStream = Utils.openRead(treeFile)//if(treeFile == "-"){ Source.stdin } else {  Source.fromFile(treeFile) }
    val trees = inStream.getLines.mkString("").split(';').filter(x => x.length > 0)
    trees.foreach{ t =>
      val tree = Newick.Tree.fromString(t + ';')
      tree.display
    }
   

  }

  ///////////////////////////////////////////////////////////////////////////

  override def usage() = {
    println("PrintTree <treeFile>")
    println("")
    println("  treeFile: A tree in Newick format")
    println("")
  }

}

}
