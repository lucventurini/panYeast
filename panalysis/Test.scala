package panalysis {

import java.io.File
import java.io._

object Test extends ActionObject {

  override val description = "Run a test"

  val tops = Map("checkFastaCompression" -> checkFastaCompression _,
                 "checkReadFasta"        -> checkReadFasta _
                 ).map{ case (k,v) => (k.toLowerCase -> v)}

  override def main(args: Array[String]) = {

    if (args.length < 1 || args(0).toLowerCase == "help" || !(tops contains args(0).toLowerCase)) {
      usage
    } else {
      tops(args(0).toLowerCase)(args)
    }


  }

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

  override def usage = {
    println(tops.keys.mkString("\n"))
  }

}

}
