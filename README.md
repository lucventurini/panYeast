# PANalysis
Pipeline to analyze a set of genomes.
By stretching the definition of the pan-genome to any set of genomes, even with little genetic similarity, we  pan genome.

## Dependencies:
 - Snakemake
 - Conda
 - interproscan

## Usage
 -  Generate instance of the pipeline (DO THIS FIRST)

```bash
    /path/to/github/clone/gen_pipeline.sh data.json out_dir
```

 - orthology detection & phylogentic tree

```bash
    snakemake --use-conda --cores 10 -R pancore_tree
```
 - annotations

```bash
    snakemake --use-conda --cores 10 -R perform_annotations
```

 - Genome statistics

```bash
    snakemake --use-conda --cores 10 -R all_asm_stats
```

 - More...

```bash
    snakemake --use-conda --cores 10 -R all_gffs # Generate all GFF files (with augustus/BRAKER1)
    snakemake --use-conda --cores 10 -R iadhore_ortholog_clusters # Link syntenic clusters to ortholog clusters
    snakemake --use-conda --cores 10 -R panalysis # Perform various actions, such as a TSN-E plot of all genes, and validation of orthology clusters using functional annotations
```
    
 

### Configuration

#### Generated Snakemake file
  After running gen_pipeline.sh, there will be a file named `Snakefile` in your out_dir.
  In this file, you can modify various parameters, in the `tconfig` python dictionary.

#### data.json
  You will provide a file describing the data you have.
  Example:
```python
    {
      "dataprefix": "/home/thiesgehrmann/data/genomes/phaeoacremonium", # prefix to apply to all file names provided here"
      "data": {
        "palvesii": {
          "name" : "P alvesii",               # Name of the genome
          "asm" : "palvesii/p.alvesii.fasta", # You need AT LEAST the genome FASTA files
          "augustus_species" : "tminima"      # You can also specify the genome model to predict genes
        },
    
        "pkrajenii": {
          "name": "P krajdenii",                
          "asm" : "pkrajenii/p.krajenii.fasta",
          "gff" : "pkrajenii/p.krajenii.gff"    # If you have gene models, you can provide them!
        },
    
        "tminima": {
          "name": "Togninia_minima",
          "asm" : "togninia_minima/Togninia_minima_UCRPA7_v1_supercontigs.renamed.fasta",
             # If you have RNASeq data, you can provide that for gene model training (concatenate if multiple samples)
          "rnaseq" : [ "togninia_minima/t_minima.PRJNA270932/tminima_R1.fastq", "togninia_minima/t_minima.PRJNA270932/tminima_R2.fastq"]
        },
    
        "outgroup": {
          "name": "Diaporthe ampelina",
          "asm": "outgroup_diaporthe_ampelina/genome.fasta",
          "gff": "outgroup_diaporthe_ampelina/genes.gff"
        }
      }
    }
```



