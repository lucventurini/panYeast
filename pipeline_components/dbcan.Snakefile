rule dbcan_files:
  output:
    hmms = "%s/dbcan/hmms.txt" %__DBCAN_OUTDIR__,
    parser = "%s/dbcan/hmmscan-parser.sh" % __DBCAN_OUTDIR__,
    info   = "%s/dbcan/info.txt"
  conda:  "%s/conda_envs/dbcan.yaml" % __PIPELINE_COMPONENTS__
  shell: """
    wget http://csbl.bmb.uga.edu/dbCAN/download/dbCAN-fam-HMMs.txt -O {output.hmms}
    wget http://csbl.bmb.uga.edu/dbCAN/download/hmmscan-parser.sh -O {output.parser}
    wget http://csbl.bmb.uga.edu/dbCAN/download/FamInfo.txt -O {output.info}
    hmmpress {output.hmms}
    chmod +x {output.parser}
  """

rule dbcan_scan:
  input:
    db = rules.dbcan_files.output.hmms,
    parser = rules.dbcan_files.output.parser,
    prots = lambda wildcards: "%s/prots.%s.fa" % (__PROTS_OUTDIR__, wildcards.asm)
  output:
    hmmer_raw = "%s/hmmer_output.{asm}.dm" % __DBCAN_OUTDIR__,
  conda:  "%s/conda_envs/dbcan.yaml" % __PIPELINE_COMPONENTS__
  threads: 2
  shell: """
    hmmscan --cpu {threads} --domtblout {output.hmmer_raw} {input.db} {input.prots}
  """

rule dbscan_filter:
  input:
    parser = rules.dbcan_files.output.parser,
    hmmer_raw = lambda wildcards: "%s/hmmer_output.%s.dm" % (__DBCAN_OUTDIR__, wildcards.asm),
  output:
    cazyme_annots = "%s/cazyme_annots.{asm}.tsv" % (__DBCAN_OUTDIR__)
  threads: 1
  conda: "%s/conda_envs/dbcan.yaml" % __PIPELINE_COMPONENTS__
  params:
    evalue = tconfig["dbcan_evalue"],
    coverage = tconfig["dbcan_coverage"]
  shell: """
    {input.parser} {input.hmmer_raw} \
      | awk -F $'\t' 'BEGIN{{OFS = FS}}{{ if (($5 < {params.evalue}) && $10 > {params.coverage}){{print $0}}}}' \
      > {output.cazyme_annots}
  """

rule dbcan_grouped:
  input:
    cazyme_annots = lambda wildcards: "%s/cazyme_annots.%s.tsv" % (__DBCAN_OUTDIR__, wildcards.asm)
  output:
    cazymes = "%s/cazymes.{asm}.tsv" % (__DBCAN_OUTDIR__)
  shell: """
    cut -f1,3 {input.cazyme_annots} \
     | awk -F$'\t' '
       BEGIN{{
         split("",annots)
       }}
       {{
         if (!($2 in annots)){{
           annots[$2] = $1
         }} else {{
           annots[$2] = annots[$2] "," $1
         }}
       }}
       END{{
         for (gene in annots) {{
           print gene, annots[gene]
         }}
       }}' > {output.cazymes}
  """

rule dbcan_all:
  input:
    cazymes = expand("%s/cazymes.{asm}.tsv" % __DBCAN_OUTDIR__, asm=config["data"].keys())
  output:
    cazymes = "%s/all_cazymes.tsv" % __DBCAN_OUTDIR__
  shell: """
    cat {input.cazymes} > {output.cazymes}
  """
