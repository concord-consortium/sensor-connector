candle -ext WixUtilExtension -ext WixBalExtension -out tmp\ resources\win-bundles.wxs
light  -ext WixUtilExtension -ext WixBalExtension tmp\*.wixobj -o dist\SensorConnector-bundle.exe
