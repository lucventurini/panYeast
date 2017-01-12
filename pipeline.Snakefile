

__PIPELINE_COMPONENTS__ = "%s/pipeline_components" % INSTALL_DIR


__AUGUSTUS_OUTDIR__   = "%s/augustus" % WORKDIR
__DIAMOND_OUTDIR__    = "%s/diamond"  % WORKDIR
__ORTHAGOGUE_OUTDIR__ = "%s/orthagogue" % WORKDIR
__MCL_OUTDIR__        = "%s/mcl" % WORKDIR

__LOGS_OUTDIR__       = "%s/logs" % WORKDIR

###############################################################################

rule all:
  input:
    cluster = "%s/mcl.out" % __MCL_OUTDIR__


###############################################################################

include: "%s/augustus.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/diamond.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/orthagogue.Snakefile" %  __PIPELINE_COMPONENTS__
include: "%s/mcl.Snakefile" % __PIPELINE_COMPONENTS__
