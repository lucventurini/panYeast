package panalysis {

object mainClass {

  val ops = Map("test"    -> test _,
                "readMCL" -> readMCL _)

  ///////////////////////////////////////////////////////////////////////////

  def main(args: Array[String]) = {

    if (args.length < 1) {
      usage
    } else {

      val op = args(0)

      ops(op)(args.slice(1, args.length))
    }
    
  }

  ///////////////////////////////////////////////////////////////////////////

  def usage() = {

    println("You done goofed, consequenses will never be the same!")

  }

  ///////////////////////////////////////////////////////////////////////////

  def test(args: Array[String]) = {
    
  }

  ///////////////////////////////////////////////////////////////////////////

  def readMCL(args: Array[String]) = {

    val clusters = new mclClust(args(0), Utils.readProtMapFile(args(1))).paraClusters
    clusters.zipWithIndex.foreach{ case (clust:Array[Array[Protein]], index:Int) => println("%d,%d".format(index, clust.length))}

  }

}

}
