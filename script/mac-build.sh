#!/bin/sh

export JAVA_HOME=`/usr/libexec/java_home`

# build 32-bit package
rm -rf dist
export JRE_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK
ant mac-package && mv dist/SensorConnector-*.dmg .

# build 64-bit package
rm -rf dist
export JRE_HOME=$JAVA_HOME
ant mac-package-x64 && mv dist/SensorConnector-x64-*.dmg .
