#!/bin/sh

rm dist -r -Force
ant win-bundle
mv dist/SensorConnector-*.msi .
mv dist/SensorConnector-*.exe .
rm *unsigned*
