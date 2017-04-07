# If we get a GFF file as part of the original definition

def gff_wrapper_input(wildcards):
  if "gff" in config["data"][wildcards.asm]:
    return "%s/renamed.%s.gff" % (__GFF_OUTDIR__, wildcards.asm)
  if ("rnaseq" in config["data"][wildcards.asm]):
    return "%s/genes.braker.%s.gff" % (__BRAKER_OUTDIR__, wildcards.asm)
  else:
    return "%s/augustus.%s.gff" % (__AUGUSTUS_OUTDIR__, wildcards.asm)
  #fi
 #edef

rule gff_wrapper:
  input:
    gff = lambda wildcards: gff_wrapper_input(wildcards)
  output:
    gff = "%s/genes.{asm}.gff" % __GFF_OUTDIR__
  shell: """
    ln -sf {input.gff} {output.gff}
  """

rule rename_given_gff:
  input:
    gff = lambda wildcards: config["dataprefix"] + '/' + config["data"][wildcards.asm]["gff"]
  output:
    gff = "%s/renamed.{asm}.gff" % (__GFF_OUTDIR__)
  params:
    geneid_prefix = lambda wildcards: wildcards.asm
  shell:"""
    if [ `echo {input.gff} | rev | cut -d. -f1 | rev` == 'gz' ]; then
      zcat {input.gff}
    else
      cat {input.gff}
    fi | sed -e "s/\(\(ID\|Parent\)[=]\)\([^;]\+\)/\\1{params.geneid_prefix}|\\3/g"  > {output.gff}
  """


rule all_gffs:
  input: expand("%s/genes.{asm}.gff" % __GFF_OUTDIR__, asm=config["data"].keys())

