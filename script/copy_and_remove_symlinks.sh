#!/bin/sh

mkdir $2
cd $1
find . | cpio -pdL $2
