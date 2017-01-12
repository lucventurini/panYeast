rule merge_protein_files:
  input:
    fa = expand("%s/augustus.{asm}.prots.fa" % __AUGUSTUS_OUTDIR__, asm=config["data"].keys())
  output:
    fa = "%s/input_fasta.fa" % __DIAMOND_OUTDIR__
  params:
    rule_outdir = __DIAMOND_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}
    cat {input.fa} > {output.fa}
  """

rule diamond_makedb:
  input:
    fa = "%s/input_fasta.fa" % __DIAMOND_OUTDIR__
  output:
    db = "%s/db.dmnd" % __DIAMOND_OUTDIR__
  params:
    rule_outdir = __DIAMOND_OUTDIR__
  shell: """
    diamond makedb --in {input.fa} -d {params.rule_outdir}/db
  """

rule diamond_align:
  input:
    fa = "%s/input_fasta.fa" % __DIAMOND_OUTDIR__,
    db = "%s/db.dmnd" % __DIAMOND_OUTDIR__
  output:
    aln = "%s/diamond.m8" % __DIAMOND_OUTDIR__
  params:
    rule_outdir = __DIAMOND_OUTDIR__
  threads: 8
  benchmark: "%s/diamond_align.log" % __LOGS_OUTDIR__
  shell: """
    diamond blastp -d {params.rule_outdir}/db -q {input.fa} -o {output.aln}
  """
