###############################################################################

def trans_wrapper_input(wildcards):

  gff    = "gff" in config["data"][wildcards.asm]
  rnaseq = "rnaseq" in config["data"][wildcards.asm]
  aa     = "aa" in config["data"][wildcards.asm]
  nt     = "nt" in config["data"][wildcards.asm]

    # We are given pre-made aa and nt files... FIX THEM
  if (nt):
    return "%s/renamed_trans.%s.fa" % (__TRANS_OUTDIR__, wildcards.asm)
  else:
    return "%s/generated_trans.%s.fa" % (__TRANS_OUTDIR__, wildcards.asm)
  #fi
#edef

rule trans_wrapper:
  input:
    fa = lambda wildcards: trans_wrapper_input(wildcards)
  output:
    fa = "%s/transcripts.{asm}.fa" % __TRANS_OUTDIR__
  shell: """
    ln -s {input.fa} {output.fa}
  """

###############################################################################

rule given_trans:
  input:
    trans = lambda wildcards: (config["dataprefix"] + '/' + config["data"][wildcards.asm]["nt"])
  output:
    trans = "%s/renamed_trans.{asm}.fa" % __TRANS_OUTDIR__
  params:
    idfield     = tconfig["nt_idfield"],
    field_delim = tconfig["nt_field_delim"]
  threads: 1
  shell: """
    if [ `echo {input.trans} | rev | cut -d. -f1 | rev` == 'gz' ]; then
      zcat {input.trans}
    else
      cat {input.trans}
    fi \
    | awk -v org="{wildcards.asm}" -v idfield="{params.idfield}" -v field_delim="{params.field_delim}" '
      {{
        if (substr($0,1,1) == ">") {{
          split(substr($0,2),a,field_delim);
          print ">" org "|" a[idfield]
        }} else {{
          print $0
        }}
      }}' \
    > {output.trans}
  """

###############################################################################


rule gen_trans:
  input:
    gff = lambda wildcards: "%s/genes.%s.gff" % (__GFF_OUTDIR__, wildcards.asm),
    asm = lambda wildcards: "%s/asm.%s.fa" % (__GIVEN_ASM_OUTDIR__, wildcards.asm)
  output:
    trans_fasta = "%s/generated_trans.{asm}.fa" % __TRANS_OUTDIR__
  threads: 1
  shell: """
    gffread -w {output.trans_fasta}.pre -g {input.asm} {input.gff}
    awk '{{ if (substr($0,1,1) == ">") {{ split($0,a," "); print a[1]}} else {{ print $0 }}}}' {output.trans_fasta}.pre > {output.trans_fasta}
  """

###############################################################################

rule all_trans:
  input: expand("%s/transcripts.{asm}.fa" % __TRANS_OUTDIR__, asm=config["data"].keys())
