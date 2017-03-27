# If we get a GFF file as part of the original definition

rule rename_given_gff:
  input:
    gff = lambda wildcards: config["dataprefix"] + '/' + config["data"][wildcards.asm]["gff"]
  output:
    gff = "%s/renamed.{asm}.gff" % (__GIVEN_GFF_OUTDIR__)
  params:
    geneid_prefix = lambda wildcards: wildcards.asm
  shell:"""
    if [ `echo {input.gff} | rev | cut -d. -f1 | rev` == 'gz' ]; then
      zcat {input.gff}
    else
      cat {input.gff}
    fi | sed -e "s/\([= ]\)\([0-9]\+\)/\\1{params.geneid_prefix}|\\2/g"  {input.gff} > {output.gff}
  """
