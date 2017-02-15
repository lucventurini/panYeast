

__PIPELINE_COMPONENTS__ = "%s/pipeline_components" % INSTALL_DIR


__AUGUSTUS_OUTDIR__   = "%s/augustus" % WORKDIR
__PROTS_OUTDIR__      = "%s/prots" % WORKDIR
__DIAMOND_OUTDIR__    = "%s/diamond"  % WORKDIR
__ORTHAGOGUE_OUTDIR__ = "%s/orthagogue" % WORKDIR
__MCL_OUTDIR__        = "%s/mcl" % WORKDIR
__PANALYSIS_OUTDIR__  = "%s/panalysis" % WORKDIR

__LOGS_OUTDIR__       = "%s/logs" % WORKDIR

###############################################################################

__NOCASE__ = "/dev/null/nothing/ever/exists"

###############################################################################

rule all:
  input:
    cluster = expand("%s/mcl_{ival}.out" % __MCL_OUTDIR__, ival=[1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9])


###############################################################################

include: "%s/augustus.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/prots.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/diamond.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/orthagogue.Snakefile" %  __PIPELINE_COMPONENTS__
include: "%s/mcl.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/panalysis.Snakefile" % __PIPELINE_COMPONENTS__

