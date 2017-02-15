import scala.collection.mutable.ArrayBuffer

package panalysis {

object MCIReader {

  val reportLines = 10000

  ///////////////////////////////////////////////////////////////////////////

  def readLines(clustFile: String) = {

    val buf      = io.Source.fromFile(clustFile).getLines.filter(line => line(0) != '#')
    var inmatrix = false
    var rowline  = ""
    var rows     = ArrayBuffer[Array[String]]()
    var dim1     = 0
    var dim2     = 0
    var count    = 0

    println("Reading MCL File")
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
            rowline += line
            if (line contains "$") {
              count += 1
              if (count % reportLines == 0) {
                print("\rProcessed %d lines".format(count))
              }
              rows += rowline.filterNot("\n$".toSet).split("[ \t]+").filter(x => x.length > 0)
              rowline = ""
            }
          }
        }
      }
    }
    println("")

    (dim1, dim2, rows)

  }

  ///////////////////////////////////////////////////////////////////////////

  def readClustering(clustFile: String) = {

    val (dim_gene, dim_cluster, lines) = readLines(clustFile)
    val clusters : Array[Array[Int]] = lines.map(line => line.drop(1).map(x => x.toInt).toArray).toArray
    (dim_gene, dim_cluster, clusters.zipWithIndex.map{ case (c,i) => ClusterTypes.IntCluster(i, c)})

  }

  ///////////////////////////////////////////////////////////////////////////

  def readNetwork(netFile:String) = {

    val (dimNodes, dimDup, netLines) = readLines(netFile)
    var network = new mciNetwork
    var count   = 0
 
    println("Parsing network")
    netLines.map{ arr =>
      val outNode = arr(0).toInt
      arr.drop(1).map(edge => edge.split(':')).map{ case Array(inNode:String, weight:String) => (outNode, inNode.toInt, weight.toFloat)}
    }.foreach{ x => x.foreach{
        case (outNode, inNode, weight) => {
          network += (outNode, inNode, weight)
          count += 1
          if ( count % reportLines == 0) {
            print("\rProcessed %d edges".format(count))
          }
        }
      }
    }
    println("")
    network
  }

  ///////////////////////////////////////////////////////////////////////////
  

}

}

