rule extract_given_asm:
  input:
    asm = lambda wildcards: config["dataprefix"] + '/' + config["data"][wildcards.asm]["asm"]
  output:
    asm = "%s/asm.{asm}.fa" % (__GIVEN_ASM_OUTDIR__)
  shell:"""
    if [ `echo "{input.asm}" | rev | cut -d. -f1 | rev` == 'gz' ]; then
      zcat "{input.asm}" > "{output.asm}"
    else
      ln -sf "{input.asm}" "{output.asm}"
    fi
  """

rule all_asms:
  input: expand("%s/asm.{asm}.fa" % (__GIVEN_ASM_OUTDIR__), asm=config["data"].keys())
