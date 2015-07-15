#!/bin/sh

rm -rf dist
ant win-bundle && mv dist/SensorConnector-*.msi . && mv dist/SensorConnector-*.exe .
rm *unsigned*
