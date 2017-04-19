rule interproscan:
  input:
    prot = lambda wildcards: "%s/prots.%s.fa" % (__PROTS_OUTDIR__, wildcards.asm)
  output:
    annot = "%s/annot.{asm}.tsv" % __INTERPROSCAN_OUTDIR__
  shell: """
    cat {input.prot} \
    | tr -d '*' \
    > {output.annot}.input
    interproscan.sh -appl Pfam -f TSV --goterms --iprlookup -i {output.annot}.input -o {output.annot}
  """

rule all_annots:
  input:
    annots = expand("%s/annot.{asm}.tsv" % __INTERPROSCAN_OUTDIR__, asm=config["data"].keys())
