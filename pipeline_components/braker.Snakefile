###############################################################################
# BRAKER1                                                                     #
###############################################################################

# Generate a genome index for STAR

rule braker_align_gen_index:
  input:
    asm = lambda wildcards: "%s/asm.%s.fa" % (__GIVEN_ASM_OUTDIR__, wildcards.asm),
  output:
    index = "%s/aln.{asm}.idx" % __BRAKER_OUTDIR__
  params:
    rule_outdir = __BRAKER_OUTDIR__
  shell: """
    mkdir {params.rule_outdir}/aln.{wildcards.asm}.idx
    STAR --runMode genomeGenerate --genomeDir {output.index} --genomeFastaFiles {input.asm} 
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
    asm = lambda wildcards: "%s/asm.%s.fa" % (__GIVEN_ASM_OUTDIR__, wildcards.asm),
    bam = lambda wildcards: "%s/aln.%s.bam" % (__BRAKER_OUTDIR__, wildcards.asm)
  output:
    gff = "%s/genes.augustus.{asm}.gff" % __BRAKER_OUTDIR__,
    aa  = "%s/prots.augustus.{asm}.fa" % __BRAKER_OUTDIR__
  threads: 4
  params:
    rule_outdir = __BRAKER_OUTDIR__,
    braker_params = tconfig["braker_params"]
  shell: """
    echo "#######################"
    echo "#Attention: Deleting configuration for species {wildcards.asm}, predicted location is:"
    loc="$(readlink -f `which augustus` | rev | cut -d/ -f1 --complement | rev)/../config/species/{wildcards.asm}"
    echo "#$loc"
    echo "#CHANGE in rule braker if incorrect
    echo "#######################"
    rm -rf {params.rule_outdir}/braker/{wildcards.asm}
    rm -rf "$(readlink -f `which augustus` | rev | cut -d/ -f1 --complement | rev)/../config/species/{wildcards.asm}"
    braker.pl --cores {threads} \
              --GENEMARK_PATH=`which gmes_petap.pl | rev | cut -d/ -f1 --complement | rev` \
              --BAMTOOLS_PATH=`which bamtools | rev | cut -d/ -f1 --complement | rev` \
              --genome={input.asm} \
              --bam={input.bam} \
              --species={wildcards.asm} \
              --gff3 \
              --overwrite \
              {params.braker_params} \
              --workingdir={params.rule_outdir}/
    ln -s {params.rule_outdir}/braker/{wildcards.asm}/augustus.gff3 {output.gff}
    ln -s {params.rule_outdir}/braker/{wildcards.asm}/augustus.aa {output.aa}

  """
    
###############################################################################

# Rename the proteins that come out of the BRAKER prediction

rule braker_rename:
  input:
    gff = lambda wildcards: "%s/genes.augustus.%s.gff" % (__BRAKER_OUTDIR__, wildcards.asm),
    aa  = lambda wildcards: "%s/prots.augustus.%s.fa" % (__BRAKER_OUTDIR__, wildcards.asm)
  output:
    gff = "%s/genes.braker.{asm}.gff" % __BRAKER_OUTDIR__,
    aa  = "%s/prots.braker.{asm}.fa" % __BRAKER_OUTDIR__
  params:
    geneid_prefix = lambda wildcards: wildcards.asm
  shell: """
    sed -e "s/\([= ]\)\(g[0-9]\+\)/\\1{params.geneid_prefix}|\\2/g"  {input.gff} > {output.gff}
    sed -e 's/^>\(.\+\)$/>{params.geneid_prefix}|\\1/' {input.aa} > {output.aa}
  """

###############################################################################
  
