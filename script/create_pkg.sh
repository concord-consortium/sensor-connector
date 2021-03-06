#!/bin/bash

# NOTE: This is all probably really brittle!

mv dist/app/SensorConnector.app dist/package/

# Requires PackageMaker to be installed in /Applications
# cp -r resources/sensorconnector-package.pmdoc dist/package
cp resources/mac-postinstall.sh dist/package/scripts/postinstall
chmod a+x dist/package/scripts/postinstall

cp resources/Distribution.xml dist/package/
cp resources/bg.png dist/package/resources/background.png
cd dist/package

# TODO Update the version in Distribution.xml
PKG_VERSION=$SC_APP_VERSION.$(date +%Y%m%d.%H%M)
echo "Package version: $PKG_VERSION"

# Using pkgbuild and productbuild is more future-proof
pkgbuild --root ./SensorConnector.app --scripts scripts --identifier org.concord.sensorconnector.app.pkg --version $PKG_VERSION --install-location /Applications/SensorConnector.app SensorConnectorApp.pkg
productbuild --distribution ./Distribution.xml --resources ./resources --package-path ./SensorConnectorApp.pkg ./SensorConnector-unsigned.pkg
productsign --sign "Developer ID Installer: Concord Consortium" SensorConnector-unsigned.pkg ../app/SensorConnector.pkg

cd -
