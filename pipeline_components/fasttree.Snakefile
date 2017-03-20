rule fasttree:
  input:
    aln = rules.mergealignments.output.mergedaln
  output:
    tree = "%s/species.tree" % __FASTTREE_OUTDIR__
  params:
    fasttree_params = tconfig["fasttree_params"]
  shell: """
    FastTree -out {params.tree} -nt {input.aln} {params.fasttree_params}
  """
