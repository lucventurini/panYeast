rule tsne_trans:
  input:
    protmap = rules.orthofinder_convert_panalysis_input.output.protmap,
    mci     = rules.orthofinder_convert_panalysis_input.output.mci_output
  output:
    tsne_count  = "%s/tsne.count.tsv" % __TSNE_OUTDIR__,
    tsne_binary = "%s/tsne.binary.tsv" % __TSNE_OUTDIR__
  params:
    install_dir = INSTALL_DIR
  threads: 2
  shell: """
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar tsne Binary {input.protmap} {input.mci} {output.tsne_binary}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar tsne Count  {input.protmap} {input.mci} {output.tsne_count}
  """

###############################################################################

rule tsne_plot:
  input:
    tsne_binary = rules.tsne_trans.output.tsne_binary,
    tsne_count  = rules.tsne_trans.output.tsne_count
  output:
    tsne_binary = "%s/tsne.binary.pdf" % __TSNE_OUTDIR__,
    tsne_count  = "%s/tsne.count.pdf" % __TSNE_OUTDIR__
  params:
    install_dir = INSTALL_DIR
  shell: """
    Rscript {params.install_dir}/panalysis/tsneR.R {input.tsne_binary} {output.tsne_binary}
    Rscript {params.install_dir}/panalysis/tsneR.R {input.tsne_count} {output.tsne_count}
  """
