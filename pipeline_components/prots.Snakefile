###############################################################################

rule jgi_prots:
  input:
    jgi_prot = lambda wildcards: (config["dataprefix"] + '/' + config["data"][wildcards.asm]["aa"]) if config["status"] == "jgi" else __NOCASE__
  output:
    prot = "%s/prots.{asm}.fa" % __PROTS_OUTDIR__
  threads: 1
  shell: """
    if [ `echo {input.jgi_prot} | rev | cut -d. -f1 | rev` == 'gz' ]; then
      zcat {input.jgi_prot}
    else
      cat {input.jgi_prot}
    fi \
    | sed -e 's/^>[^|]\+|[^|]\+|\([0-9]\+\)|.*$/>{wildcards.asm}|g\\1/' \
    > {output.prot}
  """

###############################################################################

rule augustus_prots:
  input:
    prot_fasta = lambda wildcards: ("%s/augustus.%s.prots.fa" % (__AUGUSTUS_OUTDIR__, wildcards.asm)) if config["status"] == "raw" else __NOCASE__
  output:
    prot_fasta = "%s/prots.{asm}.fa" % __PROTS_OUTDIR__
  threads: 1
  shell: """
    ln -s {input.prot_fasta} {output.prot_fasta}
  """

###############################################################################
