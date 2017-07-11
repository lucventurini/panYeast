# If we get a GFF file as part of the original definition

def gff_wrapper_input(wildcards):
  if "gff" in config["data"][wildcards.asm]:
    return "%s/renamed.%s.gff" % (__GFF_OUTDIR__, wildcards.asm)
  if ("rnaseq" in config["data"][wildcards.asm]):
    return "%s/genes.braker.%s.gff" % (__BRAKER_OUTDIR__, wildcards.asm)
  else:
    return "%s/augustus.%s.gff" % (__AUGUSTUS_OUTDIR__, wildcards.asm)
  #fi
 #edef


rule gff_wrapper:
  input:
    gff = lambda wildcards: gff_wrapper_input(wildcards)
  output:
    gff = "%s/genes.{asm}.gff" % __GFF_OUTDIR__
  shell: """
    ln -sf "{input.gff}" "{output.gff}"
  """

rule all_braker_gffs:
  input:
    gffs = expand("%s/genes.braker.{asm}.gff" % (__BRAKER_OUTDIR__), asm=[ a for a in config["data"].keys() if ("rnaseq" in config["data"][a]) and ("gff" not in config["data"][a])])

rule rename_given_gff:
  input:
    gff = lambda wildcards: config["dataprefix"] + '/' + config["data"][wildcards.asm]["gff"]
  output:
    gff = "%s/renamed.{asm}.gff" % (__GFF_OUTDIR__)
  params:
    geneid_prefix = lambda wildcards: wildcards.asm
  shell:"""
    if [ `echo "{input.gff}" | rev | cut -d. -f1 | rev` == 'gz' ]; then
      zcat "{input.gff}"
    else
      cat "{input.gff}"
    fi | sed -e "s/\(\(ID\|Parent\)[=]\)\([^;]\+\)/\\1{params.geneid_prefix}|\\3/g"  > "{output.gff}"
  """


rule all_gffs:
  input: expand("%s/genes.{asm}.gff" % __GFF_OUTDIR__, asm=config["data"].keys())

rule asm_stats:
  input:
    gff = lambda wildcards: "%s/genes.%s.gff" % (__GFF_OUTDIR__, wildcards.asm),
    asm = lambda wildcards: "%s/asm.%s.fa" % (__GIVEN_ASM_OUTDIR__, wildcards.asm)
  output:
    stats = "%s/stats/stats.{asm}.tsv" % __GFF_OUTDIR__
  params:
    rule_outdir = __GFF_OUTDIR__
  shell: """
    mkdir -p "{params.rule_outdir}/stats"

    ncontigs=`cat {input.asm} | grep '^>'  | wc -l`
    size=`cat {input.asm} | sed -e 's/^>.*/\t/' | tr -d '\n' | tr '\t' '\n' | awk 'BEGIN{{SUM=0}}{{ SUM += length($0) }} END{{ print SUM}}'`
    n50=`cat {input.asm} | sed -e 's/^>.*/\t/' | tr -d '\n' | tr '\t' '\n' | awk '{{ print length($0)}}' | sort -n | awk '{{len[i++]=$1;sum+=$1}} END {{for (j=0;j<i+1;j++) {{csum+=len[j]; if (csum>sum/2) {{print len[j];break}}}}}}'`
    ngenes=`cat {input.gff} | grep -v "^#" | grep gene | wc -l`
    avggenelength=`cat {input.gff} | grep -v "^#" | grep -i gene | awk 'BEGIN{{SUM=0;n=0}}{{ SUM+=($5 - $4); n++ }}END{{print (SUM/n)}} '`
    avgtranscriptlength=`cat {input.gff} | grep -v "^#" | grep -i transcript | awk 'BEGIN{{SUM=0;n=0}}{{ SUM+=($5 - $4); n++ }}END{{print (SUM/n)}} '`
    avgexonlength=`cat {input.gff} | grep -v "^#" | grep -i -e "\(CDS\|exon\)" | awk 'BEGIN{{SUM=0;n=0}}{{ SUM+=($5 - $4); n++ }}END{{print (SUM/n)}} '`
    avgintronlength=`echo "scale=2; ($avgtranscriptlength - $avgexonlength)" | bc`
    avgexonspergene=`cat {input.gff}| grep -v '^#' | grep -i -e '\(CDS\|exon\)' | awk -F 'arent=' '{{print $2}}' | uniq -c | sed -e 's/^\s\+//' | cut -d\  -f1 | awk 'BEGIN{{SUM=0;n=0}}{{ SUM+=$0; n++ }}END{{print (SUM/n)}} '`

    echo -en "{wildcards.asm}\t$ncontigs\t$size\t$n50\t$ngenes\t$avggenelength\t$avgtranscriptlength\t$avgexonlength\t$avgintronlength\t$avgexonspergene\n" > {output.stats}
  """

rule all_asm_stats:
  input:
    stats = expand("%s/stats/stats.{asm}.tsv" % __GFF_OUTDIR__, asm=config["data"].keys())
  output:
    stats = "%s/stats/all_stats.tsv" % __GFF_OUTDIR__
  shell: """
    echo -en "#genome\tnContigs\tgenomeSize\tN50\tnGenes\tMeanGeneLength\tMeanTranscriptLength\tMeanExonLength\tMeanIntronLength\tMeanNExonsPerGene\n" > {output.stats}
    cat {input.stats} >> {output.stats}
  """
