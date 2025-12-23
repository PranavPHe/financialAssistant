#!/bin/bash
clear 

javac -cp "./lib/snakeyaml-2.5.jar:." -d . ./src/Main.java

if [ $? -ne 0 ]; then
    echo "Compilation failed"
    exit 1
fi

java -cp "./lib/snakeyaml-2.5.jar:." src.Main
