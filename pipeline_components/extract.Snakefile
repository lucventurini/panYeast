rule extract_prots:
  input:
    fa = lambda wildcards: "%s/prots.%s.fa" % (__PROTS_OUTDIR__, wildcards.asm)
  output:
    fa = "%s/{asm}/aa.fa.gz" % __EXTRACT_OUTDIR__
  params:
    rule_outdir = __EXTRACT_OUTDIR__
  shell: """
    mkdir -p "{params.rule_outdir}/{wildcards.asm}"
    gzip < {input.fa} > {output.fa}
    """

    
rule extract_trans:
  input:
    fa = lambda wildcards:  "%s/transcripts.%s.fa" % (__TRANS_OUTDIR__, wildcards.asm)
  output:
    fa = "%s/{asm}/nt.fa.gz" % __EXTRACT_OUTDIR__
  params:
    rule_outdir = __EXTRACT_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}/{wildcards.asm}
    gzip < {input.fa} > {output.fa}
    """

rule extract_gff:
  input:
    gff = lambda wildcards:  "%s/genes.%s.gff" % (__GFF_OUTDIR__, wildcards.asm)
  output:
    gff = "%s/{asm}/genes.gff.gz" % __EXTRACT_OUTDIR__
  params:
    rule_outdir = __EXTRACT_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}/{wildcards.asm}
      # Strip the asm name from the 
    sed -e 's/={wildcards.asm}|/=/g' {input.gff} \
     | gzip \
     > {output.gff}
    """

rule extract_asm:
  input:
    fa = lambda wildcards: "%s/asm.%s.fa" % (__GIVEN_ASM_OUTDIR__, wildcards.asm)
  output:
    fa = "%s/{asm}/asm.fa.gz" % __EXTRACT_OUTDIR__
  params:
    rule_outdir = __EXTRACT_OUTDIR__
  shell: """
    mkdir -p {params.rule_outdir}/{wildcards.asm}
    gzip < {input.fa} > {output.fa}
    """

rule extract_json:
  input:
    asm = expand("%s/{asm}/asm.fa.gz" % __EXTRACT_OUTDIR__, asm=config["data"].keys()),
    gff = expand("%s/{asm}/genes.gff.gz" % __EXTRACT_OUTDIR__, asm=config["data"].keys()),
    aa  = expand("%s/{asm}/aa.fa.gz" % __EXTRACT_OUTDIR__, asm=config["data"].keys()),
    nt  = expand("%s/{asm}/nt.fa.gz" % __EXTRACT_OUTDIR__, asm=config["data"].keys())
  output:
   data_json = "%s/config.json" % __EXTRACT_OUTDIR__
  params:
    rule_outdir = __EXTRACT_OUTDIR__
  threads: 1
  run:
    with open(output.data_json, 'w') as fd:
      fd.write('{ "dataprefix" : "%s",\n' % params.rule_outdir)
      fd.write('  "data" : {\n')
      for (i, asm) in enumerate(config["data"].keys()):
        fd.write('    "%s" : {\n' % asm)
        if 'name'in config["data"][asm]:
          fd.write('      "name" : "%s",\n' % config["data"][asm]["name"])
        fd.write('      "asm" : "%s/asm.fa.gz",\n' % (asm) )
        fd.write('      "gff" : "%s/genes.gff.gz",\n' % (asm) )
        fd.write('      "aa" : "%s/aa.fa.gz",\n' % (asm) )
        fd.write('      "nt" : "%s/nt.fa.gz"\n' % (asm) )
        fd.write('    }')
        if i < len(config["data"].keys())-1:
          fd.write(',')
        fd.write('\n')
      fd.write('}}')

def extract_main_snakemake_format_var(v):
  if type(v) == 'str':
    return '"%s"' % v
  else:
    return str(v)

import copy

rule extract_main_snakefile:
  input:
    json = rules.extract_json.output.data_json
  output:
    sf = "%s/Snakefile" % __EXTRACT_OUTDIR__
  run:
    new_tconfig = copy.deepcopy(tconfig)
    new_tconfig['aa_field_delim'] = '|'
    new_tconfig['nt_field_delim'] = '|'
    new_tconfig['aa_idfield'] = 2
    new_tconfig['nt_idfield'] = 2

    with open(output.sf, 'w') as fd:
      fd.write('configfile: "config.json"\n')
      fd.write('\n')
      fd.write('WORKDIR="__WORKDIR_REPLACE__"\n')
      fd.write('INSTALL_DIR="%s"\n' % INSTALL_DIR)
      fd.write('\n')
      fd.write('tconfig={\n')
      for (i,k) in enumerate(sorted(new_tconfig.keys())):
        fd.write('  "%s" : "%s"' % (k, extract_main_snakemake_format_var(tconfig[k])))
        if i < len(tconfig.keys())-1:
          fd.write(",")
        fd.write("\n")
      fd.write('}\n')
      fd.write('include: "%s/pipeline.Snakefile" % INSTALL_DIR\n')
    
rule extract:
  input:
    json = rules.extract_json.output.data_json,
    sf = rules.extract_main_snakefile.output.sf


