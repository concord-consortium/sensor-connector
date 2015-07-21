#!/bin/bash

DIR="$2/SensorConnector.app/Contents/Resources/"
WHO=$(who -m | awk '{print $1;}')
if [ "$WHO" = "root" ]; then
  IS_ROOT=true
else
  IS_ROOT=false
fi

# Install the trusted CA cert
TMP=$(mktemp -t "ca.cert.pm")
cat > $TMP <<ENDCERT
-----BEGIN CERTIFICATE-----
MIIFpDCCA4ygAwIBAgIJALDEmAKeBRrtMA0GCSqGSIb3DQEBCwUAMF8xCzAJBgNV
BAYTAlVTMRYwFAYDVQQIDA1NYXNzYWNodXNldHRzMRswGQYDVQQKDBJDb25jb3Jk
IENvbnNvcnRpdW0xGzAZBgNVBAMMEkNvbmNvcmQgQ29uc29ydGl1bTAeFw0xNTA3
MDkyMjIyMThaFw0zNTA3MDQyMjIyMThaMF8xCzAJBgNVBAYTAlVTMRYwFAYDVQQI
DA1NYXNzYWNodXNldHRzMRswGQYDVQQKDBJDb25jb3JkIENvbnNvcnRpdW0xGzAZ
BgNVBAMMEkNvbmNvcmQgQ29uc29ydGl1bTCCAiIwDQYJKoZIhvcNAQEBBQADggIP
ADCCAgoCggIBAM8bww5QL8BmmwNIpNI4Uba5Ly+A2lnQdDizJ7NUGJQgw9buNY7v
ZJj4hrDqgg972dEfxjTo5RsgWjPlJ8zbJDJf90m6VKbXYZ7Nhuf9gT7hTpTtdZuz
Qf74urmxhvakkvC2gay6s0Ep7sJXSJ4X/wyZcH9ay/ziV2E4UTIgKi7EUUyxJMsy
zUBl8OTrjhTsCgi/2J2jsDOTey3NVuVIXWceb4BBr0nKPRO7loK8g3TZZBDk9y45
hH2+QbyIi5PaINdkGwOCBqnpoHL5vDv08DsVRUxVikonAiyGrKRcBjCsFfnOmpib
ElicYSYlJAv5vnSocC96u1pqfmqmXJ1BgoFEnq8qxKd8qwyANgnDxH4iPsn5RncH
A3W/mvo/iQhiqkkLHMBfT/bnF3LHQBvfprMooaLflBEE5g1UuOJcPMIbhdeb4umk
stsFmP7eFFkaaPduBGpbw8AzeGR8F1pIzH3KhuZeiQmi/M6HJ9Llg+Xyc2zeDBei
j5t5cRqQWLKrtMgeABm48++EsJ07B1Rm/kEhCg0oAtTWQBKxvGX0465Qs0Onjzf8
hyeuVM4/7313xru4mbMP9F/OsQcCvv3pSutPEx9kQFUxowh0QWaI6HlXIJ80jDBW
cd0pWfYGCdMOeawN0rlc7GvB5SPQ/4cjroAvQmMrNwvgOTXsZDMmoNj5AgMBAAGj
YzBhMB0GA1UdDgQWBBQCCunPXtIYw/p6F8HjshiOmJuYzDAfBgNVHSMEGDAWgBQC
CunPXtIYw/p6F8HjshiOmJuYzDAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQE
AwIBhjANBgkqhkiG9w0BAQsFAAOCAgEAzZs8ny2qqcDIc8LnwpfoJB0WNmKpFGyQ
D2c2V319swN/bKet8PzKQVafIFZWbxqwfL7dUmuVYheoFZh6BcoAT2u04Aqf+KGA
uP16Umwe/iKEk1MdDwnh9bdYhUswbI51rvKLJrmHSV+Afwm9PX7GQ7ygUwfyy8dW
YKbMosw3KGI0No65inGCYGTqplRg+CcL1x+OVHsiGhyHoQt1ItcznHZrOiWCaJSt
1pZLzNIH0uUaZa4pnGoPycGNMgd0wrAgZV0NFRy19JawKlU/kkpvpM93UGzx4tqk
2YYoh2NekC4Prd8KNbsWBGqjdY1iBh6REPypxnl5U0hDmaLGi2p5h1t4oN9mlOiU
K8QR74UCxGr+LBgxCJwFUpFTm4rFvjJAeaLhmripH0MmGPGCVkFL5SqlcCwOTdKH
5EW5TLPUNA6lwAZxd2ylCe04nmpW2m6ePd1E27/HdepMnY1+yqVFY24S/GfCluBk
Mldqvu1uEVapy8z0cllzIhQrR4ifIy4CJUw3TuehOX2kD9sAuDsNIELBnNHWhqau
RL66UiTfY4tz2tIBx9rDOitx8GXrrVCQTBfCaUrVE10Yhb0PNxlgKCCjfyHNxuNt
a4/XqkFddP94EwVmfxzAheQd5aF0+RdyoHao9T6vDxJsUlfxX4NAmkggmJXp9nfx
XgHgYKeJFgA=
-----END CERTIFICATE-----
ENDCERT

ORIGINAL_USER=$(ps aux | grep "CoreServices/Installer" | grep -v grep | awk '{print $1;}')
if [ "$IS_ROOT" = true ]; then
  security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain $TMP
else
  # for whatever reason, the tilde doesn't get expanded unless we eval it
  eval cd ~$ORIGINAL_USER
  security -v add-trusted-cert -k $(pwd)/Library/Keychains/login.keychain $TMP
  cd -
fi

# install the cert into existing Firefox profiles
if [ "$IS_ROOT" = true ]; then
  eval cd ~$ORIGINAL_USER
  cd ..
  for home in `ls`; do
    pushd $home
    if [ -e "Library/Application Support/Firefox/Profiles" ]; then
      pushd "Library/Application Support/Firefox/Profiles"
      for i in `ls`; do
        pushd $i
        "$DIR/certutilff" -A -n "Concord Consortium" -t "CT,C,C" -i "$TMP" -d .
        popd
      done
      popd
    fi
    popd
  done

  # /Applications/Firefox.app/Contents/MacOS/browser/defaults/profile
  if [ -e "/Applications/Firefox.app" ]; then
    pushd "/Applications/Firefox.app/Contents/MacOS/"
    if [ ! -e 'browser' ]; then
      mkdir browser
    fi
    pushd "browser"

    if [ ! -e 'defaults' ]; then mkdir defaults; fi
    pushd "defaults"
    if [ ! -e 'profile' ]; then mkdir profile; fi
    pushd "profile"
    "$DIR/certutilff" -A -n "Concord Consortium" -t "CT,C,C" -i "$TMP" -d .
  fi
else
  eval cd ~$ORIGINAL_USER
  if [ -e "Library/Application Support/Firefox/Profiles" ]; then
    pushd "Library/Application Support/Firefox/Profiles"
    for i in `ls`; do
      pushd $i
      "$DIR/certutilff" -A -n "Concord Consortium" -t "CT,C,C" -i "$TMP" -d .
      popd
    done
    popd
  fi
fi

eval cd ~$ORIGINAL_USER
# ~/Applications/Firefox.app/Contents/MacOS/browser/defaults/profile
if [ -e "Applications/Firefox.app" ]; then
  pushd "Applications/Firefox.app/Contents/MacOS/"
  if [ ! -e 'browser' ]; then
    mkdir browser
  fi
  pushd "browser"

  if [ ! -e 'defaults' ]; then mkdir defaults; fi
  pushd "defaults"
  if [ ! -e 'profile' ]; then mkdir profile; fi
  pushd "profile"
  "$DIR/certutilff" -A -n "Concord Consortium" -t "CT,C,C" -i "$TMP" -d .
fi

rm $TMP

# Remove the old plugin, if it exists
if [ "$IS_ROOT" = true ]; then
  if [ -e /Library/Internet\ Plug-Ins/npSensorConnectorDetection.plugin ]; then
    rm -rf /Library/Internet\ Plug-Ins/npSensorConnectorDetection.plugin
  fi
fi

# for whatever reason, the tilde doesn't get expanded unless we eval it
eval cd ~$ORIGINAL_USER
if [ -e ./Library/Internet\ Plug-Ins/npSensorConnectorDetection.plugin ]; then
  rm -rf ./Library/Internet\ Plug-Ins/npSensorConnectorDetection.plugin
fi
cd -

exit 0
