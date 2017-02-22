###############################################################################

rule diamond_makedb:
  input:
    fa = lambda wildcards: "%s/augustus.%s.prots.fa" % (__AUGUSTUS_OUTDIR__, wildcards.asm)
  output:
    db = "%s/db.{asm}.dmnd" % __DIAMOND_OUTDIR__
  params:
    rule_outdir = __DIAMOND_OUTDIR__
  shell: """
    mkdir {params.rule_outdir}
    diamond makedb --in {input.fa} -d {params.rule_outdir}/db.{wildcards.asm}
  """

###############################################################################

rule diamond_align:
  input:
    cmp = expand("%s/diamond.{asm}.m8" % __DIAMOND_OUTDIR__, asm=list(config["data"].keys())[:-1])
  output:
    cmp = "%s/diamond.m8" % __DIAMOND_OUTDIR__
  shell: """
    cat {input.cmp} > {output.cmp}
  """

###############################################################################

rule diamond_pairs:
  input:
    db  = lambda wildcards: "%s/db.%s.dmnd" % (__DIAMOND_OUTDIR__, wildcards.asm),
    cmp = lambda wildcards: expand("%s/cmp.%s.{asm2}.m8" % (__DIAMOND_OUTDIR__, wildcards.asm), asm2=list(config["data"].keys())[list(config["data"].keys()).index(wildcards.asm)+1:])
  output:
    cmp = "%s/diamond.{asm}.m8" % __DIAMOND_OUTDIR__
  shell: """
    cat{input.cmp} > {output.cmp}
  """

###############################################################################

rule diamond_pair:
  input:
    db   = lambda wildcards: "%s/db.%s.dmnd" % (__DIAMOND_OUTDIR__, wildcards.asm),
    asm2 = lambda wildcards: "%s/augustus.%s.prots.fa" % (__AUGUSTUS_OUTDIR__, wildcards.asm2)
  output:
    cmp = "%s/cmp.{asm}.{asm2}.m8" % __DIAMOND_OUTDIR__
  threads: 2
  params:
    rule_outdir = __DIAMOND_OUTDIR__,
    params = tconfig["diamond_params"]
  shell: """
    diamond blastp -p {threads} -d {params.rule_outdir}/db.{wildcards.asm} {params.params} -q {input.asm2} -o {output.cmp}
  """

