#!/bin/sh

rm -rf dist
ant win-bundle && mv dist/SensorConnector-*-1.msi . && mv dist/SensorConnector-*-1.exe .
