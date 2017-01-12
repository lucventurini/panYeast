rule orthagogue:
  input:
    aln = rules.diamond_align.output.aln
  output:
    mci = "%s/all.mci" % __ORTHAGOGUE_OUTDIR__
  threads: 8
  params:
    rule_outdir = __ORTHAGOGUE_OUTDIR__
  benchmark: "%s/orthagogue.log" % __LOGS_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}
    orthAgogue -i {input.aln} -O {params.rule_outdir} -c {threads} -e 6
  """
