# If we get a GFF file as part of the original definition

rule rename_given_gff:
  input:
    gff = lambda wilcards: config["dataprefix"] + '/' + config["data"][wildcards.asm]["gff"]
  output:
    gff = "%s/renamed.{asm}.gff" % (__GIVEN_GFF_OUTDIR__)
  params:
    geneid_prefix = lambda wildcards: wildcards.asm
  shell:"""
    sed -e "s/\([= ]\)\([0-9]\+\)/\\1{params.geneid_prefix}|\\2/g"  {input.gff} > {output.gff}
  """
