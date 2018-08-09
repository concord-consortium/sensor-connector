#!/bin/sh
NAME=`echo ${JRE_HOME} | grep -E -o 'jdk[^\/]*'`
export JRE_NAME="$NAME"
echo $NAME
