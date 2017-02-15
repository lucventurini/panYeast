rule merge_protein_files:
  input:
    fa = expand("%s/prots.{asm}.fa" % __PROTS_OUTDIR__, asm=config["data"].keys())
  output:
    fa = "%s/input_fasta.fa" % __DIAMOND_OUTDIR__
  params:
    rule_outdir = __DIAMOND_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}
    cat {input.fa} > {output.fa}
  """

rule diamond_makedb:
  input:
    fa = "%s/input_fasta.fa" % __DIAMOND_OUTDIR__
  output:
    db = "%s/db.dmnd" % __DIAMOND_OUTDIR__
  params:
    rule_outdir = __DIAMOND_OUTDIR__
  shell: """
    diamond makedb --in {input.fa} -d {params.rule_outdir}/db
  """

rule diamond_align:
  input:
    fa = "%s/input_fasta.fa" % __DIAMOND_OUTDIR__,
    db = "%s/db.dmnd" % __DIAMOND_OUTDIR__
  output:
    cmp = "%s/diamond.m8" % __DIAMOND_OUTDIR__
  params:
    rule_outdir = __DIAMOND_OUTDIR__,
    params = tconfig["diamond_params"]
  threads: 20
  benchmark: "%s/diamond_align.log" % __LOGS_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}/diamond_temp
    diamond blastp -d {params.rule_outdir}/db {params.params} -p {threads} -t {params.rule_outdir}/diamond_temp -q {input.fa} -o {output.cmp}
  """

## Remove hits that are in the same species
#rule diamond_filter:
#  input:
#    cmp = rules.diamond_align.output.cmp
#  output:
#    cmp = "%s/diamond.filt.m8" % __DIAMOND_OUTDIR__
#  shell: """
#    awk '{{ split($1, e1, "|");
#           split($2, e2, "|");
#           if (e1[1] != e2[1]) {{
#             print $0;
#           }}
#         }}' {input.cmp} > {output.cmp}
#  """
#
## Take only the first N hits
#rule diamond_trim:
#  input:
#    cmp = rules.diamond_filter.output.cmp
#  output:
#    cmp = "%s/diamond.filt.trim.m8" % __DIAMOND_OUTDIR__
#  params:
#    asm_count = len(config["data"].keys())
#  shell: """
#    awk 'BEGIN{{ pat="";
#                pat_count=0;
#         }}
#         {{ if(pat != $1) {{
#              pat = $1;
#              pat_count=1;
#            }} 
#            if (pat_count <= {params.asm_count}) {{
#              print $0;
#              pat_count+=1;
#            }}
#         }}' {input.cmp} > {output.cmp}
#  """
