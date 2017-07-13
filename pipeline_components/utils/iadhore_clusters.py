#!/usr/bin/env python

from pyadhore import iadhore
import sys

multiplicons = sys.argv[1]
segments     = sys.argv[2]
output       = sys.argv[3]

multiplicons = "run/synteny/output/multiplicons.txt"
segments = "run/synteny/output/segments.txt"

data = iadhore.read(multiplicons, segments)
n_clust = len(data.multiplicon_graph)
leaves = data.get_multiplicon_leaves()
with open(output, "w") as ofd:
  for clust_id in range(1, n_clust+1):
    if not(data.get_multiplicon_properties(clust_id)["is_redundant"]):
      sp_genes = data.get_multiplicon_properties(clust_id)["segments"].values()
      genes    = [ g for gene_list in sp_genes for g in gene_list ]
      for g in genes:
        ofd.write("%d\t%s\n" % (clust_id, g))
      #efor
    #fi
  #efor
#ewith

