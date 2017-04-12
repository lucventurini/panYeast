
package panalysis {

import scala.io.Source
import java.io.{BufferedWriter, OutputStreamWriter, FileOutputStream}
import scala.collection.JavaConversions._

object AddInodesToTree extends ActionObject {

  override val description = "Add identifiers to the internal nodes of the tree, for later reference"

  ///////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val treeFile  = args(0)
    val outFile   = args(1)
    val annotName = if (args.length > 2) { args(2) } else { "-" }

    val inStream    = if(treeFile == "-"){ Source.stdin } else {  Source.fromFile(treeFile) }
    var trees       = inStream.getLines.mkString("").split(';').filter(x => x.length > 0).map(t => Newick.Tree.fromString(t + ';'))

    trees.foreach{ t =>
      t.getNodes.indices.filter(n => !t.isLeaf(n)).zipWithIndex.foreach{ case (n, i) =>
        if (t.getNodeName(n) != "") {
          t.addNodeAnnot(n, annotName, t.getNodeName(n))
        }
        t.setNodeName(n, "INODE_%d".format(i))
      }
    }

    val outfd = if(outFile == "-") new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"))

    trees.foreach{ t =>
      outfd.write("\n%s\n".format(t.toNewick))
    }
    outfd.close()
  }

  override def usage = {
    println("AddInodesToTree <inTree> <outTree> [annotName]")
    println("")
    println(" inTree:    Newick tree format (- for stdin)")
    println(" outTree:   Newick tree format (- for stdout)")
    println(" annotName: Put the current name into an annotation with the following name, default: sup")
  }

}
}
