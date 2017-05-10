package panalysis {

import scala.io.Source
import java.io.{BufferedWriter, OutputStreamWriter, FileOutputStream}
import scala.collection.JavaConversions._

object TreePanCoreEnrichments extends ActionObject {

  override val description = "For each node in a tree, check if the core, accessory and specific gene clusters are enriched for some functions"

  override def main(args: Array[String]) = {
    println("test")
  }

  override def usage = {
    println("test")
  }

}

}
