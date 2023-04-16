#!/bin/bash
mkdir -p build
cp -r app/* build
export CLASSPATH=.:$(find complibs -name '*jar'|tr '\n' ':'):$(find app/WEB-INF/lib/ -name '*jar'|tr '\n' ':')
javafiles=$(find src -name '*java')
javac -d build/WEB-INF/classes $javafiles
