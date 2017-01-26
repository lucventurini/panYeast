package panalysis {

  class mclClust(mciFile: String, protMapFile: String) {
    val (dim_gene, dim_cluster, clusters) = mciReader.readMCLClusters(mciFile)
    val prots    = readProtMapFile(protMapFile)

    // read the ProteinMapFile
    ///////////////////////////////////////////////////////////////////////////
    def readProtMapFile(protMapFile: String) = {
      io.Source.fromFile(protMapFile).getLines.map(line => line.stripLineEnd.split('\t')).map{case Array(id: String, prot: String) => (id.toInt, prot)}.toMap
    }

    ///////////////////////////////////////////////////////////////////////////

    def clusterParalogs(clusterID:Int) = {
      
    }    

  }

}
