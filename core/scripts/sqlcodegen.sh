#!/bin/bash

if [ $(uname) == "Linux" ]; then
    COREPATH=`readlink -f "$(dirname "$0")/.."`
elif [ $(uname) == "Darwin" ]; then
    if which greadlink >/dev/null; then
        COREPATH=`greadlink -f "$(dirname "$0")/.."`
    else
        echo "OSX users need to install greadlink (brew install coreutils)"
    fi
else
    echo "$(uname) platform not supported"
    exit 1
fi

JAR=$COREPATH/lib/*.jar
DBPATH=$COREPATH/codegen/h2

cd $COREPATH/src/main/resources/db/

rm $DBPATH* 2>/dev/null

if [ $# -eq 1 ]; then
    cd "$1"
fi

for SCRIPT in *.sql; do
    if [ ! -f $SCRIPT ]; then
        echo "$SCRIPT doesn't exists"
        continue;
    fi
    echo "import $SCRIPT"
    java -cp $JAR org.h2.tools.RunScript -url jdbc:h2:$DBPATH -script "$SCRIPT"
done

cd $COREPATH
mvn -Dcodegen clean install
