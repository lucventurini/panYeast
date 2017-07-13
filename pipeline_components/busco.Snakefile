rule busco_dataset:
  output:
    tgz  = "%s/dataset.tar.gz" % __BUSCO_OUTDIR__,
    db   = "%s/dataset" % __BUSCO_OUTDIR__
  params:
    db = tconfig["busco_database"]
  shell: """
    wget {params.db} -O {output.tgz}
    mkdir -p {output.dir}
    tar -xf {output.tgz} --strip-components=1 -C {output.dir}
  """

rule busco:
  input:
    proteins = lambda wildcards: "%s/prots.%s.fa" % (__PROTS_OUTDIR__, wildcards.asm) ,
    db       = lambda wildcards: "%s/dataset" % (__BUSCO_OUTDIR__)
  output:
    summary = "%s/busco.{asm}.summary" % __BUSCO_OUTDIR__
  threads: 4
  conda: "%s/conda_envs/busco.yaml" % __PIPELINE_COMPONENTS__
  params:
    rule_outdir = __BUSCO_OUTDIR__
  shell: """
   cd {params.rule_outdir} && run_busco -i {input.proteins} -f -m prot -l {input.db} -c {threads} -t busco_tmp.{wildcards.asm}.{wildcards.sample_id} -o busco.{wildcards.asm}.{wildcards.sample_id}
   ln -sf {params.rule_outdir}/run_busco.{wildcards.asm}.{wildcards.sample_id}/short_summary_busco.{wildcards.asm}.{wildcards.sample_id}.txt {output.summary}
  """

rule all_busco:
  input:
    summaries = expand("%s/busco.{asm}.summary" % __BUSCO_OUTDIR__, asm=config["data"].keys())
