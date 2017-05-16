package panalysis {

import java.io.File
import java.io._

object Test extends ActionObject {

  override val description = "Run a test"

  val tops = Map("checkFastaCompression" -> checkFastaCompression _,
                 "checkReadFasta"        -> checkReadFasta _,
                 "topologicalSorting"    -> testTopoSort _,
                 "testLongestPath"       -> testLongestPath _,
                 "testMidPointRoot"      -> testMidPointRoot _,
                 "testAnnotations"       -> testAnnotations _,
                 "printTreeNodes"        -> printTreeNodes _,
                 "testFDR"               -> testFDR _
                 ).map{ case (k,v) => (k.toLowerCase -> v)}

  /////////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {

    if (args.length < 1 || args(0).toLowerCase == "help" || !(tops contains args(0).toLowerCase)) {
      usage
    } else {
      tops(args(0).toLowerCase)(args.drop(1))
    }


  }

  /////////////////////////////////////////////////////////////////////////////

  def testAnnotations(args: Array[String]) = {

    val annotationFile = args(0)
    val protMapFile    = args(1)

    val protMap = ProtMap(protMapFile)
    val annots  = Annotations(annotationFile, protMap)

    annots.pa.keys.foreach{ k =>
      println("%s\n  |%s\n".format(k.toString, annots.pa(k).map( _.toString ).mkString("\n  |")))
    }

    annots.ap.keys.foreach{ k =>
      println("%s\n  %s".format(k.toString, annots.ap(k).mkString(", ")))
    }

    println("#P: %d\n#A: %d".format(annots.nAnnotatedGenes, annots.nAnnotations))

  }

  /////////////////////////////////////////////////////////////////////////////

  def checkFastaCompression(args: Array[String]) = {
    val seq = "AAAAACCCCCGGGGGTTTTTACACACACTGTGTGAAAAACCCCCGGGGGTTTTTACACACACTGTGTGAAAAACCCCCGGGGGTTTTTACACACACTGTGTGAAAAAC".toLowerCase
    val x = Fasta.FastaSeq.fromString(seq)
    val bseq = x.toString.toLowerCase
    println(seq)
    (0 to bseq.length-1).foreach{ i =>
      if (seq.charAt(i) == bseq.charAt(i)){
        printf("|")
      } else {
        printf(" ")
      }
    }
    println("")
    println(bseq)
    if (bseq == seq) {
      println("OK")
    } else {
      println("NOT OK")
    }

  }

  /////////////////////////////////////////////////////////////////////////////

  def checkReadFasta(args: Array[String]) = {
    val tmpf  = File.createTempFile("temporary", "fa")
    val outfd = new PrintWriter(new FileWriter(tmpf, false))
    val seq = "ATCGATCGATGCTAGCTAGCTACGTAATATAGCTAGCTAGCTACGATATATAGCTACGTAGTAGCTACGTACGTACGTAATACG".toLowerCase
    outfd.write(">test\n%s\n>test2\n%s\n>test3\n%s".format(seq, seq, seq))
    outfd.close

    println(tmpf.getAbsolutePath)
    val fa = Fasta.read(tmpf.getAbsolutePath).map( x => x).toArray
    Fasta.print(fa)
    println(seq)

  }

  /////////////////////////////////////////////////////////////////////////////

  def testTopoSort(args: Array[String]) = {
    val tree = if (args.length > 0) {
      Newick.readFile(args(0))(0)
    } else {
      Newick.Tree.fromString("((NC,D)A,(E,F)B,X)R;")
    }

    tree.display
    println(tree.topologicalSorting map tree.getNodeName mkString(","))
  }

  /////////////////////////////////////////////////////////////////////////////
  //
  def printTreeNodes(args: Array[String]) = {
    val tree = if (args.length > 0) {
      Newick.readFile(args(0))(0)
    } else {
      Newick.Tree.fromString("((NC,D)A,(E,F)B,X)R;")
    }

    println(tree.getNodes.indices.map( id => "%d: %s".format(id, tree.getNodeName(id))).mkString("\n"))
  }

  /////////////////////////////////////////////////////////////////////////////

  def testLongestPath(args: Array[String]) = {
    val tree = Newick.Tree.fromString("((NC:1,D:1)A:1,(E:1,F:1)B:1,X:1)R;")

    tree.display
    println(tree.longestPath map tree.getNodeName mkString(","))
  }

  /////////////////////////////////////////////////////////////////////////////

  def testMidPointRoot(args: Array[String]) = {

    val tree = Newick.Tree.fromString("(((WC:1,WC2:1)NC:1,D:1)A:1,(E:1,F:1)B:1,X:1)R;")

    tree.display
    println(tree.longestPath map tree.getNodeName mkString(","))

    tree.midPointRoot.display

  }

  /////////////////////////////////////////////////////////////////////////////

  def testFDR(args: Array[String]) = {
    
    val p = if(args.length == 0) {Array(0.01, 0.02, 0.03, 0.04, 0.05,0.06,0.07,0.08,0.09,0.1,0.11,0.12,0.13,0.14,0.0001) } else args.map(_.toDouble)

    println(p.mkString(","))
    println(Statistics.fdr_bh(p).mkString(","))
  }


  /////////////////////////////////////////////////////////////////////////////

  override def usage = {
    println("Available tests")
    println(tops.keys.mkString("\n"))
  }

  /////////////////////////////////////////////////////////////////////////////

}

}
