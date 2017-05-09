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
                 "testAnnotations"       -> testAnnotations _
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
    val tree = Newick.Tree.fromString("((NC,D)A,(E,F)B,X)R;")

    tree.display
    println(tree.topologicalSorting map tree.getNodeName mkString(","))
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

  override def usage = {
    println("Available tests")
    println(tops.keys.mkString("\n"))
  }

}

}
