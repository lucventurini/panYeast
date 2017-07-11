###############################################################################

rule get_cluster_genes:
  input:
    protmap = rules.orthofinder.output.protmap,
    mci     = rules.orthofinder.output.mci_output
  output:
    cluster_genes       = "%s/genes/cluster_genes.tsv" % __PANALYSIS_OUTDIR__,
    cluster_genes_flat  = "%s/genes/cluster_genes.flat" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}/tsne
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar getClusterGenes {input.protmap} {input.mci} {output.cluster_genes}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar getClusterGenes {input.protmap} {input.mci} {output.cluster_genes_flat} flatFlag
  """

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

rule cluster_features:
  input:
    protmap = rules.orthofinder.output.protmap,
    mci     = rules.orthofinder.output.mci_output,
    annots  = rules.combine_annots.output.annots,
    tree    = rules.fasttree_wrapper.output.tree
  output:
    iscore      = "%s/tsne/iscore" % __PANALYSIS_OUTDIR__,
    issc        = "%s/tsne/issinglecopy" % __PANALYSIS_OUTDIR__,
    ngenes      = "%s/tsne/ngenes" % __PANALYSIS_OUTDIR__,
    nspecies    = "%s/tsne/nspecies" % __PANALYSIS_OUTDIR__,
    annotscores = "%s/tsne/annotscores" % __PANALYSIS_OUTDIR__,
    nfunctions  = "%s/tsne/nfunctions" % __PANALYSIS_OUTDIR__,
    nannotgenes = "%s/tsne/nannotgenes" % __PANALYSIS_OUTDIR__,
    coretree    = "%s/tsne/coretree" % __PANALYSIS_OUTDIR__,
    specifictree = "%s/tsne/specifictree" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  threads: 2
  shell: """
    mkdir -p {params.rule_outdir}/tsne
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 GetClusterFeatures {input.protmap} {input.mci} {output.iscore} isCore
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 GetClusterFeatures {input.protmap} {input.mci} {output.issc} isSingleCopy
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 GetClusterFeatures {input.protmap} {input.mci} {output.ngenes} nGenes
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 GetClusterFeatures {input.protmap} {input.mci} {output.nspecies} nSpecies
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 GetClusterFeatures {input.protmap} {input.mci} {output.annotscores} annotScores {input.annots}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 GetClusterFeatures {input.protmap} {input.mci} {output.nfunctions} nFunctions {input.annots}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 GetClusterFeatures {input.protmap} {input.mci} {output.nannotgenes} nAnnotGenes {input.annots}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 GetClusterFeatures {input.protmap} {input.mci} {output.coretree} coreNode {input.tree}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 GetClusterFeatures {input.protmap} {input.mci} {output.specifictree} specificNode {input.tree}
  """


###############################################################################

rule tsne_plot:
  input:
    tsne_binary = rules.tsne_trans.output.tsne_binary,
    tsne_count  = rules.tsne_trans.output.tsne_count,
    features    = rules.cluster_features.output
  output:
    tsne_binary = "%s/tsne/tsne.binary.pdf" % __PANALYSIS_OUTDIR__,
    tsne_count  = "%s/tsne/tsne.count.pdf" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR
  conda: "%s/conda_envs/r.yaml"% __PIPELINE_COMPONENTS__
  shell: """
    Rscript {params.install_dir}/panalysis/plotScripts/tsneR.R raw {input.tsne_binary} {output.tsne_binary}
    Rscript {params.install_dir}/panalysis/plotScripts/tsneR.R raw {input.tsne_count} {output.tsne_count}

    Rscript {params.install_dir}/panalysis/plotScripts/tsneR.R intensity {input.tsne_binary} {output.tsne_binary}.nGenes.pdf {rules.cluster_features.output.ngenes}
    Rscript {params.install_dir}/panalysis/plotScripts/tsneR.R intensity {input.tsne_binary} {output.tsne_binary}.nSpecies.pdf {rules.cluster_features.output.nspecies}
    Rscript {params.install_dir}/panalysis/plotScripts/tsneR.R intensity {input.tsne_binary} {output.tsne_binary}.annotScores.pdf {rules.cluster_features.output.annotscores}

    Rscript {params.install_dir}/panalysis/plotScripts/tsneR.R labels {input.tsne_binary} {output.tsne_binary}.isCore.pdf {rules.cluster_features.output.iscore}
    Rscript {params.install_dir}/panalysis/plotScripts/tsneR.R labels {input.tsne_binary} {output.tsne_binary}.isSingleCopy.pdf {rules.cluster_features.output.issc}
    Rscript {params.install_dir}/panalysis/plotScripts/tsneR.R labels {input.tsne_binary} {output.tsne_binary}.coreNode.pdf {rules.cluster_features.output.coretree}
    Rscript {params.install_dir}/panalysis/plotScripts/tsneR.R labels {input.tsne_binary} {output.tsne_binary}.specificNode.pdf {rules.cluster_features.output.specifictree}
  """

###############################################################################

rule cluster_plots:
  input:
    annotscores = rules.cluster_features.output.annotscores,
    nfunctions  = rules.cluster_features.output.nfunctions,
    ngenes      = rules.cluster_features.output.ngenes
  output:
    annotscores_vs_nfunctions = "%s/plots/annotscores_nfunctions.pdf" % __PANALYSIS_OUTDIR__,
    annotscores_vs_ngenes     = "%s/plots/annotscores_ngenes.pdf" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  conda: "%s/conda_envs/r.yaml"% __PIPELINE_COMPONENTS__
  shell: """
    mkdir -p {params.rule_outdir}
    Rscript {params.install_dir}/panalysis/plotScripts/scatterplot.R plain {input.annotscores} {input.nfunctions} {output.annotscores_vs_nfunctions} "Score" "Number of Functions"
    Rscript {params.install_dir}/panalysis/plotScripts/scatterplot.R hexbin {input.annotscores} {input.nfunctions} {output.annotscores_vs_nfunctions}.hexbin.pdf "Score" "Number of Functions"

    Rscript {params.install_dir}/panalysis/plotScripts/scatterplot.R plain {input.annotscores} {input.ngenes} {output.annotscores_vs_ngenes} "Score" "Number of Genes"
    Rscript {params.install_dir}/panalysis/plotScripts/scatterplot.R hexbin {input.annotscores} {input.ngenes} {output.annotscores_vs_ngenes}.hexbin.pdf "Score" "Number of Genes"

  """

###############################################################################

rule pancore_tree:
  input:
    tree = rules.fasttree_wrapper.output.tree,
    clust   = rules.orthofinder.output.mci_output,
    protmap = rules.orthofinder.output.protmap,
    tree_names = rules.tree_names.output.name_map
  output:
    tree = "%s/tree/pantree.newick" % __PANALYSIS_OUTDIR__,
    tree_named = "%s/tree/pantree_names.newick" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  threads: 20
  shell: """
    mkdir -p {params.rule_outdir}/tree
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar addpantotree {input.tree} {input.protmap} {input.clust} {output.tree}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar changeNodeNamesTree {output.tree} {input.tree_names} {output.tree_named}
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar printTree {output.tree_named}
    
  """

###############################################################################

rule getpantree_clusters:
  input:
    tree = rules.fasttree_wrapper.output.tree,
    clust   = rules.orthofinder.output.mci_output,
    protmap = rules.orthofinder.output.protmap,
    tree_names = rules.tree_names.output.name_map
  output:
    pancore_clusters = "%s/pancore/tree_clusters" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  threads: 20
  shell: """
    mkdir -p {params.rule_outdir}/pancore
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar GetPanTree {input.tree} {input.protmap} {input.clust} {output.pancore_clusters}
  """

###############################################################################

rule self_cmp_clust:
  input:
    clust   = rules.orthofinder.output.mci_output,
    protmap = rules.orthofinder.output.protmap
  output:
    f_file = '%s/fmeasure/fmeasure_clust' % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  threads: 5
  shell: """
    mkdir -p {params.rule_outdir}/fmeasure
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar cmpClust clust {input.clust} {input.protmap} {input.clust} {input.protmap} | tee {output.f_file}
  """

###############################################################################

rule self_cmp_paraclust:
  input:
    clust   = rules.orthofinder.output.mci_output,
    protmap = rules.orthofinder.output.protmap
  output:
    f_file = '%s/fmeasure/fmeasure_paraclust' % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  threads: 5
  shell: """
    mkdir -p {params.rule_outdir}/fmeasure
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar cmpClust paraClust {input.clust} {input.protmap} {input.clust} {input.protmap} | tee {output.f_file}
  """

###############################################################################

rule validate_clusters:
  input:
    clust   = rules.orthofinder.output.mci_output,
    protmap = rules.orthofinder.output.protmap,
    annots  = rules.combine_annots.output.annots
  output:
    scores = "%s/annotations/validationScores" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}/annotations
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 ValidateClustersWithAnnots {input.protmap} {input.clust} {input.annots} {output.scores}
  """

###############################################################################  

rule validate_clusters_plot:
  input:
    scores = rules.validate_clusters.output.scores
  output:
    plot = "%s/annotation_validation/scores.pdf"
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  shell: """
    touch {output.plot}
  """

###############################################################################

rule get_cluster_annotations:
  input:
    clust   = rules.orthofinder.output.mci_output,
    protmap = rules.orthofinder.output.protmap,
    annots  = rules.combine_annots.output.annots
  output:
    annots = "%s/annotations/cluster_annotations.tsv" % __PANALYSIS_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    rule_outdir = __PANALYSIS_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}/annotations
    java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar --annot-idfield 4 --annot-protfield 0 --annot-descfield 5 getclusterAnnots {input.protmap} {input.clust} {input.annots} {output.annots}
  """

###############################################################################

rule panalysis:
  input:
    tsne      = rules.tsne_plot.output,
    tree      = rules.pancore_tree.output,
    fmeas_c   = rules.self_cmp_clust.output,
    fmeas_pc  = rules.self_cmp_paraclust.output,
    pancore   = rules.getpantree_clusters.output,
    clust_val = rules.validate_clusters_plot.output


