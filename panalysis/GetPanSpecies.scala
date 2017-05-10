package panalysis {

import scala.io.Source
import java.io.{BufferedWriter, OutputStreamWriter, FileOutputStream}
import scala.collection.JavaConversions._

object GetPanSpecies extends ActionObject {

  override val description = "For a set of species, find clusters that are core/accessory/specific to those species."

  /////////////////////////////////////////////////////////////////////////////

  override def main(args: Array[String]) = {
    val protMapFile    = args(0)
    val clusteringFile = args(1)
    val species        = args(2)
    val outFile        = args(3)

    val protMap     = ProtMap(protMapFile)
    val intClusters = MCIReader.readClustering(clusteringFile)._3
    val clustering  = Clustering(intClusters, protMap)
    val speciesArr  = species.split(",")

    val validSpecies = speciesArr.filter{ s =>
      val isPresent = protMap.taxa contains s
      if (!isPresent) { Utils.warning("Species '%s' is not present in this dataset.".format(s)) }
      isPresent
    }

    val outfd = if(outFile == "-") new BufferedWriter(new OutputStreamWriter(System.out, "utf-8")) else new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"))

    outfd.write("#ClusterID\tisCore\tisAccessory\tisSpecific\n")
    clustering.getTaxaSubsetCoreAccSpecific(validSpecies).foreach{ case (id, isc, isa, iss) =>
      outfd.write("%d\t%d\t%d\t%d\n".format(id, if (isc) 1 else 0, if (isa) 1 else 0, if (iss) 1 else 0))
    }
    outfd.close()

  }

  override def usage = {
    println("GetPanSpecies <protMapFile> <clusteringFile> <speciesList> <outFile>")
    println("")
    println("  protMapFile: Protein map produced e.g. by orthofinder")
    println("  clusteringFile: MCL clustering file")
    println("  speciesList: A comma-separated list of species")
    println("  outFile: output file, - for stdout")
  }

}
}
