###############################################################################
# ORTHOFINDER                                                                 #
###############################################################################


###############################################################################
# Put all the protein sequences into the same folder

rule orthofinder_fasta_input:
  input:
    prot = lambda wildcards: "%s/prots.%s.fa" % (__PROTS_OUTDIR__, wildcards.asm)
  output:
    prot = "%s/input/{asm}.fa" % __ORTHOFINDER_OUTDIR__
  params:
    rule_outdir = "%s" % __ORTHOFINDER_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}/input
    ln -s {input.prot} {output.prot}
  """

###############################################################################
# Make the DIAMOND indexes for the diamond searches, and generate all diamond commands


orthofinder_blast_dir   = "%s/input/Results/WorkingDirectory/" % __ORTHOFINDER_OUTDIR__
orthofinder_results_dir = "%s/input/Results/" % __ORTHOFINDER_OUTDIR__

rule orthofinder_diamond_mkdb:
  input:
    prots = expand("%s/input/{asm}.fa" % __ORTHOFINDER_OUTDIR__, asm=config["data"].keys())
  output:
    diamond_cmds = "%s/diamond_cmds" % __ORTHOFINDER_OUTDIR__,
    sequenceids = "%s/input/Results/WorkingDirectory/SequenceIDs.txt"% __ORTHOFINDER_OUTDIR__,
    speciesids  = "%s/input/Results/WorkingDirectory/SpeciesIDs.txt"% __ORTHOFINDER_OUTDIR__
  params:
    rule_outdir = "%s" % __ORTHOFINDER_OUTDIR__,
    orthofinder_params = tconfig["orthofinder_blast_params"]
  shell: """
    dorthofinder.py -f {params.rule_outdir}/input {params.orthofinder_params} -op --constOut \
      | tee {params.rule_outdir}/dorthofinder.f.log \
      | grep "diamond blastp\|blastp -outfmt" \
      > {output.diamond_cmds}
  """

###############################################################################
# Run all the diamond commands... This may take some time

rule orthofinder_diamond:
  input:
    diamond_cmds = "%s/diamond_cmds" % __ORTHOFINDER_OUTDIR__,
  output:
    diamond_completed = "%s/diamond_cmds.completed" % __ORTHOFINDER_OUTDIR__,
  params:
    rule_outdir = "%s" % __ORTHOFINDER_OUTDIR__,
    basch = "%s/pipeline_components/utils/bascheduler.sh" % INSTALL_DIR
  threads: 20
  shell: """
    source {params.basch}
    baschf {input.diamond_cmds} {threads}
    cp {input.diamond_cmds} {output.diamond_completed}
  """

###############################################################################
# Run the MCL step of orthofinder... Had some problems with this...

rule orthofinder_mcl:
  input:
    diamond_completed = "%s/diamond_cmds.completed" % __ORTHOFINDER_OUTDIR__,
    sequenceids = rules.orthofinder_diamond_mkdb.output.sequenceids,
    speciesids  = rules.orthofinder_diamond_mkdb.output.speciesids
  output:
    mci_output = "%s/input/Results/WorkingDirectory/mci_output.mci" % __ORTHOFINDER_OUTDIR__,
  threads: 8
  params:
    blast_dir = "%s/input/Results/WorkingDirectory/" % __ORTHOFINDER_OUTDIR__,
    of_params = tconfig["orthofinder_mcl_params"] 
  shell: """
    dorthofinder.py -a {threads} -b {params.blast_dir} {params.of_params} -og --constOut
  """

###############################################################################
# Convert the output of orthofinder into the normal MCL output.
rule orthofinder:
  input:
    protmap    = rules.orthofinder_diamond_mkdb.output.sequenceids,
    mci_output = rules.orthofinder_mcl.output.mci_output
  output:
    protmap    = "%s/protmap.tsv" % __ORTHOFINDER_OUTDIR__,
    mci_output = "%s/orthofinder.mci" % __ORTHOFINDER_OUTDIR__
  shell: """
    cat {input.protmap} \
     | awk -F : '{{gsub(/^[ \t]+/,"",$2);print NR-1 "\t" $2}}' \
     > {output.protmap}
     cp {input.mci_output} {output.mci_output}
   """
