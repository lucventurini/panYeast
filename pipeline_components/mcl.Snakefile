rule mcl:
  input:
    mci = rules.orthagogue.output.mci
  output:
    cluster = "%s/mcl_{ival}.out" % __MCL_OUTDIR__
  threads: 8
  params:
    ival        = lambda wildcards: float(wildcards.ival),
    rule_outdir = __MCL_OUTDIR__,
    mcl_params  = tconfig["mci_params"]
  shell: """
    mkdir -p {params.rule_outdir}
    mcl {input.mci} -te {threads} -I {params.ival} {params.mcl_params} -o {output.cluster}
  """
 
