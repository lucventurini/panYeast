package panalysis {

object mainClass {

  val ops = Map("test"       -> test _,
                "tsneCount"  -> tsneCount _,
                "tsneBinary" -> tsneBinary _,
                "tsneCountSpecies"  -> tsneCountSpecies _,
                "tsneBinarySpecies" -> tsneBinarySpecies _,
                "readMCL"    -> readMCL _,
                "readNet"    -> readNet _)

  ///////////////////////////////////////////////////////////////////////////

  def main(args: Array[String]): Unit = {

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
    val protMap = Utils.readProtMapFile(args(0))
    Utils.protMapTaxa(protMap).foreach(x => println(x))
  }

  ///////////////////////////////////////////////////////////////////////////

  def readMCL(args: Array[String]) = {

    val protMap = Utils.readProtMapFile(args(1))
    val clusters = new mclClust(args(0), protMap).paraClusters
    clusters.zipWithIndex.foreach{ case (clust:Array[Array[Protein]], index:Int) => println("%d,%d".format(index, clust.length))}

  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneCountSpecies(args: Array[String]) = {
    val protMap = Utils.readProtMapFile(args(1))
    val clusters = new mclClust(args(0), protMap)
    val matrix   = clusters.tsneMatrixParalogCounts.transpose
    val result   = TSNE.run(matrix)

    Utils.doubleMatrixToFile(result, "tsneCountSpecies.mat", "\t")
  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneBinarySpecies(args: Array[String]) = {
    val protMap = Utils.readProtMapFile(args(1))
    val clusters = new mclClust(args(0), protMap)
    val matrix   = clusters.tsneMatrixParalogCounts.transpose
    val result   = TSNE.run(matrix)

    Utils.doubleMatrixToFile(result, "tsneBinarySpecies.mat", "\t")
  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneCount(args: Array[String]) = {
    val protMap = Utils.readProtMapFile(args(1))
    val clusters = new mclClust(args(0), protMap)
    val matrix   = clusters.tsneMatrixParalogCounts
    val result   = TSNE.run(matrix)

    val isCore = clusters.getCoreLabels.map(x => x.toDouble).toArray
    val counts = clusters.getCountLabels.map(x => x.toDouble).toArray
    val labels = Array(isCore, counts).transpose

    Utils.doubleMatrixToFile(Utils.hcatMatrix(result,labels), "tsneCount.mat", "\t")
  }

  ///////////////////////////////////////////////////////////////////////////

  def tsneBinary(args: Array[String]) = {
    val protMap = Utils.readProtMapFile(args(1))
    val clusters = new mclClust(args(0), protMap)
    val matrix   = clusters.tsneMatrixBinary
    val result   = TSNE.run(matrix)

    val isCore = clusters.getCoreLabels.map(x => x.toDouble).toArray
    val counts = clusters.getCountLabels.map(x => x.toDouble).toArray
    val labels = Array(isCore, counts).transpose

    Utils.doubleMatrixToFile(Utils.hcatMatrix(result,labels), "tsneBinary.mat", "\t")
  }


  ///////////////////////////////////////////////////////////////////////////

  def readNet(args: Array[String]) = {
    val network = mciReader.readMCLNetwork(args(0))
    println("#Nodes: %d\n#Edges: %d".format(network.length, network.length))
  }

}

}
