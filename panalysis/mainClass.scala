package panalysis {

import scala.annotation.tailrec

object mainClass {

  val ops = Map("tsne"             -> TSNE,            // java -Xms20G -jar panalysis.jar tsne Count ../run/orthagogue/proteins.map ../run/mcl/mcl_1.3.out  tsneCount.mat
                "getClusterFastas" -> GetClusterFastas, // java -Xms20g -jar panalysis.jar getClusterFastas singlecopycore ../run/diamond/input_fasta.fa ../run/orthagogue/proteins.map ../run/mcl/mcl_1.4.out run/clusterFastas/test.
                "resolveParalogs"  -> ResolveParalogs,
                "actionTemplate"   -> ActionTemplate,
                "protMapCheck"     -> ProtMapCheck,
                "printTree"        -> PrintTree,
                "addPanToTree"     -> AddPanToTree,
                "getPanTree"       -> GetPanTree,
                "reRootTree"       -> ReRootTree,
                "changeNodeNamesTree" -> ChangeNodeNamesTree,
                "cmpClust"         -> CmpClust,
                "addInodesToTree"  -> AddInodesToTree,
                "resolveParalogs"  -> ResolveParalogs,
                "GetClusterGenes"  -> GetClusterGenes,
                "ValidateClustersWithAnnots" -> ValidateClustersWithAnnots,
                "GetPanSpecies"    -> GetPanSpecies,
                "GetClusterFeatures" -> GetClusterFeatures,
                "PanCoreEnrichments" -> PanCoreEnrichments,
                "GetClusterAnnots" -> GetClusterAnnots,
                "GetPan"           -> GetPan,
                "test"             -> Test).map{ case (k,v) => (k.toLowerCase, v) }

  ///////////////////////////////////////////////////////////////////////////

  def main(args: Array[String]): Unit = {

    val (globalOpts, actOpts) = args.span(a => ! (ops contains a.toLowerCase))

    processGlobalOptions(globalOpts.toList)
    runAction(actOpts)
  }

  ///////////////////////////////////////////////////////////////////////////
    //Utils.setParallelismGlobally(5)
  @tailrec def processGlobalOptions(args: List[String]): Unit = {

    args match {
      case Nil => Unit
      case ("-d" | "--debug") :: tail => {
        Debug.enable
        Debug.message("Debug messages enabled")
        processGlobalOptions(tail)
      }
      case ("-s" | "--silent") :: tail => {
        Utils.disableMessages
        Debug.message("Silent mode entered")
        processGlobalOptions(tail)
      }
      case ("-t" | "--threads") :: value :: tail => {
        Utils.setParallelismGlobally(value.toInt)
        Debug.message("Set number of threads to %d".format(value.toInt))
        processGlobalOptions(tail)
      }
      case "--core-range" :: value1 :: value2 :: tail => {
        Debug.message("Setting core percentage between [%s,%s]".format(value1, value2))
        Clustering.setCoreRange(value1.toDouble, value2.toDouble)
        processGlobalOptions(tail)
      }
      case "--acc-range" :: value1 :: value2 :: tail => {
        Debug.message("Setting accessory percentage between [%s,%s]".format(value1, value2))
        Clustering.setAccRange(value1.toDouble, value2.toDouble)
        processGlobalOptions(tail)
      }
      case "--specific" :: value :: tail => {
        Debug.message("Setting specific to %s".format(value))
        Clustering.setSpecific(value.toDouble)
        processGlobalOptions(tail)
      }
      case "--annot-idfield" :: value :: tail => {
        Debug.message("Setting annotation idField to %s".format(value))
        Annotations.setIDField(value.toInt)
        processGlobalOptions(tail)
      }
      case "--annot-protfield" :: value :: tail => {
        Debug.message("Setting annotation protField to %s".format(value))
        Annotations.setProtField(value.toInt)
        processGlobalOptions(tail)
      }
      case "--annot-descfield" :: value :: tail => {
        Debug.message("Setting annotation descField to %s".format(value))
        Annotations.setDescField(value.toInt)
        processGlobalOptions(tail)
      }
      case "--alpha" :: value :: tail => {
        Debug.message("Setting alpha level to %s".format(value))
        Statistics.setAlpha(value.toDouble)
        processGlobalOptions(tail)
      }

      case "--tsne-perplexity" :: value ::tail => {
        Debug.message("Setting TSNE perplexity to %s".format(value))
        TSNEUtils.setPerplexity(value.toDouble)
        processGlobalOptions(tail)
      }
      case "--tsne-maxiter" :: value ::tail => {
        Debug.message("Setting TSNE maximum iterations to %s".format(value))
        TSNEUtils.setMaxIter(value.toInt)
        processGlobalOptions(tail)
      }

      case option :: tail => {
        Utils.error("Unknown Option '%s'".format(option))
        processGlobalOptions(tail)
      }
    }

  }

  ///////////////////////////////////////////////////////////////////////////

  def runAction(args: Array[String]) = {

    if (args.length < 1 || args(0).toLowerCase == "help" || !(ops contains args(0).toLowerCase)) {
      usage
    } else {

      val action = ops(args(0).toLowerCase)
      if ( args.length == 1  || args(1).toLowerCase == "help") {
        action.usage
      } else {
        action.main(args.slice(1, args.length))
       }
    }
    
  }

  ///////////////////////////////////////////////////////////////////////////

  def usage() = {

    println("Panalysis: Toolkit for pan genome analysis")
    println("")
    println("Usage: panalysis [global options] <action>")
    println("  Global options:")
    println("    -t|--threads <nthreads>: Number of threads to use (may not be used depending upon task, also doesn't really seem to work...)")
    println("    -d|--debug: Enable debug messages (Default: %s)".format(Debug.enabled.toString))
    println("    -s|--silent: Do not produce any output except normal program output")
    println("    --core-range <lower> <upper>: The percentage of genomes in a set the cluster must be in for it to be a core gene (Default: [%f,%f])".format(Clustering.coreRange.min, Clustering.coreRange.max))
    println("    --acc-range <lower> <upper>: The percentage of genomes in a set the cluster must be in for it to be a core gene (Default: [%f,%f])".format(Clustering.accRange.min, Clustering.accRange.max))
    println("    --specific <value>: The percentage of genomes within a group should have this cluster (Default %f)".format(Clustering.specific))
    println("    --annot-idfield <value>: When loading an annotation file, use column <value> as the annotation id (Default %d)".format(Annotations.idField))
    println("    --annot-protfield <value>: When loading an annotation file, use column <value> as the protein name (Default %d)".format(Annotations.protField))
    println("    --annot-descfield <value>: When loading an annotation file, use column <value> as the description field (Default %d)".format(Annotations.descField))
    println("    --alpha <value>: For statistical tests, use alpha level of <value> (Default: %f)".format(Statistics.alpha))
    println("    --tsne-perplexity <value>: Set TSNE perplexity to <value> (Default %f)".format(TSNEUtils.perplexity))
    println("    --tsne-maxiter <value>: Set the maximum number of TSNE iterations to run to <value> (Default %d)".format(TSNEUtils.maxIter))
    println("")
    println("Actions")
    ops.keys.toList.sorted.foreach{ k =>
      val action = ops(k)
      println("  %s%s%s -> %s".format(Console.BOLD, k, Console.RESET, action.description))
    }
  }

}

}
