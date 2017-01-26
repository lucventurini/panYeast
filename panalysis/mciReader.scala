import scala.collection.mutable.ArrayBuffer

package panalysis {

  object mciReader {

    ///////////////////////////////////////////////////////////////////////////

    def readMCLClusters(clustFile: String) = {

      val buf         = io.Source.fromFile(clustFile).getLines.filter(line => line(0) != '#')
      var inmatrix    = false
      var clusterline = ""
      var clusters    = ArrayBuffer[Array[Int]]()
      var dim_gene    = 0
      var dim_cluster = 0
      for(line <- buf) {
        if (line contains "dimensions") {
          val dim     = line.stripLineEnd.trim.split(' ')(1).split('x').map(x => x.toInt)
          dim_gene    = dim(0)
          dim_cluster = dim(1)
        } else if (line contains "begin") {
          inmatrix = true
        } else {
          if (inmatrix) {
            if (!(line contains ")")) {
              clusterline += line
              if (line contains "$") {
                clusters += clusterline.filterNot("\n$\t".toSet).split(' ').filter(x => x.length > 0).drop(1).map(x => x.toInt).toArray
                clusterline = ""
                println("%d -> %d".format(clusters.length-1, clusters.last.length))
              }
            }
          }
        }  
      }

      (dim_gene, dim_cluster, clusters.toArray)
  
    }

    ///////////////////////////////////////////////////////////////////////////
    

  }


}
