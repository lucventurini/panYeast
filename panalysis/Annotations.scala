package panalysis {

  case class AnnotationEntry(id: String, description: String) {
    override def toString = { "%s: %s".format(this.id, this.description) }

    def ==(that: AnnotationEntry) = { this.id == that.id }

    def equals(that: AnnotationEntry) = { this == that }

    override def hashCode = this.id.hashCode
  }

  class Annotations(annotations: Array[(Int,Protein,AnnotationEntry)])  {

      // Group by protein
    val gp = annotations.groupBy(_._2.toString)
      // Index annotations by protein name
    val pa = gp.map{ case (k,v) => (k.toString, v.map(_._3).distinct) }
      // Index annotations by proteinID
    val ia = gp.map{ case (k,v) => (v(0)._1, v.map( _._3).distinct) }

      // group by annotation
    val ga = annotations.groupBy(_._3)
      // Index protein names by annotations
    val ap = ga.map{ case (k,v) => (k, v.map(_._2).distinct) }
      // Index proteinIDs by annotation
    val ai = ga.map{ case (k,v) => (k, v.map(_._1).distinct) }

    val nAnnotatedGenes = pa.keys.size
    val nAnnotations    = ap.keys.size

  }

  /////////////////////////////////////////////////////////////////////////////

  object Annotations {

    var idField   = 0
    var protField = 1
    var descField = 2
    var fieldSep  = "\t"

    ///////////////////////////////////////////////////////////////////////////

    def setIDField(value: Int) = { this.idField = value }
    def setProtField(value: Int) = { this.protField = value }
    def setDescField(value: Int) = { this.descField = value }
    def setFieldSep(value: String) = { this.fieldSep = value }

    ///////////////////////////////////////////////////////////////////////////

    def apply(annotationFile: String, protMap: ProtMap) = {
      new Annotations(this.readFile(annotationFile, protMap))
    }

    ///////////////////////////////////////////////////////////////////////////

    def readFile(annotationFile: String, protMap: ProtMap) = {
      val minFields = math.max(math.max(this.idField, this.protField), this.descField)
      io.Source.fromFile(annotationFile).getLines.filter(line => line(0) != '#').map(_.split(this.fieldSep)).filter(_.length > minFields).map{ fields =>
        val protein = Protein(fields(this.protField))
        val annot   = AnnotationEntry(fields(this.idField), fields(this.descField))
        (protMap.inverse(protein), protein, annot)
      }.toArray
    }

    ///////////////////////////////////////////////////////////////////////////

  }

}
