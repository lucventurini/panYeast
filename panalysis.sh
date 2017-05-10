#!/usr/bin/env bash

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";

java -jar -Xmx100G $SCRIPTDIR/panalysis/panalysis.jar $@
