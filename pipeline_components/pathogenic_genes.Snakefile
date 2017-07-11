def pathogenic_genes_wrapper():
  if len(tconfig["pathogenicity_databases"]) == 0:
    return "%s/skip_matches.tsv" % __PATHOGENIC_GENES_OUTPUT__
  else:
    return "%s/do_matches.tsv" % __PATHOGENIC_GENES_OUTPUT__

rule pathogenic_genes:
  input:
    matches = pathogenic_genes_wrapper()
  output:
    matches = "%s/matches.tsv" % __PATHOGENIC_GENES_OUTPUT__
  shell: """
    ln -s {input.matches} {output.matches}
  """

rule skip_pathogenic_genes:
  output:
    matches = "%s/skip_matches.tsv" % __PATHOGENIC_GENES_OUTPUT__
  shell: """
    touch {output.matches}
  """

rule search_pathogenicity:
  input:
    databases = tconfig["pathogenicity_databases"].values(),
    prots     = rules.all_prots.input
  output:
    matches = "%s/do_matches.tsv" % __PATHOGENIC_GENES_OUTPUT__
  threads: 20
  conda: "%s/conda_envs/orthofinder.yaml" % __PIPELINE_COMPONENTS__
  params:
    rule_outdir = __PATHOGENIC_GENES_OUTPUT__
  shell: """
    cat {input.databases} | tr ' \t' '_#' > "{params.rule_outdir}/pathogenic_db.fa"
    cat {input.prots} > "{params.rule_outdir}/allprots.fa"
    diamond makedb --in "{params.rule_outdir}/pathogenic_db.fa" -d "{params.rule_outdir}/pathogenic_db.dmnd"
    diamond blastp -d "{params.rule_outdir}/pathogenic_db.dmnd" -q "{params.rule_outdir}/allprots.fa" -o {output.matches} --evalue 1E-150 -p {threads}
  """

rule pathogenicity_cluster_annotations:
  input:
    cluster_genes_flat = rules.get_cluster_genes.output.cluster_genes_flat,
    pathogenic_genes   = rules.pathogenic_genes.output.matches,
    syntenic_cluster_annots = rules.iadhore_ortholog_clusters.output.annots
  output:
    gene_cluster_annots     = "%s/gene_cluster_annots.tsv" % __PATHOGENIC_GENES_OUTPUT__,
    ortholog_cluster_annots = "%s/ortholog_cluster_annots.tsv" % __PATHOGENIC_GENES_OUTPUT__,
    syntenic_cluster_annots = "%s/syntenic_cluster_annots.tsv" % __PATHOGENIC_GENES_OUTPUT__
  shell: """
    echo -e "#ClusterID\tAnnotationID\tNumberOfGenesWithAnnotation" > {output.ortholog_cluster_annots}
    join -t$'\t' -12 -21 -o1.1,1.2,2.2 <( sort -k2,2 {input.cluster_genes_flat}) <(cat {input.pathogenic_genes} | awk -F$'\t' 'BEGIN{{OFS=FS}}{{ if ($3 > 60){{ print $0}}}}' | sort -k3,3n | cut -f1,2 | sort -k1,1) \
    | tee {output.gene_cluster_annots} \
    | cut -f1,3 \
    | sort -k1,1n -k2,2 \
    | uniq -c \
    | sed -e 's/^[ \t]\+//' \
    | tr ' ' '\t' \
    | awk -F$'\t' 'BEGIN{{OFS=FS}}{{print $2,$3,$1}}' \
    >> {output.ortholog_cluster_annots}

    echo -e "#GeneID\tOrthologClusterID\tSyntenicCluster\tAnnotation" > {output.syntenic_cluster_annots}
    join -11 -22 -o1.1,1.2,1.3,2.3 -t$'\t' <( sort -k1,1 {input.syntenic_cluster_annots}) <(cat {output.gene_cluster_annots} | sort -k2,2) >> {output.syntenic_cluster_annots}
    
  """
