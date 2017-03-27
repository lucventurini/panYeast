
###############################################################################

def read_fasta(fname, sep='auto', fieldnames=()):
    # Copied from my own code at
    # https://github.com/mhulsman/ibidas/blob/677f64a293826d800376a64d1705b97079a0ac60/ibidas/wrappers/fasta.py#L6

    f = open(fname); # Removed safeguard because lazy
    fas  = [];
    seqs = []

    seqid = "";
    seq   = "";

    for line in f:
        line = line.strip('\n\r');
        if not line or line[0] == ">":
            if seqid:
                fas.append(seqid);
                seqs.append(seq.replace(' ',''));
            seqid = line[1:];
            seq = "";
            continue;
        seq = seq + line;

    f.close();

    if seq:
        fas.append(seqid);
        seqs.append(seq.replace(' ',''));

    return (fas, seqs)

#edef



###############################################################################

nt_bases = ['t', 'c', 'a', 'g'];
nt_codons = [a+b+c for a in nt_bases for b in nt_bases for c in nt_bases];
amino_acids = 'FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG';
codon_table = dict(zip(nt_codons, amino_acids));

def nt_translate(seq):
  aa = '';
  for i in range(0, len(seq), 3):
    cod = seq[i:i+3].lower();
    aa += codon_table[cod] if (cod in codon_table) else '*';
  #efor
  return aa;
#edef

###############################################################################
