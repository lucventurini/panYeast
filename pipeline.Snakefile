

__PIPELINE_COMPONENTS__ = "%s/pipeline_components" % INSTALL_DIR
__RUN_DIR__             = "%s/run/" % WORKDIR


__AUGUSTUS_OUTDIR__   = "%s/augustus" % __RUN_DIR__
__BRAKER_OUTDIR__     = "%s/braker" % __RUN_DIR__
__GFF_OUTDIR__  = "%s/given_gff" % __RUN_DIR__
__GIVEN_ASM_OUTDIR__  = "%s/given_asm" % __RUN_DIR__
__PROTS_OUTDIR__      = "%s/prots" % __RUN_DIR__
__TRANS_OUTDIR__      = "%s/trans" % __RUN_DIR__

# PERHAPS REPLACE THESE THREE WITH ORTHOFINDER
#__DIAMOND_OUTDIR__    = "%s/diamond"  % __RUN_DIR__
#__ORTHAGOGUE_OUTDIR__ = "%s/orthagogue" % __RUN_DIR__
#__MCL_OUTDIR__        = "%s/mcl" % __RUN_DIR__


__ORTHOFINDER_OUTDIR__ = "%s/orthofinder" % __RUN_DIR__

__CLUSTALO_OUTDIR__  = "%s/clustalo" % __RUN_DIR__
__FASTTREE_OUTDIR__  = "%s/fasttree" % __RUN_DIR__

__PANALYSIS_OUTDIR__  = "%s/panalysis" % __RUN_DIR__


__EXTRACT_OUTDIR__ = "%s/extract" % __RUN_DIR__

__LOGS_OUTDIR__       = "%s/logs" % __RUN_DIR__

###############################################################################

__NOCASE__ = "/dev/null/nothing/ever/exists"

###############################################################################

# Undefined for now
# rule all:

###############################################################################

include: "%s/augustus.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/braker.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/gff.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/asm.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/prots.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/trans.Snakefile" % __PIPELINE_COMPONENTS__

#include: "%s/diamond.Snakefile" % __PIPELINE_COMPONENTS__
#include: "%s/orthagogue.Snakefile" %  __PIPELINE_COMPONENTS__
#include: "%s/mcl.Snakefile" % __PIPELINE_COMPONENTS__

include: "%s/orthofinder.Snakefile" % __PIPELINE_COMPONENTS__

include: "%s/clustalo.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/fasttree.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/panalysis.Snakefile" % __PIPELINE_COMPONENTS__


include: "%s/extract.Snakefile"% __PIPELINE_COMPONENTS__
