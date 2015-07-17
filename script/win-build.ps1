$scriptpath = $MyInvocation.MyCommand.Path
$dir = Split-Path $scriptpath
cd $dir
cd ..

rm dist -r -Force
ant win-bundle
mv dist/SensorConnector-*.msi .
mv dist/SensorConnector-*.exe .
rm *unsigned*

# Read-Host -Prompt "Press Enter to exit"