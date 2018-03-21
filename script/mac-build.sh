#!/bin/sh

# Usage: With no arguments builds 32-bit and 64-bit packages
#        Use "-32" or "-64" argument to build 32-bit/64-bit package only

export JAVA_HOME=`/usr/libexec/java_home`

# build 32-bit package
if [[ ("$1" == "-32") || ($# -eq 0) ]]; then
  rm -rf dist
  export JRE_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK
  ant mac-package && mv dist/SensorConnector-*.dmg .
fi

# build 64-bit package
if [[ ("$1" == "-64") || ($# -eq 0) ]]; then
  rm -rf dist
  export JRE_HOME=$JAVA_HOME
  ant mac-package-x64 && mv dist/SensorConnector-x64-*.dmg .
fi
