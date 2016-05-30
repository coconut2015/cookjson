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

# test 1
echo "-- Json output --"
../bin/convertjson -f ${FILE} -t test1.json
cat test1.json
echo
# test 2
echo "-- Pretty Json output --"
../bin/convertjson -f ${FILE} -t test2.json -p
cat test2.json
echo
# test 3
echo "-- Bson output --"
../bin/convertjson -f ${FILE} -t test3.bson
echo "---- bson dump --"
../bin/fixbson test3.bson
bsondump test3.bson
# test 4
echo "---- json dump --"
../bin/convertjson -f test3.bson -t test4.json -p
cat test4.json
echo

# test 5
echo "-- Bson double output --"
../bin/convertjson -f ${FILE} -t test5.bson -d
echo "---- bson dump --"
../bin/fixbson test5.bson
bsondump test5.bson
# test 6
echo "---- json dump --"
../bin/convertjson -f test5.bson -t test6.json -p
cat test6.json
echo
