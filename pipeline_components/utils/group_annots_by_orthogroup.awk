      BEGIN{{
        OFS = FS
        split("",genecluster)
        split("",annots)
        printf "#orthogroup\tngenes\tnannot\tannot_genomes\tgenes\n"
      }}
      {{
        # If we are in the cluster_genes file...
        if (FNR == NR) {{
          annots[$1] = $12
        }} else {{
          ngenes = split($2,genes, ",")
          split("",genomes)
          nannot = 0
          ngenomes = 0
          genestring = ""
          for (gene_i in genes) {{
            gene = genes[gene_i]
            if (gene in annots) {{
              split(gene,genesplit, "|")
              if (! (genesplit[1] in genomes)) {{
                ngenomes += 1
              }}
              #print $1 ", " gene ", " genesplit[1] ", " ngenomes
              genomes[genesplit[1]] = 1
              nannot += 1
              genestring = genestring "," gene
            }} else {{
              genestring = genestring "," gene "*"
            }}
          }}
          if (nannot > 0) {{
            printf "%d\t%d\t%d\t%d\t%s\n", $1, ngenes, nannot, ngenomes, substr(genestring,2)
          }}
        }}
    }}
