
configfile: "config.json"

WORKDIR="__WORKDIR_REPLACE__"
INSTALL_DIR="__INSTALL_DIR_REPLACE__"

tconfig={
  "augustus_species" : "saccharomyces_cerevisiae_S288C",
  "augustus_params"  : "",

  "diamond_params" : "-k %d --no-self-hits" % (int(1.5*len(config["data"].keys()))),

  "orthagogue_params" : "-t 0 -p 1 -s \'|\' -u",

  "mci_params" : "", #Same as Roary
}


include: "%s/pipeline.Snakefile" % INSTALL_DIR

