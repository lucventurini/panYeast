
###############################################################################

rule tsne_trans:
  input:
    protmap = rules.orthofinder.output.protmap,
    mci     = rules.orthofinder.output.mci_output
  output:
    tsne_count  = "%s/tsne/tsne.count.tsv" % __PANALYSIS_OUTDIR__,
    tsne_binary = "%s/tsne/tsne.binary.tsv" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  threads: 2
  shell: """
    mkdir -p {params.rule_outdir}/tsne
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar tsne Binary {input.protmap} {input.mci} {output.tsne_binary}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar tsne Count  {input.protmap} {input.mci} {output.tsne_count}
  """

###############################################################################

rule tsne_plot:
  input:
    tsne_binary = rules.tsne_trans.output.tsne_binary,
    tsne_count  = rules.tsne_trans.output.tsne_count
  output:
    tsne_binary = "%s/tsne/tsne.binary.pdf" % __PANALYSIS_OUTDIR__,
    tsne_count  = "%s/tsne/tsne.count.pdf" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR
  shell: """
    Rscript {params.install_dir}/panalysis/tsneR.R {input.tsne_binary} {output.tsne_binary}
    Rscript {params.install_dir}/panalysis/tsneR.R {input.tsne_count} {output.tsne_count}
  """

###############################################################################

rule pancore_tree:
  input:
    tree = rules.fasttree_annotate_inodes.output.tree,
    clust   = rules.orthofinder.output.mci_output,
    protmap = rules.orthofinder.output.protmap
  output:
    tree = "%s/tree/tree.newick" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar addpantotree {input.tree} {input.protmap} {input.clust} {output.tree} 
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar printTree {output.tree}
  """

rule panalysis:
  input:
    tsne    = rules.tsne_plot.output,
    tree    = rules.pancore_tree.output


