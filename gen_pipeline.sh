#!/bin/sh

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";

###############################################################################

function error() {
  local msg="$1"
  echo "ERROR: $msg"
}

###############################################################################

function warning() {
  local msg="$1"
  echo "WARNING: $msg"
}

###############################################################################

function readWhileInvalidChar(){

  validchars="$1"
  while true; do
    read -n1 -p "($validchars): " c
    if [[ "$validchars" == *"$c"* ]]; then
      echo $c;
      break;
    fi
  done
}

###############################################################################

function verifyOverWrite(){
  local file="$1";

  if [ -e "$file" ]; then
    warning "The file $file already exists, are you sure you want to overwrite it?"
    resp=`readWhileInvalidChar yn`
    mv "$file" "$file.bak"
    warning "The file $file is moved to $file.bak"
    echo ""
    if [ "$resp" == "n" ]; then
      error "File cannot be overwritten"
      exit 1
    fi
  fi
}


###############################################################################

function usage(){
  cmd="$1";

  echo "Usage: $cmd <data_desc.tsv> <data_dir> <out_dir>"
  echo ""
  echo "  config.json:   The config file"
  echo "  out_dir:       Where the pipeline and all its data will reside"
}


###############################################################################

if [ $# -ne 2 ]; then
  usage $0
  exit 1
fi

data_desc="$1";
out_dir="$2";

###############################################################################

mkdir -p "$out_dir"


###############################################################################


  # Generate config file
verifyOverWrite $out_dir/config.json
cp $data_desc $out_dir/config.json

###############################################################################

  # Copy Snakefile to proper directory
verifyOverWrite "$out_dir/Snakefile"
cat "$SCRIPTDIR/Snakefile" \
  | sed -e "s!__INSTALL_DIR_REPLACE__!$SCRIPTDIR!" \
        -e "s!__WORKDIR_REPLACE__!$out_dir!" \
  > "$out_dir/Snakefile"

###############################################################################

echo "Pipeline initialized in $out_dir."
echo "Data configuration is in $out_dir/config.json"
echo "Change the necessary parameters in $out_dir/Snakefile"

