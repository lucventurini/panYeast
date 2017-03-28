package panalysis {

import java.io._
import scala.io.Source
import scala.Console

object ReRootTree extends ActionObject {

  override val description = "Given an outgroup, root a tree"

  override def main(args: Array[String]) = {
    val treeFile = args(0)
    val outGroup = args(1)
    val outFile  = args(2)

    var trees = Source.fromFile(treeFile).getLines.mkString("").split(';').filter(x => x.length > 0).map(t => Newick.Tree.fromString(t + ';'))

    val outfd = if(outFile == "-") new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"))

    trees.filter(t => t.getNodes.map(_.name) contains outGroup).map(t => t.outGroupRoot(outGroup)).foreach{ t =>
      outfd.write("\n%s\n".format(t.toNewick))
    }
    outfd.close()
  }

  override def usage = {
    println("reRootTree <tree> <outGroup> <outFile>")
    println("")
    println(" tree: Tree in newick format '-' for stdin")
    println(" outGroup: Name of node to use as outgroup")
    println(" outFile: Output location of tree in newick format '-' for stdout")
  }

}
}
