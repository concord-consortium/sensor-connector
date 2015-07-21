
%SYSTEMROOT%\System32\certutil.exe -delstore ROOT 00b0c498029e051aed

:: Script removes Concord Certificate from independent Firefox certificate store since
:: the browser does not use the Windows built in certificate store

cd "C:\Users\"
for /D %%G in ("*") DO (
  pushd %%G
  IF EXIST "AppData\Roaming\Mozilla\Firefox\Profiles\" (
    pushd "AppData\Roaming\Mozilla\Firefox\Profiles\"
    for /D %%H in ("*") DO (
      pushd %%H
      "%~dp0\certutilff.exe" -D -n "Concord Consortium" -d .
      popd
    )
    popd
  )
  popd
)

:: Also remove it from the default firefox profile, so that new profiles will not get the cert.
if EXIST "C:\Program Files (x86)\Mozilla Firefox\browser\defaults\profile\cert8.db" (
  pushd "C:\Program Files (x86)\Mozilla Firefox\browser\defaults\profile"
  "%~dp0\certutilff.exe" -D -n "Concord Consortium" -d .
  popd
)

exit 0
