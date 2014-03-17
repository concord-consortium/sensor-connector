#!/bin/bash

# This is based on the instructions here:
# http://stackoverflow.com/questions/96882/how-do-i-create-a-nice-looking-dmg-for-mac-os-x-using-command-line-tools

hdiutil create -srcfolder "dist/app" -volname "Sensor Server Installer" -fs HFS+ -fsargs "-c c=64,a=16,e=16" -format UDRW -size 140M dist/sensor_server_installer.tmp.dmg

LOC=$(hdiutil attach -readwrite -noverify -noautoopen "dist/sensor_server_installer.tmp.dmg" | egrep '^/dev/' | sed 1q | awk '{print $1}')

sleep 10

echo '
   tell application "Finder"
     tell disk "Sensor Server Installer"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 900, 412}
           set theViewOptions to the icon view options of container window
           set icon size of theViewOptions to 150
           set arrangement of theViewOptions to not arranged
           set background picture of theViewOptions to file ".background:bg.png"
           set position of item "SensorServer.app" of container window to {100, 100}
           set position of item "Applications" of container window to {375, 100}
           update without registering applications
           close
           open
           delay 5
     end tell
   end tell
' | osascript

chmod -Rf go-w "/Volumes/Sensor Server Installer"
sync
sync
hdiutil detach ${LOC}
sleep 5
hdiutil convert "dist/sensor_server_installer.tmp.dmg" -format UDZO -imagekey zlib-level=9 -o "dist/sensor_server_installer.dmg"
rm -f dist/sensor_server_installer.tmp.dmg 
