rule iadhore_blast_input:
  input:
    pairs = rules.orthofinder_all.output.mci_input_pairs
  output:
    pairs = "%s/families.tsv" % __SYNTENY_OUTDIR__
  shell: """
    ln -s {input.pairs} {output.pairs]
  """

rule iadhore_genome_input:
  input:
    gff = lambda wildcards: "%s/genes.%s.gff" % (__GFF_OUTDIR__, wildcards.asm)
  output:
    listfile = "%s/lsts/lst.{asm}/lst" % __SYNTENY_OUTDIR__
  params:
    rule_outdir = __SYNTENY_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}/lsts/lst.{wildcards.asm}

    cat {input.gff} \
     | grep -v '^#' \
     | awk -F $'\t' 'BEGIN{{ OFS=FS}}{{
         if(tolower($3) == "transcript") {{
           print $1, $4, $9, $7
         }}
       }}' \
     | sort -t$'\t' -k1,1 -k2,2n \
     | awk -v odir="{params.rule_outdir}/lsts/lst.{wildcards.asm}" -F $'\t' '
       BEGIN{{
         prev="xxyy";
         count=0
         print "genome={wildcards.asm}"
       }}{{
         if ($1 != prev){{
           if ( prev != "xxyy") {{
             close(odir "/" count ".lst")
           }}
           prev=$1
           count++
           print prev " " odir "/" count ".lst"
         }}
         split($3,info,"=|;")
         for(i in info) {{
           if (info[i] == "ID"){{
             print info[i+1] $4 >> odir "/" count ".lst"
             break
           }}
         }}
       }}' > {output.listfile}
        
  """

rule iadhore_ini_input:
  input:
    genomes = expand("%s/lsts/lst.{asm}/lst" % __SYNTENY_OUTDIR__, asm=config["data"].keys()),
    pairs   = rules.orthofinder_all.output.mci_input_pairs
  output:
    ini = "%s/iadhore.ini" % __SYNTENY_OUTDIR__
  threads: 20
  params:
    rule_outdir = __SYNTENY_OUTDIR__
  shell: """
    cat {input.genomes} > {output.ini}
    
    echo "blast_table= {input.pairs}" >> {output.ini}
    echo "" >> {output.ini}
    #echo "table_type= family" >> {output.ini}
    echo "visualizeAlignment=true" >> {output.ini}
    echo "output_path= {params.rule_outdir}/output" >> {output.ini}
    echo "" >> {output.ini}
    echo "alignment_method=gg2" >> {output.ini}
    echo "gap_size=30" >> {output.ini}
    echo "cluster_gap=35" >> {output.ini}
    echo "q_value=0.75" >> {output.ini}
    echo "prob_cutoff=0.01" >> {output.ini}
    echo "anchor_points=3" >> {output.ini}
    echo "level_2_only=false" >> {output.ini}
    echo "number_of_threads={threads}" >> {output.ini}

  """

rule iadhore:
  input:
    ini = rules.iadhore_ini_input.output.ini
  output:
    multiplicons      = "%s/output/multiplicons.txt" % __SYNTENY_OUTDIR__,
    multiplicon_pairs = "%s/output/multiplicon_pairs.txt" % __SYNTENY_OUTDIR__,
    segments          = "%s/output/segments.txt" % __SYNTENY_OUTDIR__
  threads: 20
  conda: "%s/conda_envs/synteny.yaml" % __PIPELINE_COMPONENTS__
  shell: """
    i-adhore {input.ini}
  """

rule iadhore_clusters:
  input:
    multiplicons      = rules.iadhore.output.multiplicons,
    multiplicon_pairs = rules.iadhore.output.multiplicon_pairs
  output:
    clusters = "%s/cluster_genes.tsv" % __SYNTENY_OUTDIR__
  params:
    utils_dir = __PIPELINE_COMPONENTS__ + "/utils"
  shell: """
    awk -f {params.utils_dir}/filter.awk -F$'\t' -v FIELD=2 <(cat {input.multiplicons} | awk '{{ if ($13 == 0){{ print $1 }}}}' | uniq) <(cat {input.multiplicon_pairs}) \
    | awk -F$'\t' 'BEGIN{{OFS=FS}}{{print $2, $3 "\\n" $2, $4}}' \
    | sort -k1,1n -k2,2 \
    | uniq \
    > {output.clusters}
  """

# This didn't really work in the end...
#rule iadhore_clusters:
#  input:
#    multiplicons = rules.iadhore.output.multiplicons,
#    segments     = rules.iadhore.output.segments
#  output:
#    clusters = "%s/cluster_genes.tsv" % __SYNTENY_OUTDIR__
#  conda: "%s/conda_envs/synteny.yaml" % __PIPELINE_COMPONENTS__
#  params:
#    utils_dir = __PIPELINE_COMPONENTS__ + "/utils"
#  shell: """
#    {params.utils_dir}/iadhore_clusters.py "{input.multiplicons}" "{input.segments}" "{output.clusters}"
#  """

rule iadhore_ortholog_clusters:
  input:
    iadhore_clusters  = rules.iadhore_clusters.output.clusters,
    ortholog_clusters = rules.get_cluster_genes.output.cluster_genes_flat
  output:
    annots = "%s/iadhore_ortholog_clusters.tsv" % __SYNTENY_OUTDIR__
  shell: """
    echo -e "#Protein\tortholog_cluster\tiadhore_cluster" > {output.annots}
    join -t$'\t' -j2 -o 1.2,1.1,2.1 <(sort -k2,2 {input.ortholog_clusters}) <(sort -k2,2 {input.iadhore_clusters}) >> {output.annots}
  """

 # Use the orthology information to extend the syntenic clusters that we found
#rule extend_syntenic_clusters:
#  input:
#    annots            = rules.iadhore_ortholog_clusters.output.annots,
#    ortholog_clusters = 

#rule synteny_summary:
#  input:
#    iadhore_clusters = rules.iadhore_clusters.output.clusters

