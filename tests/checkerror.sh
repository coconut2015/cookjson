#!/bin/bash
#

if [ $# -ne 1 ]; then
    cat <<EOF
Usage:
    $0 [file]
EOF
    exit 0
fi

FILE=$1

../bin/convertjson -f ${FILE} -t test.json
