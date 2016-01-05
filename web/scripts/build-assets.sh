#!/bin/bash
cd "$(dirname "$0")"

FORCE=false
while getopts ":f" opt; do
  case $opt in
    f)
      FORCE=true
      ;;
  esac
done

#COMPILE_DIR="../src/main/java/assets/compiled/"
COMPILE_DIR="/var/tmp/serposcope-compiled-assets/"
mkdir -p "$COMPILE_DIR"
if [ -z "$COMPILE_DIR" ] || [ ! -d $COMPILE_DIR ]; then
    echo "COMPILE_DIR must be a rw dir : $COMPILE_DIR"
    exit 1
fi

LAYOUT=../src/main/java/serposcope/views/layout/root.ftl.html
JS=`sed -n -e '/<!-- start-js -->/,/<!-- end-js -->/ p' $LAYOUT  | grep -v -- '-->' | sed -r 's/^\s+//' | egrep -v ^$`

FILES=`echo $JS | sed -r 's/<script[^<>]+src="([^">]+)"[^>]*><\/script>/\1/g' | sed -r 's/(^\s+)|(\s+$)//g'`

if $FORCE; then
    rm -Rf $COMPILE_DIR/*
fi

echo > ../src/main/java/assets/js/combined.min.js

for SCRIPT in $FILES; do
    FILE="../src/main/java"$SCRIPT
    FILE_MIN="$COMPILE_DIR/"`echo $SCRIPT | sed -r 's/\.js$/.min.js/'`
    DIR_MIN=$(dirname "$FILE_MIN")
    mkdir -p $DIR_MIN

    if [ ! -f "$FILE_MIN" ] || [ "$FILE" -nt "$FILE_MIN" ]; then
        echo "compressing JS file $FILE ..."
        yui-compressor.sh --type js -o "$FILE_MIN" "$FILE"
        if [ $? -ne 0 ]; then
            echo "compress error"
            rm ../src/main/java/assets/js/combined.min.js
            exit 1
        fi
    fi

    cat $FILE_MIN >> ../src/main/java/assets/js/combined.min.js
done

#FILES_MIN=`echo $FILES | sed -r 's/\.js/-min\.js/g'`
#echo "combining JS assets..."
#cat $FILES_MIN > js/combined.min.js

exit 0
