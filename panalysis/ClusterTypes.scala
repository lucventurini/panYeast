package panalysis {

object ClusterTypes {

  class Cluster[T](id: Int, cluster: Array[T]) {

    override def toString = {
      "%d: %s".format(this.id, this.cluster.mkString(","))
    }

  }

  /////////////////////////////////////////////////////////////////////////////

  class ParaCluster[T](id: Int, cluster: Array[Array[T]], taxaIndexed: Boolean) {

    def isSingleCopy = {
      this.cluster.map(pc => pc.length <= 1).foldLeft(true)(_ & _)
    }

    ///////////////////////////////////////////////////////////////////////////

    def isCore(nTaxa: Int) = {
      if (this.taxaIndexed) {
        this.nNonEmptyTaxa == nTaxa
      } else {
        this.cluster.length == nTaxa
      }
    }

    ///////////////////////////////////////////////////////////////////////////

    def nNonEmptyTaxa = {
      this.cluster.filter(_.length > 0).length
    }

    ///////////////////////////////////////////////////////////////////////////

    def format(taxa: Array[String]) = {
      if (this.taxaIndexed) {
        "#cluster %d\n%s".format(this.id, taxa.zip(this.cluster).map{ case (t,c) => "%s: %s".format(t, c.mkString(", ")) }.mkString("\n"))
      } else {
        "#cluster %d\n%s".format(this.id, this.cluster.zipWithIndex.map{ case (c,t) => "%d: %s".format(t, c.mkString(", ")) }.mkString("\n"))
      }
    }

    ///////////////////////////////////////////////////////////////////////////

    override def toString = {
      "%d: %s".format(this.id, this.cluster.map(pc => pc.mkString(",")).mkString("\t"))
    }

  }

  /////////////////////////////////////////////////////////////////////////////

  case class ProteinCluster(id: Int, cluster: Array[Protein]) extends Cluster[Protein](id, cluster){

    def toInt = {
      IntCluster(this.id, this.cluster.map(p => p.uniqueID))
    }

    ///////////////////////////////////////////////////////////////////////////

    def toParaCluster: ProteinParaCluster = {
      val parray = this.cluster.foldLeft(Array[Array[Protein]]()){
        (arr, currProt) => {
          if (arr.length == 0) {
            Array(Array(currProt))
          } else {
            val lastProt = arr.last.last
            if (lastProt.taxa == currProt.taxa) {
              arr.dropRight(1) :+ (arr.last :+ currProt)
            } else {
              arr :+ Array(currProt)
            }
          }
        }
      }
      ProteinParaCluster(this.id, parray, false)
    }

    /////////////////////////////////////////////////////////////////////////////

    def overlap(c2: ProteinCluster) = {
      (this.cluster.map(p => p.toString).toSet & c2.cluster.map(p => p.toString).toSet).size
    }

    /////////////////////////////////////////////////////////////////////////////

    def fMeasureComponent(c2: ProteinCluster) = {
      (2.toDouble*this.overlap(c2).toDouble) / (c2.cluster.length + this.cluster.length).toDouble
    }

  }

  ///////////////////////////////////////////////////////////////////////////////

  case class ProteinParaCluster(id: Int, cluster: Array[Array[Protein]], taxaIndexed: Boolean) extends ParaCluster[Protein](id, cluster, taxaIndexed) {
    def apply(id: Int, cluster: Array[Array[Protein]]) = {
      ProteinParaCluster(id, cluster, false)
    }

    /////////////////////////////////////////////////////////////////////////////

    def toInt = {
      IntParaCluster(this.id, this.cluster.map(pc => pc.map(p => p.uniqueID)), this.taxaIndexed)
    }

    /////////////////////////////////////////////////////////////////////////////

    def indexByTaxa(taxa: Array[String]) = {
      this.taxaIndexed match {
        case false => {
          val taxaPC = this.cluster.map{ pc =>
            (pc(0).taxa, pc)
          }.toMap
          ProteinParaCluster(this.id, taxa.map( t => taxaPC.getOrElse(t, Array.empty[Protein])), true)
        }
        case true => {
          this
        }
      }
    }

    /////////////////////////////////////////////////////////////////////////////

    def nSubsetTaxa(taxa: Array[Int]): Int = {
      this.taxaIndexed match {
        case true  => taxa.filter(t => this.cluster(t).length > 0).length
        case false => -1
      }
    }

    /////////////////////////////////////////////////////////////////////////////

    def overlap(c2: ProteinParaCluster) = {
      this.cluster.indices.map{ i =>
        val ov = this.cluster(i).map(p => p.toString).toSet & c2.cluster(i).map(p => p.toString).toSet
        if (ov.size >= 1) 1 else 0
      }.foldLeft(0){case (a,b) => a+b}
    }

    /////////////////////////////////////////////////////////////////////////////

    def fMeasureComponent(c2: ProteinParaCluster) = {
      (2.toDouble*this.overlap(c2).toDouble) / (c2.nNonEmptyTaxa + this.nNonEmptyTaxa).toDouble
    }
  }

  ///////////////////////////////////////////////////////////////////////////////


  case class IntCluster(id: Int, cluster: Array[Int]) extends Cluster[Int](id, cluster) {
    def toProtein(protMap: ProtMap) = {
      ProteinCluster(this.id, this.cluster.map(p => protMap(p)))
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  case class IntParaCluster(id: Int, cluster: Array[Array[Int]], taxaIndexed: Boolean) extends ParaCluster[Int](id, cluster, taxaIndexed) {
    def apply(id: Int, cluster: Array[Array[Int]]) = {
      IntParaCluster(id, cluster, false)
    }

    /////////////////////////////////////////////////////////////////////////////

    def toProtein(protMap: ProtMap) = {
      ProteinParaCluster(this.id, this.cluster.map(pc => pc.map(p => protMap(p))), this.taxaIndexed)
    }
  }

}

}
