import scala.collection.mutable.ArrayBuffer

package panalysis {

object mciReader {

  ///////////////////////////////////////////////////////////////////////////

  def readMCLLines(clustFile: String) = {

    val buf         = io.Source.fromFile(clustFile).getLines.filter(line => line(0) != '#')
    var inmatrix    = false
    var clusterline = ""
    var clusters    = ArrayBuffer[Array[String]]()
    var dim1    = 0
    var dim2 = 0
    for(line <- buf) {
      if (line contains "dimensions") {
        val dim     = line.stripLineEnd.trim.split(' ')(1).split('x').map(x => x.toInt)
        dim1 = dim(0)
        dim2 = dim(1)
      } else if (line contains "begin") {
        inmatrix = true
      } else {
        if (inmatrix) {
          if (!(line contains ")")) {
            clusterline += line
            if (line contains "$") {
              clusters += clusterline.filterNot("\n$".toSet).split("[ \t]+").filter(x => x.length > 0)
              clusterline = ""
            }
          }
        }
      }
    }

    (dim1, dim2, clusters)

  }

  ///////////////////////////////////////////////////////////////////////////

  def readMCLClusters(clustFile: String, proteinMap:Array[Protein]) = {

    val (dim_gene, dim_cluster, clusterlines) = readMCLLines(clustFile)
    val clusters = clusterlines.map(line => line.drop(1).map(x => x.toInt).toArray.map( x => proteinMap(x)))
    (dim_gene, dim_cluster, clusters.toArray)

  }

  ///////////////////////////////////////////////////////////////////////////

  def readMCLNetwork(netFile:String) = {

    val (dimNodes, dimDup, netLines) = readMCLLines(netFile)
    var network = Array.fill[Double](dimNodes.toInt, dimNodes.toInt) { 0.0 }
    var count   = 0

    netLines.map{ arr =>
      val outNode = arr(0).toInt
      count += 1
      print("\rProcessed %d clusters".format(count))
      arr.drop(1).map(edge => edge.split(':')).map{ case Array(inNode:String, weight:String) => (outNode, inNode.toInt, weight.toFloat)}
    }.foreach{ x => x.foreach{
        case (outNode, inNode, weight) => {
          println("%d -> %d: %f".format(outNode, inNode, weight))
          network(outNode)(inNode) = weight
          network(inNode)(outNode) = weight
        }
      }
    }
    //.foldLeft(Array[(Int,Int,Float)]())(_ ++ _)
    network
  }

  ///////////////////////////////////////////////////////////////////////////
  

}

}

