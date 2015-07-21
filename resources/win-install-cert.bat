cd %~dp0

%SYSTEMROOT%\System32\certutil.exe -addstore ROOT ca.cert.pem

:: Script adds Concord Certificate to independent Firefox certificate store since
:: the browser does not use the Windows built in certificate store

cd "C:\Users\"
for /D %%G in ("*") DO (
  pushd %%G
  IF EXIST "AppData\Roaming\Mozilla\Firefox\Profiles\" (
    pushd "AppData\Roaming\Mozilla\Firefox\Profiles\"
    for /D %%H in ("*") DO (
      pushd %%H
      "%~dp0\certutilff.exe" -A -n "Concord Consortium" -t "CT,C,C" -i "%~dp0\ca.cert.pem" -d .
      popd
    )
    popd
  )
  popd
)

:: Also add it to the default firefox profile, so that new profiles will get the cert.
if EXIST "C:\Program Files (x86)\Mozilla Firefox\" (
  pushd "C:\Program Files (x86)\Mozilla Firefox\"
  if NOT EXIST "browser" ( md browser )
  pushd "browser"
  if NOT EXIST "defaults" ( md defaults )
  pushd "defaults"
  if NOT EXIST "profile" ( md profile )
  pushd "profile"
  if NOT EXIST "cert8.db" (
    copy "%~dp0\cert8.db" .
    copy "%~dp0\key3.db" .
    copy "%~dp0\secmod.db" .
  ) else (
    "%~dp0\certutilff.exe" -A -n "Concord Consortium" -t "CT,C,C" -i "%~dp0\ca.cert.pem" -d .
  )
  popd
  popd
  popd
)


exit 0
