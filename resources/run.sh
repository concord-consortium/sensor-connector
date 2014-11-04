#!/bin/bash

cd "$( dirname "${BASH_SOURCE[0]}" )"
cd ..
export JAVA_HOME=$(pwd -P)/Resources/jdk
export PATH=$JAVA_HOME/Home/bin:$PATH

java -d32 -Djavax.net.debug=ssl:handshake -Djava.ext.dirs="Resources/jdk/Home/lib/ext:Resources/jdk/Home/lib:Java" -Djava.library.path="" -Djna.library.path="Frameworks" -Dapple.awt.UIElement=true -Xdock:icon=Resources/cc-lightbulb.icns org.concord.sensor.server.SensorConnector
