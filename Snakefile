
configfile: "config.json"

WORKDIR="run"
INSTALL_DIR="./"

tconfig={
  "augustus_species" : "saccharomyces_cerevisiae_S288C",
  "augustus_params"  : "",

  "mci_params" : "-I 1.5", #Same as Roary
}


include: "%s/pipeline.Snakefile" % INSTALL_DIR

