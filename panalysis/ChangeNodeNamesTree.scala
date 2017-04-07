
package panalysis {

import scala.io.Source
import java.io.{BufferedWriter, OutputStreamWriter, FileOutputStream}
import scala.collection.JavaConversions._

object ChangeNodeNamesTree extends ActionObject {

  override val description = "Given a tree, Change the names of the nodes based on a mapping file"

  ///////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val treeFile       = args(0)
    val nameMapFile    = args(1)
    val outFile        = args(2)

    var trees   = Source.fromFile(treeFile).getLines.mkString("").split(';').filter(x => x.length > 0).map(t => Newick.Tree.fromString(t + ';'))
    val nameMap = Source.fromFile(nameMapFile).getLines.map(l => l.split("\t")).filter(l => l.length == 2).map(l => l(0) -> l(1)).toMap
    println(Source.fromFile(nameMapFile).getLines.filter(l => l.split("\t").length == 2).mkString("\n"))

    val outfd = if(outFile == "-") new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"))

    nameMap.foreach{ case (k,v) => println("%s -> %s".format(k,v))}
    trees.indices.foreach{ treeID =>
      trees(treeID).getNodes.foreach(n => println(n.name))
      trees(treeID).getNodes.indices.filter(nodeID => nameMap contains trees(treeID).getNodeName(nodeID)).foreach{ nodeID =>
        println("%s -> %s".format(trees(treeID).getNodeName(nodeID), nameMap(trees(treeID).getNodeName(nodeID))))
        trees(treeID).setNodeName(nodeID, nameMap(trees(treeID).getNodeName(nodeID)))
      }
    }

    trees.foreach{ t =>
      outfd.write("\n%s\n".format(t.toNewick))
    }
    outfd.close()

  }

  ///////////////////////////////////////////////////////////////////////////

  override def usage() = {
    println("ChangeNodeNamesTree <treeFile> <nameMapFile> <outFile>")
    println("")
    println("  treeFile: A tree in Newick format")
    println("  nameMapFile: Tab separated file with two columns, first the names of nodes in original tree, and second with names of nodes in output tree")
    println("  outFile: output file, - for stdout")
    println("")
  }

}

}
