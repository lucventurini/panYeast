package panalysis {

object GetClusterAnnots extends ActionObject {

  override val description = "Get the annotations for each cluster"

  override def main(args: Array[String]) = {
    val protMapFile    = args(0)
    val clusteringFile = args(1)
    val annotFile      = args(2)
    val outFile        = args(3)

    val protMap     = ProtMap(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)
    val annots      = Annotations(annotFile, protMap)
    val outfd       = Utils.openWrite(outFile)

    outfd.write("#ClusterID\tAnnotationID\tAnnotationDescription\tnAnnotGenes\tnGenes\tnSpecies\tAnnotationScore\n")
    clustering.functionalConsistencyPerCluster(annots).sortBy( x => (x._1, -x._6)).foreach{ case (cid, annot, nGenes,nTaxa, nAnnot, score) =>
      outfd.write("%d\t%s\t%s\t%d\t%d\t%d\t%f\n".format(cid, annot.id, annot.description, nAnnot, nGenes, nTaxa, score))
    }

    outfd.close()

  }

  override def usage = {
    println("getClusterAnnots <protMapFile> <clusteringFile> <annotFile> <outFile>")
    println("")
    println("  protMapFile: Protein map produced e.g. by orthofinder")
    println("  clusteringFile: MCL clustering file")
    println("  annotFile: Annotation file")
    println("  outFile: output file, - for stdout")
    println("")

  }

}

}
