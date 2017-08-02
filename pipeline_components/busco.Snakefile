rule busco_dataset:
  output:
    tgz  = "%s/dataset.tar.gz" % __BUSCO_OUTDIR__,
    db   = "%s/dataset" % __BUSCO_OUTDIR__
  params:
    db = tconfig["busco_database"]
  shell: """
    wget {params.db} -O {output.tgz}
    mkdir -p {output.db}
    tar -xf {output.tgz} --strip-components=1 -C {output.db}
  """

rule singleBusco:
  input:
    proteins = lambda wildcards: "%s/prots.%s.fa" % (__PROTS_OUTDIR__, wildcards.asm) ,
    db       = rules.busco_dataset.output.db
  output:
    summary = "%s/busco.{asm}.summary" % __BUSCO_OUTDIR__
  threads: 4
  conda: "%s/conda_envs/busco.yaml" % __PIPELINE_COMPONENTS__
  params:
    rule_outdir = __BUSCO_OUTDIR__
  shell: """
   cd {params.rule_outdir} && BUSCO -i {input.proteins} -f -m prot -l {input.db} -c {threads} -t busco_tmp.{wildcards.asm}.{wildcards.asm} -o busco.{wildcards.asm}
   ln -sf {params.rule_outdir}/run_busco.{wildcards.asm}/short_summary_busco.{wildcards.asm}.txt {output.summary}
  """

rule busco:
  input:
    summaries = expand("%s/busco.{asm}.summary" % __BUSCO_OUTDIR__, asm=config["data"].keys())
  output:
    summary = "%s/busco_summary.tsv" % __BUSCO_OUTDIR__
  run:
    import csv
    spFiles = zip(config["data"].keys(), input.summaries)
    with open(output.summary, "w") as ofd:
      ofd.write("#C: Complete BUSCOs (C)\n")
      ofd.write("#S: Complete and single-copy BUSCOs (S)\n")
      ofd.write("#D: Complete and duplicated BUSCOs (D)\n")
      ofd.write("#F: Fragmented BUSCOs (F)\n")
      ofd.write("#M: Missing BUSCOs (M)\n")
      ofd.write("#T: Total BUSCO groups searched\n")
      ofd.write("#species\tC\tS\tD\tF\tM\tT\n")
      for (species, file) in spFiles:
        ofd.write("%s" % species)
        with open(file, "r") as ifd:
          reader = csv.reader(ifd, delimiter='\t')
          for row in reader:
            if len(row) == 3:
              ofd.write("\t%s" % row[1])
            #fi
          #efor
        #ewith
        ofd.write("\n")
      #efor
