
configfile: "config.json"

WORKDIR="run"
INSTALL_DIR="./"

tconfig={
  "augustus_species" : "saccharomyces_cerevisiae_S288C",
  "augustus_params"  : "",

  "diamond_params" : "-k 300 --no-self-hits",

  "orthagogue_params" : "-t 0 -p 1 -s \'|\' -u",

  "mci_params" : "", #Same as Roary
}


include: "%s/pipeline.Snakefile" % INSTALL_DIR

