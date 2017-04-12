package panalysis {

import java.io._
import scala.io.Source
import scala.Console

object ReRootTree extends ActionObject {

  override val description = "Reroot a tree"

  override def main(args: Array[String]) = {
    val actions = Map( "outGroup" -> outGroupRoot _,
                       "midPoint" -> midPointRoot _).map{ case (x,y) => (x.toLowerCase, y)}

    if (args.length < 1 || args(0).toLowerCase == "help" || !(actions contains args(0).toLowerCase)) {
      usage
    } else {
      actions(args(0).toLowerCase)(args.slice(1, args.length))
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  def outGroupRoot(args: Array[String]) = {
    val treeFile = args(0)
    val outGroup = args(1)
    val outFile  = args(2)

    val inStream = if(treeFile == "-"){ Source.stdin } else {  Source.fromFile(treeFile) }
    var trees = inStream.getLines.mkString("").split(';').filter(x => x.length > 0).map(t => Newick.Tree.fromString(t + ';'))

    val outfd = if(outFile == "-") new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"))

    trees.filter(t => t.getNodes.map(_.name) contains outGroup).map(t => t.outGroupRoot(outGroup)).foreach{ t =>
      outfd.write("\n%s\n".format(t.toNewick))
    }
    outfd.close()
  }

  /////////////////////////////////////////////////////////////////////////////

  def midPointRoot(args: Array[String]) = {
    val treeFile = args(0)
    val outFile  = args(1)

    val inStream = if(treeFile == "-"){ Source.stdin } else {  Source.fromFile(treeFile) }
    var trees = inStream.getLines.mkString("").split(';').filter(x => x.length > 0).map(t => Newick.Tree.fromString(t + ';'))

    val outfd = if(outFile == "-") new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"))

    trees.map(t => t.midPointRoot).foreach{ t =>
      outfd.write("\n%s\n".format(t.toNewick))
    }
    outfd.close()
  }

  /////////////////////////////////////////////////////////////////////////////

  override def usage = {
    println("reRootTree <action> <options>")
    println(" Actions:")
    println("   outGroup <tree> <outGroup> <outFile>")
    println("   midPoint <tree> <outFile>")
    println("")
    println(" tree: Tree in newick format '-' for stdin")
    println(" outGroup: Name of node to use as outgroup")
    println(" outFile: Output location of tree in newick format '-' for stdout")
  }

}
}
