#!/bin/bash

mvn install:install-file -Dfile=lib/tungsten-fsm.jar -DgroupId=com.continuent.tungsten -DartifactId=tungsten-fsm -Dversion=1.2 -Dpackaging=jar
