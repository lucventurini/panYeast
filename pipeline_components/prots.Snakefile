###############################################################################
# PREPARE PROTEIN SEQUENCES                                                   #
###############################################################################

###############################################################################

def prot_wrapper_input(wildcards):

  gff    = "gff" in config["data"][wildcards.asm]
  rnaseq = "rnaseq" in config["data"][wildcards.asm]
  aa     = "aa" in config["data"][wildcards.asm]
  nt     = "nt" in config["data"][wildcards.asm]

    # We are given pre-made aa and nt files... FIX THEM
  if (aa):
    return "%s/renamed_prots.%s.fa" % (__PROTS_OUTDIR__, wildcards.asm)
    # We don't have annotations, if we have RNA-Seq, use BRAKER, otherwise, use augustus
  if (not(gff) and rnaseq):
    return "%s/braker_prots.%s.fa" % (__PROTS_OUTDIR__, wildcards.asm)
  else:
    return "%s/augustus_prots.%s.fa" % (__PROTS_OUTDIR__, wildcards.asm)
  #fi
#edef


rule prots_wrapper:
  input:
    fa = lambda wildcards: prot_wrapper_input(wildcards)
  output:
    fa = "%s/prots.{asm}.fa" % __PROTS_OUTDIR__
  shell: """
    ln -s {input.fa} {output.fa}
  """

###############################################################################

rule given_prots:
  input:
    prot = lambda wildcards: (config["dataprefix"] + '/' + config["data"][wildcards.asm]["aa"])
  output:
    prot = "%s/renamed_prots.{asm}.fa" % __PROTS_OUTDIR__
  params:
    idfield     = tconfig["aa_idfield"],
    field_delim = tconfig["aa_field_delim"]
  threads: 1
  shell: """
    if [ `echo {input.prot} | rev | cut -d. -f1 | rev` == 'gz' ]; then
      zcat {input.prot}
    else
      cat {input.prot}
    fi \
    | awk -v org="{wildcards.asm}" -v idfield="{params.idfield}" -v field_delim="{params.field_delim}" '
      {{
        if (substr($0,1,1) == ">") {{
          split($0,a,field_delim); print ">" org "|" a[idfield]
        }} else {{
          print $0
        }}
      }}' \
    > {output.prot}
  """

###############################################################################

rule augustus_prots:
  input:
    prot_fasta = lambda wildcards: "%s/augustus.%s.prots.fa" % (__AUGUSTUS_OUTDIR__, wildcards.asm)
  output:
    prot_fasta = "%s/augustus_prots.{asm}.fa" % __PROTS_OUTDIR__
  threads: 1
  shell: """
    ln -s {input.prot_fasta} {output.prot_fasta}
  """

###############################################################################

rule braker1_prots:
  input:
    prot_fasta = lambda wildcards: "%s/braker.%s.prots.fa" % (__BRAKER_OUTDIR__, wildcards.asm)
  output:
    prot_fasta = "%s/braker_prots.{asm}.fa" % __PROTS_OUTDIR__
  threads: 1
  shell: """
    ln -s {input.prot_fasta} {output.prot_fasta}
  """
