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

rule combine_annots:
  input:
    annots = expand("%s/annot.{asm}.tsv" % __INTERPROSCAN_OUTDIR__, asm=config["data"].keys())
  output:
    annots = "%s/all_annots.tsv" % __INTERPROSCAN_OUTDIR__
  shell: """
    cat {input.annots} > {output.annots}
  """

rule get_go_annots:
  input:
    annots = rules.combine_annots.output.annots
  output:
    goannots = "%s/go_annots.tsv" % __INTERPROSCAN_OUTDIR__
  shell: """
awk 'BEGIN{{ FS = "\t"}}
     {{n = split($14,goterms,"|")
      if (n > 0) {{
        for (goterm in goterms) {{
          printf "%s\t%s\t%s\\n", goterms[goterm], $1, $13
        }}
      }}
    }}' {input.annots} > {output.goannots}
  """

rule all_annots:
  input:
    annots = rules.combine_annots.output.annots
