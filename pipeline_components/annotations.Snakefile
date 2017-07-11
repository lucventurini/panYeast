
rule cp450_annots:
  input:
    ipr_annots = rules.combine_annots.output.annots
  output:
    cp450 = "%s/cp450.genes.tsv" % __ANNOTS_OUTDIR__
  shell: """
    cat {input.ipr_annots}  | awk -F$'\\t' 'BEGIN{{OFS=FS}}{{ if($12 == "IPR001128"){{print $0}}}}' > {output.cp450}
  """

rule orthogroup_cp450_annots:
  input:
    cp450_genes = rules.cp450_annots.output.cp450,
    cluster_genes = rules.get_cluster_genes.output.cluster_genes
  output:
    cp450 = "%s/cp450.orthogroups.tsv" % __ANNOTS_OUTDIR__
  params:
    awkscript = "%s/utils/group_annots_by_orthogroup.awk" % __PIPELINE_COMPONENTS__
  shell: """
    awk -F $'\\t' -f {params.awkscript} {input.cp450_genes} {input.cluster_genes} > {output.cp450}
  """

rule tf_annots:
  input:
    ipr_annots = rules.combine_annots.output.annots
  output:
    tf = "%s/tf.genes.tsv" % __ANNOTS_OUTDIR__
  shell: """
    awk -F$'\\t' '
     BEGIN{{
       OFS=FS
       split("",domains)
       split("IPR000005,IPR000007,IPR000079,IPR000116,IPR000135,IPR000524,IPR000637,IPR000679,IPR000747,IPR000792,IPR000818,IPR000835,IPR000843,IPR000910,IPR000967,IPR001005,IPR001092,IPR001138,IPR001222,IPR001289,IPR001356,IPR001367,IPR001387,IPR001471,IPR001510,IPR001594,IPR001606,IPR001766,IPR001827,IPR001878,IPR002059,IPR002100,IPR003120,IPR003150,IPR003163,IPR003316,IPR003350,IPR003654,IPR003656,IPR003657,IPR003893,IPR003956,IPR003958,IPR004022,IPR004181,IPR004827,IPR005011,IPR005398,IPR005735,IPR006455,IPR006565,IPR006600,IPR006642,IPR006856,IPR007087,IPR007103,IPR007104,IPR007106,IPR007107,IPR007196,IPR007219,IPR007396,IPR007604,IPR007738,IPR007888,IPR007889,IPR007898,IPR008895,IPR008967,IPR008994,IPR009044,IPR009057,IPR009071,IPR009395,IPR010499,IPR010666,IPR010770,IPR010982,IPR011616,IPR011700,IPR011991,IPR012346,IPR013129",doms,",")
       for(d in doms){{
         domains[doms[d]] = 1
       }}
     }}
     {{
       if($12 in domains){{
         print $0
       }}
     }}' {input.ipr_annots} > {output.tf}
  """

rule orthogroup_tf_annots:
  input:
    tf_genes = rules.tf_annots.output.tf,
    cluster_genes = rules.get_cluster_genes.output.cluster_genes
  output:
    tf = "%s/tf.orthogroups.tsv" % __ANNOTS_OUTDIR__
  params:
    awkscript = "%s/utils/group_annots_by_orthogroup.awk" % __PIPELINE_COMPONENTS__
  shell: """
    awk -F $'\\t' -f {params.awkscript} {input.tf_genes} {input.cluster_genes} > {output.tf}
  """

rule cazy_annots:
  input:
    cazy = rules.dbcan_all.output.cazymes,
  output:
    cazy = "%s/cazy.genes.tsv" % __ANNOTS_OUTDIR__
  shell: """
    ln -s {input.cazy} {output.cazy}
  """

rule cazy_groups_annots:
  input:
    cazy = rules.cazy_annots.output.cazy
  output:
    cazy = "%s/cazy_groups_summary.tsv" % __ANNOTS_OUTDIR__
  run:
    import csv
    counts = {}
    CG = set([])
    with open(input.cazy, "r") as ifd:
      reader = csv.reader(ifd, delimiter=" ")
      for row in reader:
        if len(row) < 2:
          continue
        #fi
        protein    = row[0].split("|")
        cazygroups = row[1].split(",")
        for cazygroup in cazygroups:
          CG.add(cazygroup)
          if protein[0] not in counts:
            counts[protein[0]] = {}
          #fi
          if cazygroup not in counts[protein[0]]:
            counts[protein[0]][cazygroup] = 0
          #fi
          counts[protein[0]][cazygroup] = counts[protein[0]][cazygroup] + 1
        #efor
      #efor
    #ewith
    with open(output.cazy, "w") as ofd:
      genomes = sorted(counts.keys())
      ofd.write("cazygroup\t%s\n" % '\t'.join(genomes))
      for cazygroup in sorted(CG):
        ofd.write("%s\t%s\n" % (cazygroup, '\t'.join([ str(counts[genome][cazygroup]) if cazygroup in counts[genome] else "0" for genome in genomes])))
      #efor
    #ewith
          

rule orthogroup_cazy_annots:
  input:
    cazy_genes = rules.dbcan_all.output.cazymes,
    cluster_genes = rules.get_cluster_genes.output.cluster_genes
  output:
    cazy = "%s/cazy.orthogroups.tsv" % __ANNOTS_OUTDIR__
  params:
    awkscript = "%s/utils/group_annots_by_orthogroup.awk" % __PIPELINE_COMPONENTS__
  shell: """
    awk -F $'\\t' -f {params.awkscript} {input.cazy_genes} {input.cluster_genes} > {output.cazy}
  """


def perform_annotations_input():
  annots_genes = {}
  annots_orthogroups = {}
  if tconfig["annot_cp450"]:
    annots_genes["cp450"]       = rules.cp450_annots.output.cp450
    annots_orthogroups["cp450"] = rules.orthogroup_cp450_annots.output.cp450
  if tconfig["annot_tf"]:
    annots_genes["tf"]       = rules.tf_annots.output.tf
    annots_orthogroups["tf"] = rules.orthogroup_tf_annots.output.tf
  if tconfig["annot_cazy"]:
    annots_genes["cazy"]       = rules.cazy_annots.output.cazy
    annots_orthogroups["cazy"] = rules.orthogroup_cazy_annots.output.cazy

  return (annots_genes, annots_orthogroups)

rule perform_annotations:
  input:
    annot_files = perform_annotations_input()[0].values()
    
