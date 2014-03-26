# Sensor Server

## Pre-requisites:
### All platforms

- [**Ant**](http://ant.apache.org/) (any recent version)
- [**Maven**](http://maven.apache.org/) (any recent version)
- A **32-bit JDK** (for now, the Pasco libraries require 32-bit -- see notes for your platform)

#### Windows

- a [**32-bit JDK**](http://www.oracle.com/technetwork/java/javase/overview/index.html) downloaded from Oracle. Java 6 is minimum, Java 7 is OK. Java 8 is untested.
- (optional) the [**WiX Toolset**](http://wixtoolset.org/) (to create an MSI installer)
- (optional) the [Microsoft Windows SDK](http://msdn.microsoft.com/en-US/windows/desktop/aa904949) (to sign the resulting .exe and .msi)

##### Signing (Windows)
For more info on signing apps and packages on Windows, these step-by-step instructions seem pretty good:
- [Installing your signing certificate and private key](http://support.godaddy.com/help/article/2698/installing-a-code-signing-certificate-in-windows)
- [Signing Code with Microsoft Signtool](http://support.godaddy.com/help/article/4778/signing-windows-code-with-microsoft-signtool?locale=en)

#### OS X

- the last [**Java 6 release**](http://support.apple.com/kb/DL1572?viewlocale=en_US)

## Building the installer

### Windows

After installing the pre-requisites, fire up your terminal. I use Git Bash, but it should also work with cmd.

- Make sure the JAVA_HOME environment variable is set to your 32-bit JDK:

`export JAVA_HOME="C:\Program Files (x86)\Java\jdk1.7.0_51"`

- (optional) If you want to sign the .exe, make sure to put signtool.exe on the PATH, and export the following enviroment variables with the name of your signing certificate:

```
export CERT_NAME="Your Cert's Name"

```

- From the sensor-server root, run in the console:

`ant fx-package`

- The **.exe** will be created as `dist/bundles/SensorServer/SensorServer.exe`
- The **.msi** will be created as `dist/bundles/SensorServer-1.0.msi`

### OS X

After installing the pre-requisites, fire up your terminal.

- Make sure the JAVA_HOME environment variable is set to your normal JDK:

``export JAVA_HOME=`/usr/libexec/java_home` ``

- Make sure the JRE_HOME environment variable is set to your Java 6 JDK:

`export JRE_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK`

- If you want to sign the resulting app, make sure your code signing certificate and key are in the keychain, and export the certificate name in the CERT_NAME environment variable:

`export CERT_NAME="Developer ID Application: Concord Consortium Inc (T8HS8WBPPQ)"`

- From the sensor-server root, run in the console:

`ant mac-package`

- The **application** will be created as `dist/app/SensorServer.app`
- The **.dmg** with the application will be created as `dist/sensor_server_installer.dmg`
