
configfile: "config.json"

WORKDIR="__WORKDIR_REPLACE__"
INSTALL_DIR="__INSTALL_DIR_REPLACE__"

tconfig={

    # The field to use in an AA or NT fasta file as the identifier (default 2)
  "aa_idfield" : 2,
  "aa_field_delim" : "|",
  "nt_idfield" : 2,
  "nt_field_delim" : "|",

    # BRAKER OPTIONS
  "braker_params" : "-fungus --filterOutShort",
  "star_params"   : "--readFilesCommand zcat",

    # AUGUSTUS OPTIONS
  "augustus_species" : "saccharomyces_cerevisiae_S288C",
  "augustus_params"  : "",

    # DIAMOND OPTIONS (inside orthofinder)
  "diamond_k" : "%d" % 10,

  "orthofinder_params" : "--louvain"


  
}


include: "%s/pipeline.Snakefile" % INSTALL_DIR

