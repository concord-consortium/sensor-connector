#!/bin/sh

export SC_APP_VERSION=`cat version.txt`
export SC_ARCH_BITS=32

rm -rf dist
ant win-bundle && mv dist/SensorConnector-*.msi . && mv dist/SensorConnector-*.exe .
rm *unsigned*
