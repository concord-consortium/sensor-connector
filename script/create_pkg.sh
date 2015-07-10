#!/bin/bash

# NOTE: This is all probably really brittle!

mv dist/app/SensorConnector.app dist/package/

# Requires PackageMaker to be installed in /Applications
cp -r resources/sensorconnector-package.pmdoc dist/package
cp resources/ca.cert.pem dist/package
cp resources/mac-install-cert.sh dist/package
chmod a+x dist/package/mac-install-cert.sh

cd dist/package
/Applications/PackageMaker.app/Contents/MacOS/PackageMaker --verbose --doc sensorconnector-package.pmdoc --out ../app/SensorConnector.pkg

# TODO Using pkgbuild and productbuild is more future-proof, but I ran out of time
# cd dist/package
# pkgbuild --identifier org.concord.sensorconnector.app.pkg --version 1.0.0.20140814 --root SensorConnector.app --install-location /Applications/SensorConnector.app SC.pkg
# productbuild --distribution SC.xml --package-path . SC-unsigned.pkg
# productsign --sign "Developer ID Installer: Concord Consortium Inc (T8HS8WBPPQ)" SC-unsigned.pkg ../app/SensorConnector.pkg

cd -
