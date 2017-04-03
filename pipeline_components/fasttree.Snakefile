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

###############################################################################

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

###############################################################################

rule outgroup_root_fasttree:
  input:
    tree = rules.fasttree_annotate_inodes.output.tree
  output:
    tree = "%s/rooted_tree.newick" % __FASTTREE_OUTDIR__
  params:
    install_dir = INSTALL_DIR,
    outgroup    = tconfig["outgroup_species"] if "outgroup_species" in tconfig else ""
  shell: """
   java -Xms20G -jar {params.install_dir}/panalysis/panalysis.jar reRootTree {input.tree} {params.outgroup} {output.tree} 
  """

###############################################################################

def fasttree_wrapper_input() :

  if "outgroup_species" in tconfig:
    return "%s/rooted_tree.newick" % __FASTTREE_OUTDIR__
  else:
    return "%s/species.inodes.tree" % __FASTTREE_OUTDIR__
#edef

rule fasttree_wrapper:
  input:
    tree = fasttree_wrapper_input()
  output:
    tree = "phylogeny.newick"
  shell: """
    ln -s {input.tree} {output.tree}
  """

###############################################################################

rule tree_names:
  output:
    name_map = "%s/name_map.tsv" % __FASTTREE_OUTDIR__
  run:
    with open(output.name_map, 'w') as fo:
      for asm in config["data"].keys():
        fo.write("%s\t%s\n" % (asm, config["data"][asm]["name"] if "name" in config["data"][asm] else asm))
