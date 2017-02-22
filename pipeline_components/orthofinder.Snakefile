
###############################################################################

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
    diamond_k = tconfig["diamond_k"]
  shell: """
    dorthofinder.py -f {params.rule_outdir}/input --diamond --max-target-seqs {params.diamond_k} -op --constOut \
      | tee {params.rule_outdir}/dorthofinder.f.log \
      | grep "diamond blastp" \
      > {output.diamond_cmds}
  """

###############################################################################

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

rule orthofinder_mcl:
  input:
    diamond_completed = "%s/diamond_cmds.completed" % __ORTHOFINDER_OUTDIR__,
    sequenceids = rules.orthofinder_diamond_mkdb.output.sequenceids,
    speciesids  = rules.orthofinder_diamond_mkdb.output.speciesids
  output:
    mci_output = "%s/input/Results/WorkingDirectory/mci_output.mci" % __ORTHOFINDER_OUTDIR__,
  threads: 8
  params:
    blast_dir = "%s/input/Results/WorkingDirectory/" % __ORTHOFINDER_OUTDIR__
  shell: """
    dorthofinder.py -a {threads} -b {params.blast_dir} -og --constOut
  """

###############################################################################

rule orthofinder_convert_panalysis_input:
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
