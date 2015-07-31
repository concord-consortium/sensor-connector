# Set-ExecutionPolicy -ExecutionPolicy ByPass -Scope Process

$scriptpath = $MyInvocation.MyCommand.Path
$dir = Split-Path $scriptpath
cd $dir
cd ..

$env:JAVA_HOME="C:\Program Files (x86)\Java\jdk1.7.0_79"

. "D:\Software\paths.ps1"

rm dist -r -Force
ant win-bundle
mv dist/SensorConnector-*.msi .
mv dist/SensorConnector-*.exe .
rm *unsigned*

# Read-Host -Prompt "Press Enter to exit"