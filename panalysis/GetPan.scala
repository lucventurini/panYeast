package panalysis {

object GetPan extends ActionObject {

  override val description = "Find the clusters that associate to a group of species"

  ///////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val protMapFile    = args(0)
    val clusteringFile = args(1)
    val speciesGroups  = args(2).split(";").map( _.split(",") )
    val outFile        = args(3)

    val protMap     = ProtMap(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)
    val taxaMap     = protMap.taxa.zipWithIndex.map{ case (t,i) => t -> i}.toMap

    val outfd = Utils.openWrite(outFile)
    outfd.write("#groupID\tSpecies\tcoreClusterIDs\taccessoryClusterIDs\tspecificClusterIDs\n")

    speciesGroups.zipWithIndex.foreach{ case (species, i) => 
      val characterization = clustering.getTaxaSubsetCoreAccSpecific(species)
      val core    : Array[Int] = characterization.filter{case (id, isc, isa, iss) => isc}.map( c => c._1)
      val acc     : Array[Int] = characterization.filter{case (id, isc, isa, iss) => isa}.map( c => c._1)
      val specific: Array[Int] = characterization.filter{case (id, isc, isa, iss) => iss}.map( c => c._1)
      outfd.write("%d\t%s\t%s\t%s\t%s\n".format(i, species.mkString(","), core.mkString(","), acc.mkString(","), specific.mkString(",")))
    }
    outfd.close()

  }

  ///////////////////////////////////////////////////////////////////////////

  override def usage() = {
    println("getPan <protMapFile> <clusteringFile> <species> <outFile>")
    println("")
    println("  protMapFile: Protein map produced e.g. by orthofinder")
    println("  clusteringFile: MCL clustering file")
    println("  species: Comma-separated list of species identifiers. Several sets can be provided, separated by semicolons")
    println("  outFile: output file, - for stdout")
    println("")
  }

}

}
