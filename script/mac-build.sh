#!/bin/sh

rm -rf dist
ant mac-package && mv dist/SensorConnector-*.dmg .

rm -rf dist
ant mac-package-x64 && mv dist/SensorConnector-x64-*.dmg .
