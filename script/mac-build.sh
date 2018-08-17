#!/bin/sh

# Usage: With no arguments builds 32-bit and 64-bit packages
#        Use "-32" or "-64" argument to build 32-bit/64-bit package only

export JAVA_HOME=`/usr/libexec/java_home`
export SC_APP_VERSION=`cat version.txt`

# build 32-bit package
if [[ ("$1" == "-32") || ($# -eq 0) ]]; then
  rm -rf dist
  JRE32_HOME=`/usr/libexec/java_home -d 32`
  # for Java6 we want the parent directory
  export JRE_HOME="$(dirname "$JRE32_HOME")"
  export SC_ARCH_BITS=32
  ant mac-package && mv dist/SensorConnector-*32.dmg .
fi

# build 64-bit package
if [[ ("$1" == "-64") || ($# -eq 0) ]]; then
  rm -rf dist
  export JRE_HOME=$JAVA_HOME
  export SC_ARCH_BITS=64
  ant mac-package-x64 && mv dist/SensorConnector-*64.dmg .
fi
