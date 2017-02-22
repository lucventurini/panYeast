rule panalysis_tsne:
  input:
    mcl  = lambda wildcards: "%s/mcl_%s.out" % (__MCL_OUTDIR__, wildcards.ival),
    pmap = rules.orthagogue.output.pmap
  output:
    tsne = "%s/panalysis.{tsneType}_{ival}.tsv" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR
  threads: 2
  shell: """
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar {wildcards.tsneType} {input.mcl} {input.pmap} {output.tsne}
  """

###############################################################################

rule panalysis_tsne_plot:
  input:
    tsne = lambda wildcards: "%s/panalysis.%s_%s.tsv" % (__PANALYSIS_OUTDIR__, wildcards.tsneType, wildcards.ival)
  output:
    tsnePDF = "%s/panalysis.{tsneType}_{ival}.pdf" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR
  shell: """
    Rscript {params.install_dir}/panalysis/tsneR.R {input.tsne} {output.tsnePDF}
  """


###############################################################################

rule panalysis_all:
  input:
    plots = expand("%s/panalysis.{tsneType}_{ival}.pdf" % __PANALYSIS_OUTDIR__, tsneType=["tsneBinary", "tsneCount"], ival=[1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9])


