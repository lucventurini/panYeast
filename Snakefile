
configfile: "config.json"

WORKDIR="__WORKDIR_REPLACE__"
INSTALL_DIR="__INSTALL_DIR_REPLACE__"

# User defined options
uconfig={

    # The field to use in an AA or NT fasta file as the identifier (default 2)
  "aa_idfield" : 2,
  "aa_field_delim" : "|",
  "nt_idfield" : 2,
  "nt_field_delim" : "|",

    # BRAKER OPTIONS
  "braker_params" : "-fungus --filterOutShort --alternatives-from-evidence=false",
  "star_params"   : "--readFilesCommand zcat",

    # AUGUSTUS OPTIONS
  "augustus_species" : "saccharomyces_cerevisiae_S288C",
  "augustus_params"  : "",

    # Orthofinder options
  "orthofinder_blast_params" : "--diamond --more-sensitive --max-target-seqs 10",
  "orthofinder_mcl_params"   : "--louvain --louvain-level 1",

    # Phylogenetic tree options
  #"outgroup_species : "", # Which organism to use as an outgroup?
  "fasttree_params" : "-fastest -gtr"

  
}


include: "%s/pipeline.Snakefile" % INSTALL_DIR

