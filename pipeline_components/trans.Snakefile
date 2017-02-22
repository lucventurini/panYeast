###############################################################################

rule jgi_trans:
  input:
    jgi_trans = lambda wildcards: (config["dataprefix"] + '/' + config["data"][wildcards.asm]["nt"]) if ( "nt" in config["data"][wildcards.asm]) else __NOCASE__
  output:
    trans = "%s/transcripts.{asm}.fa" % __TRANS_OUTDIR__
  params:
    idfield = 3 if config["status"] == "jgi" else 2
  threads: 1
  shell: """
    if [ `echo {input.jgi_trans} | rev | cut -d. -f1 | rev` == 'gz' ]; then
      zcat {input.jgi_trans}
    else
      cat {input.jgi_trans}
    fi \
    | awk -v org="{wildcards.asm}" -v idfield={params.idfield} '{ if (substr($0,1,1) == ">") { split($0,a,"|"); print ">" org "|" a[idfield] } else { print $0 }}' \
    > {output.trans}
  """

###############################################################################

def gen_trans_gff(wildcards):
  if "gff" in config["data"][wildcards.asm]:
    return config["dataprefix"] + '/' + config["data"][wildcards.asm]["gff"]
  else:
    return "%s/augustus.%s.gff" % (__AUGUSTUS_OUTDIR__, wildcards.asm)

rule gen_trans:
  input:
    gff = lambda wildcards: gen_trans_gff(wildcards) if not( "nt" in config["data"][wildcards.asm]) else __NOCASE__,
    asm = lambda wildcards: config["dataprefix"] + '/' + config["data"][wildcards.asm]["asm"]
  output:
    trans_fasta = "%s/transcripts.{asm}.fa" % __TRANS_OUTDIR__
  threads: 1
  shell: """
    gffread -w {output.trans_fasta}.pre -g {input.asm} {input.gff}
    awk '{{ if (substr($0,1,1) == ">") {{ split($0,a," "); split(a[2],b,"="); print ">" b[2]}} else {{ print $0 }}}}' {output.trans_fasta}.pre > {output.trans_fasta}
  """

###############################################################################
