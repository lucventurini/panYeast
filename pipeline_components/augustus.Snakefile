###############################################################################
# AUGUSTUS                                                                    #
###############################################################################
# Run Augustus and produce a gff file
rule augustus_gff:
  input:
    asm = lambda wildcards: "%s/asm.%s.fa" % (__GIVEN_ASM_OUTDIR__, wildcards.asm)
  output:
    gff = "%s/augustus_gff.{asm}.gff" % __AUGUSTUS_OUTDIR__
  threads: 4
  params:
    augustus_species = tconfig["augustus_species"],
    augustus_params  = tconfig["augustus_params"],
    rule_outdir = __AUGUSTUS_OUTDIR__
  benchmark: "%s/augustus_gff.{asm}" % __LOGS_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}
    augustus {params.augustus_params} \
             --gff3=on \
             --genemodel=complete \
             --strand=both  \
             --species={params.augustus_species} \
             {input.asm} \
      > {output.gff}
  """

###############################################################################
# Rename the proteins in the GFF file

rule augustus_gff_sample:
  input:
    gff = lambda wildcards: "%s/augustus_gff.%s.gff" % (__AUGUSTUS_OUTDIR__, wildcards.asm)
  output:
    gff = "%s/augustus.{asm}.gff" % __AUGUSTUS_OUTDIR__
  params:
    geneid_prefix = lambda wildcards: wildcards.asm
  shell: """
    sed -e "s/\([= ]\)\(g[0-9]\+\)/\\1{params.geneid_prefix}|\\2/g" {input.gff} > {output.gff}
  """

###############################################################################
# Extract the protein sequences from the GFF file

#rule augustus_gff2fasta:
#  input:
#    gff = lambda wildcards: "%s/augustus.%s.gff" % (__AUGUSTUS_OUTDIR__, wildcards.asm)
#  output:
#    prot_fasta = "%s/augustus.{asm}.prots.fa" % __AUGUSTUS_OUTDIR__
#  threads: 1
#  benchmark: "%s/augustus_gff2fasta.{asm}" % __LOGS_OUTDIR__
#  shell: """
#    cat {input.gff} \
#     | grep "^# " \
#     | tr -d '#' \
#     | grep -v -e "[pP]redict" -e "----" -e "(none)" \
#     | awk 'BEGIN{{ BUF=""; IN=0}}
#           {{if(index($0,"start") != 0){{ 
#              IN=1;
#            }}
#            if ( IN == 1){{
#              BUF=BUF $0
#              if( index($0, "end") != 0) {{
#                print BUF
#                BUF=""
#              }}
#            }}}}' \
#     | sed -e 's/^[ ]*start gene \([^ ]\+\) protein sequence = \[\([A-Za-z ]\+\)\] end gene.*$/>\\1\\n\\2/' \
#     | tr -d ' ' \
#     | fold -w80 \
#     > {output.prot_fasta}
#  """

###############################################################################
#Create protein sequence... Augustus gives wrong name by default
# This is admittedly a bit messy!!


rule augustus_prot_fasta:
  input:
    nt_fasta = lambda wildcards: "%s/transcripts.%s.fa" % (__TRANS_OUTDIR__, wildcards.asm)
  output:
    prot_fasta = "%s/augustus.{asm}.prots.fa" % __AUGUSTUS_OUTDIR__
  run:
    from utils import seq as seq
    with open(output.prot_fasta, 'w') as fo:
      for (fa_header, fa_seq) in zip(*seq.read_fasta(input.nt_fasta)):
        fo.write(">%s\n" % fa_header)
        fo.write("%s\n" % seq.nt_translate(fa_seq))
      #efor
    #ewith
#  shell: """
#    gffread -y {output.prot_fasta} -g {input.asm} {input.gff}
#  """
