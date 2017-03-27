rule fasttree:
  input:
    aln = rules.mergealignments_clean.output.aln
  output:
    tree = "%s/species.tree" % __FASTTREE_OUTDIR__
  params:
    rule_outdir = __FASTTREE_OUTDIR__,
    fasttree_params = tconfig["fasttree_params"]
  shell: """
    mkdir -p {params.rule_outdir}
    FastTree -nt {params.fasttree_params} < {input.aln} > {output.tree}
  """

rule fasttree_annotate_inodes:
  input:
    tree = rules.fasttree.output.tree
  output:
    tree = "%s/species.inodes.tree" % __FASTTREE_OUTDIR__
  shell: """
    cat {input.tree} \
     | tr -d '\n' \
     | sed -e 's/1.000/\\n/g' -e 's/;/\\n;/g' \
     | awk 'BEGIN{{n=1}}{{
       if (substr($0,1,1) == ":"){{
         print "INODE_" n $0;
         n++
       }} else if (substr($0,1,1) == ";") {{
         print "ROOT" $0
       }} else {{
         print $0
       }}
       }}' \
     | tr -d '\n' \
     > {output.tree}
  """
