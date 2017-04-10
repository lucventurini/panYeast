rule interproscan:
  input:
    prot = lambda wildcards: "%s/prots.%s.fa" % wildcards.asm
  output:
    annot = "%s/annot.{asm}.tsv" % __INTERPROSCAN_OUTDIR__
  shell: """
    interproscan.sh -appl Pfam -f TSV --goterms --iprlookup -i {input.prot} -o {output.annot}
  """

rule all_annots:
  input:
    annots = expand("%s/annot.{asm}.tsv", asm=config["data"].keys())
