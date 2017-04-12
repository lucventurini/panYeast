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
      case ("-t" | "--threads") :: value :: tail => {
        Utils.setParallelismGlobally(value.toInt)
        Debug.message("Set number of threads to %d".format(value.toInt))
        processGlobalOptions(tail)
      }
      case option :: tail => {
        Utils.error("Unknown Option '%s'".format(option))
        processGlobalOptions(tail)
      }
    }

  }

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
    println("    -t|--threads <nthreads>: Number of threads to use (may not be used depending upon task)")
    println("    -d|--debug: Enable debug messages")
    println("")
    println("Actions")
    ops.keys.foreach{ k =>
      val action = ops(k)
      println("  %s%s%s -> %s".format(Console.BOLD, k, Console.RESET, action.description))
    }
  }

}

}
