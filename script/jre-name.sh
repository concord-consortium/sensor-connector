#!/bin/sh
NAME=`echo ${JRE_HOME} | grep -E -o 'jdk[^\/]*'`
echo JRE_NAME=$NAME
export JRE_NAME="$NAME"
