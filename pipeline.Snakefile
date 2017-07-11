
###############################################################################
#  Tool Config
###############################################################################

  # Default parameters defined in the pipeline
tconfig={

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
  "orthofinder_blast_params" : "--diamond --more-sensitive",
  "orthofinder_mcl_params"   : "--louvain --louvain-level 1",

    # Phylogenetic tree options
  #"outgroup_species : "", # Which organism to use as an outgroup?
  "fasttree_params" : "-fastest -gtr",

  "pathogenicity_databases" : { "VFDB": "/home/thiesgehrmann/data/datasets/VFDB/VFDB_setB_pro.fas",
                               "PHIbase": "/home/thiesgehrmann/data/datasets/PHIbase/phi_accessions.fa"},

  "dbcan_evalue" : "1e-17",
  "dbcan_coverage" : "0.45",

  "annot_cp450" : True,
  "annot_tf" : True,
  "annot_sec_met" : False,
  "annot_cazy" : True,
  
}

  # Update tconfig with the values defined in the user configuration
if( "uconfig" in vars() ):
  tconfig.update(uconfig)

###############################################################################
# Output directories

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

__INTERPROSCAN_OUTDIR__ = "%s/interproscan" % __RUN_DIR__
__DBCAN_OUTDIR__        = "%s/dbcan" % __RUN_DIR__
__ANNOTS_OUTDIR__       = "%s/annots" % __RUN_DIR__

__ORTHOFINDER_OUTDIR__ = "%s/orthofinder" % __RUN_DIR__

__SYNTENY_OUTDIR__ = "%s/synteny" % __RUN_DIR__

__PATHOGENIC_GENES_OUTPUT__ = "%s/pathogenic_genes" % __RUN_DIR__

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
include: "%s/interproscan.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/dbcan.Snakefile" % __PIPELINE_COMPONENTS__

#include: "%s/diamond.Snakefile" % __PIPELINE_COMPONENTS__
#include: "%s/orthagogue.Snakefile" %  __PIPELINE_COMPONENTS__
#include: "%s/mcl.Snakefile" % __PIPELINE_COMPONENTS__

include: "%s/orthofinder.Snakefile" % __PIPELINE_COMPONENTS__

include: "%s/clustalo.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/fasttree.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/panalysis.Snakefile" % __PIPELINE_COMPONENTS__

include: "%s/annotations.Snakefile" % __PIPELINE_COMPONENTS__

include: "%s/synteny.Snakefile" % __PIPELINE_COMPONENTS__

include: "%s/pathogenic_genes.Snakefile" % __PIPELINE_COMPONENTS__
include: "%s/extract.Snakefile"% __PIPELINE_COMPONENTS__
