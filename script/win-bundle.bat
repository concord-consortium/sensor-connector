candle -ext WixUtilExtension -ext WixBalExtension -out tmp\ resources\win-bundles.wxs
if %errorlevel% neq 0 exit /b %errorlevel%
light  -ext WixUtilExtension -ext WixBalExtension tmp\*.wixobj -o dist\SensorConnector-bundle.exe
