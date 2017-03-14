###############################################################################
# BRAKER1                                                                     #
###############################################################################

# Generate a genome index for STAR

rule braker_align_gen_index:
  input:
    genome = lambda wildcards: config["dataprefix"] + "/" + config["data"][wildcards.asm]["asm"]
  output:
    index = "%s/aln.{asm}.idx" % __BRAKER_OUTDIR__
  params:
    rule_outdir = __BRAKER_OUTDIR__
  shell: """
    mkdir {params.rule_outdir}/aln.{wildcards.asm}.idx
    STAR --runMode genomeGenerate --genomeDir {output.index} --genomeFastaFiles {input.genome} 
  """

###############################################################################

# Align RNA-Seq reads to the genome using STAR

rule braker_align:
  input:
    rnaseq = lambda wildcards: [config["dataprefix"] + "/" + x for x in config["data"][wildcards.asm]["rnaseq"] ],
    index  = lambda wildcards: "%s/aln.%s.idx" % (__BRAKER_OUTDIR__, wildcards.asm)
  output:
    bam = "%s/aln.{asm}.bam" % __BRAKER_OUTDIR__
  threads: 4
  params:
    star_params = tconfig["star_params"],
    rule_outdir = __BRAKER_OUTDIR__
  shell: """
    STAR --twopassMode Basic --runThreadN {threads} {params.star_params} --genomeDir {input.index} --readFilesIn {input.rnaseq} --outFileNamePrefix {params.rule_outdir}/aln.{wildcards.asm}. --outSAMtype BAM SortedByCoordinate
    mv {params.rule_outdir}/aln.{wildcards.asm}.Aligned.sortedByCoord.out.bam {output.bam}
  """

###############################################################################

# Predict genes using the RNA-Seq pipeline

rule braker:
  input:
    genome = lambda wildcards: config["dataprefix"] + "/" + config["data"][wildcards.asm]["asm"],
    bam    = lambda wildcards: "%s/aln.%s.bam" % (__BRAKER_OUTDIR__, wildcards.asm)
  output:
    gtf_hints    = "%s/hints.{asm}.gff" % __BRAKER_OUTDIR__,
    gtf_genemark = "%s/genes.genemark.{asm}.gff" % __BRAKER_OUTDIR__,
    gtf_augustus = "%s/genes.augustus.{asm}.gff" % __BRAKER_OUTDIR__
  threads: 4
  params:
    rule_outdir = __BRAKER_OUTDIR__,
    braker_params = tconfig["braker_params"]
  shell: """
    mkdir -p {params.rule_outdir}/braker.{wildcards.asm}
    braker.pl --cores {threads} \
              --GENEMARK_PATH=`which gmes_petap.pl | rev | cut -d/ -f1 --complement | rev` \
              --BAMTOOLS_PATH=`which bamtools | rev | cut -d/ -f1 --complement | rev` \
              --genome={input.genome} \
              --bam={input.bam} \
              --gff3 \
              {params.braker_params} \
              --workingdir={params.rule_outdir}/braker.{wildcards.asm}
    ln -s {params.rule_outdir}/braker.{wildcards.asm}/hints.gff
  """
    
###############################################################################

# Rename the proteins that come out of the BRAKER prediction

rule braker_rename:
  input:
    gff = lambda wildcards: "%s/genes.augustus.%s.gff" % (__BRAKER_OUTDIR__, wildcards.asm)
  output:
    gff = "%s/genes.braker.{asm}.gff" % __BRAKER_OUTDIR__
  params:
    geneid_prefix = lambda wildcards: wildcards.asm
  shell: """
    sed -e "s/\([= ]\)\(g[0-9]\+\)/\\1{params.geneid_prefix}|\\2/g" {input.gff} > {output.gff}
  """

###############################################################################

# Extract the protein sequences that come from the BRAKER prediction

rule braker_gff2fasta:
  input:
    gff = lambda wildcards: "%s/genes.braker.%s.gff" % (__BRAKER_OUTDIR__, wildcards.asm)
  output:
    prot_fasta = "%s/braker.{asm}.prots.fa" % __BRAKER_OUTDIR__
  threads: 1
  benchmark: "%s/braker_gff2fasta.{asm}" % __LOGS_OUTDIR__
  shell: """
    cat {input.gff} \
     | grep "^# " \
     | tr -d '#' \
     | grep -v -e "[pP]redict" -e "----" -e "(none)" \
     | awk 'BEGIN{{ BUF=""; IN=0}}
           {{if(index($0,"start") != 0){{
              IN=1;
            }}
            if ( IN == 1){{
              BUF=BUF $0
              if( index($0, "end") != 0) {{
                print BUF
                BUF=""
              }}
            }}}}' \
     | sed -e 's/^[ ]*start gene \([^ ]\+\) protein sequence = \[\([A-Za-z ]\+\)\] end gene.*$/>\\1\\n\\2/' \
     | tr -d ' ' \
     | fold -w80 \
     > {output.prot_fasta}
  """

###############################################################################
  
