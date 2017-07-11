rule distmat:
  input:
    distmats = rules.clustalo.output.distmat_list
  output:
    distmat = "%s/distmats.list" % __GENOMICISLANDS_OUTPUT__
  conda: "%s/conda_envs/genomiclands.yaml" % __PIPELINE_COMPONENTS__
  shell: """
    touch {output.distmat}
  """
