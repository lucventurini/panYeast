rule merge_transcripts:
  input:
    trans = expand("%s/transcripts.{asm}.fa" % __TRANS_OUTDIR__, asm=config["data"].keys())
  output:
    trans = "%s/transcripts.fasta" % __CLUSTALO_OUTDIR__
  shell: """
    cat {input.trans} > {output.trans}
  """

###############################################################################

rule extract_orthagogue_clusters:
  input:
    clust   = rules.orthofinder.output.mci_output,
    protmap = rules.orthofinder.output.protmap,
    trans   = rules.merge_transcripts.output.trans
  output:
    list = "%s/clusters.list.tsv" % __CLUSTALO_OUTDIR__
  params:
    rule_outdir = __CLUSTALO_OUTDIR__,
    install_dir = INSTALL_DIR
  shell: """
    java -Xms20g -jar {params.install_dir}/panalysis/panalysis.jar getClusterFastas singlecopycore {input.trans} {input.protmap} {input.clust} {params.rule_outdir}/clusters
  """

rule extract_orthofinder_clusters:


###############################################################################

rule clustalo:
  input:
    list = "%s/clusters.list.tsv" % __CLUSTALO_OUTDIR__
  output:
    list = "%s/alignments.list.tsv" % __CLUSTALO_OUTDIR__
  threads: 20
  params:
    rule_outdir = __CLUSTALO_OUTDIR__,
    basch = "%s/pipeline_components/utils/bascheduler.sh" % INSTALL_DIR
  shell: """
    echo -en > {output.list}
    echo "" > {params.rule_outdir}/jobs_file
    mkdir -p {params.rule_outdir}/alignments
    comands=()
    source {params.basch}
    cat {input.list} | while read clusterline; do
      clusterid=`echo $clusterline | cut -d\  -f1`
      clusterfile=`echo $clusterline | cut -d\  -f3`
      alnfile="{params.rule_outdir}/alignments/alignment.$clusterid.fasta"
      echo "clustalo -i $clusterfile --threads 1 -o $alnfile; echo -e "$alnfile" >> {output.list}" >> {params.rule_outdir}/jobs_file
    done
    baschf {params.rule_outdir}/jobs_file {threads}
  """

###############################################################################

rule onelinealn:
  input:
    list = "%s/alignments.list.tsv" % __CLUSTALO_OUTDIR__
  output:
    list = "%s/alignments.oneline.list.tsv" % __CLUSTALO_OUTDIR__
  shell: """
    echo -en "" > {output.list}
    
    cat {input.list} | while read x; do
      echo $x
      newname=`echo $x | sed -e 's/[.]fasta$/.oneline&/'`
      cat $x | sed -e 's/>.*$/\t&\t/' | tr -d '\n' | tr '\t' '\n' | sed '/^\s*$/d' > $newname
      echo $newname >> {output.list}
    done
  """

###############################################################################

rule mergealignments:
  input:
    list = "%s/alignments.oneline.list.tsv" % __CLUSTALO_OUTDIR__
  output:
    mergedaln = "%s/alignments.fasta" % __CLUSTALO_OUTDIR__
  params:
    rule_outdir = __CLUSTALO_OUTDIR__
  shell: """
    rm -rf {output.mergedaln}
    touch {output.mergedaln}
    for f in `cat {input.list}`; do
      echo $f
      cat {output.mergedaln} \
        | paste - $f \
        > {params.rule_outdir}/temp;
      cp {params.rule_outdir}/temp {output.mergedaln}
    done;
    rm {params.rule_outdir}/temp
  """

###############################################################################

rule mergealignments_clean:
  input: 
    aln = rules.mergealignments.output.mergedaln
  output:
    aln = "%s/alignments.cleaned.fasta" % __CLUSTALO_OUTDIR__
  shell: """
  cat {input.aln} \
    | tr -d ' \t' \
    | sed -e 's/^>\([^|]\+\)|.*/>\\1/' \
    > {output.aln}
  """
