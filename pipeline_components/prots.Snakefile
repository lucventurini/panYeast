###############################################################################

rule jgi_prots:
  input:
    jgi_prot = lambda wildcards: (config["dataprefix"] + '/' + config["data"][wildcards.asm]["aa"]) if ("aa" in config["data"][wildcards.asm]) else __NOCASE__
  output:
    prot = "%s/prots.{asm}.fa" % __PROTS_OUTDIR__
  params:
    idfield = 3 if config["status"] == "jgi" else 2
  threads: 1
  shell: """
    if [ `echo {input.jgi_prot} | rev | cut -d. -f1 | rev` == 'gz' ]; then
      zcat {input.jgi_prot}
    else
      cat {input.jgi_prot}
    fi \
    | awk -v org="{wildcards.asm}" -v idfield={params.idfield} '{{ if (substr($0,1,1) == ">") {{ split($0,a,"|"); print ">" org "|" a[idfield] }} else {{ print $0 }}}}' \
    > {output.prot}
  """

###############################################################################

rule augustus_prots:
  input:
    prot_fasta = lambda wildcards: ("%s/augustus.%s.prots.fa" % (__AUGUSTUS_OUTDIR__, wildcards.asm)) if not("aa" in config["data"][wildcards.asm]) else __NOCASE__
  output:
    prot_fasta = "%s/prots.{asm}.fa" % __PROTS_OUTDIR__
  threads: 1
  shell: """
    ln -s {input.prot_fasta} {output.prot_fasta}
  """

###############################################################################
