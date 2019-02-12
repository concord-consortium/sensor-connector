#!/bin/sh

# Usage: With no arguments builds 32-bit package
#        Use "-32" or "-64" argument to build 32-bit/64-bit package only

export SC_APP_VERSION=`cat version.txt`

# build 32-bit package
if [[ ("$1" == "-32") || ($# -eq 0) ]]; then
	# must set JAVA_HOME manually, e.g.
	# export JAVA_HOME="c:/Program Files (x86)/Java/jdk1.8.0_202/"
	export SC_ARCH_BITS=32
	rm -rf dist
	ant win-bundle-x86 && mv dist/SensorConnector-*.msi . && mv dist/SensorConnector-*.exe .
	rm *unsigned*
fi

# build 64-bit package
if [[ ("$1" == "-64") ]]; then
	# must set JAVA_HOME manually, e.g.
	# export JAVA_HOME="c:/Program Files/Java/jdk1.8.0_202/"
	export SC_ARCH_BITS=64
	rm -rf dist
	ant win-bundle-x64 && mv dist/SensorConnector-*.msi . && mv dist/SensorConnector-*.exe .
	rm *unsigned*
fi