# SensorConnector

SensorConnector is a desktop application which connects to sensors made by Vernier and Pasco and serves up the collected data so that it is accessible to browser-based applications written in JavaScript. It is written in Java and runs natively on Mac OS and Windows machines. See http://sensorconnector.concord.org/ for more information and to download the application.

Note that currently SensorConnector can only connect to one interface at a time, e.g. one LabQuest or one Go! device. Multiple sensors are supported for interfaces such as the LabQuest that support multiple sensors.

## Usage

The SensorConnector application responds to http/https requests to the following addresses/ports:
- http://127.0.0.1:11180
- https://127.0.0.1:11181

The server responds to 'GET' requests at the following API endpoints. Where responses are provided, they are JSON-formatted UTF8-encoded.

### / or /status : IStatusReceivedTuple

Response is the current status of the SensorConnector, including the interface connected (if any), the sensors that are connected, etc. The response is a two-element array in which the sensor configuration is in `response[1]`. The response has this form as a TypeScript definition:
```
interface ISensorConfigColumnInfo {
  id:string;
  setID:string;
  position:number;
  name:string;
  units:string;
  liveValue:string;
  liveValueTimeStamp:Date;
  valueCount:number;
  valuesTimeStamp:Date;
  data?:number[];
}

interface ISensorConfigSet {
  name:string;
  colIDs:number[];
}

interface ISensorConfig {
  collection:{ canControl:boolean; isCollecting:boolean; };
  columnListTimeStamp:Date;
  columns:{ [key:string]: ISensorConfigColumnInfo; };
  currentInterface:string;
  currentState:string;
  os:{ name:string; version:string; };
  requestTimeStamp:Date;
  server:{ arch:string; version:string; };
  sessionDesc:string;
  sessionID:string;
  sets:{ [key:string]: ISensorConfigSet; };
  setID?:string;
}

interface IMachinaAction {
  inputType:string;
  delegated:boolean;
  ticket:any;
}

interface IStatusReceivedTuple
  extends Array<IMachinaAction|ISensorConfig>
            {0:IMachinaAction, 1:ISensorConfig}
```

### /connect

Scan for available devices to connect to and attempt to connect to the first one found. Clients may not need to call this method as the SensorConnector application attempts to connect automatically during application initialization. The response is the SensorConnector state at the time the request was processed.

### /disconnect

Disconnect from any connected device. Clients may not need to call this method as the SensorConnector application attempts to disconnect automatically on application termination. The response is the SensorConnector state at the time the request was processed.

### /control/start

Start collecting data. The response is the SensorConnector state at the time the request was processed, e.g. `{"currentState":"CONNECTED:POLLING"}`.

### /control/stop

Stop collecting data. The response is the SensorConnector state at the time the request was processed, e.g. `{"currentState":"CONNECTED:COLLECTING"}`.

### /columns/{columnID} : IColumnDataTuple

Responds with collected data for the specified column. The format of the response is a four-element array in which:
- `response[1]`: the column ID
- `response[2]`: the column data values
- `response[3]`: the timestamp of the data values

The column data values in `response[2]` are time values for column IDs that end in '0', and sensor data values for all other column IDs. The response has this form as a TypeScript definition:
```
interface IMachinaAction {
  inputType:string;
  delegated:boolean;
  ticket:any;
}

interface IColumnDataTuple
  extends Array<IMachinaAction|string|number[]|Date>
            {0:IMachinaAction, 1:string, 2:number[], 3:Date}
```

## Development
### Pre-requisites:
#### All platforms

- [**Ant**](http://ant.apache.org/) (any recent version)
- [**Maven**](http://maven.apache.org/) (any recent version)
- A **32-bit JDK** (for now, the Pasco libraries require 32-bit -- see notes for your platform), or a **64-bit JDK** if you don't care about supporting Pasco devices
- the JDK used to build the installer needs to be Java 7 or higher

#### Windows

- a [**32-bit JDK**](http://www.oracle.com/technetwork/java/javase/overview/index.html) downloaded from Oracle. Java 6 is minimum, Java 7 is OK. Java 8 is untested.
- (optional) the [**WiX Toolset**](http://wixtoolset.org/) (to create an MSI installer)
- (optional) the [Microsoft Windows SDK](http://msdn.microsoft.com/en-US/windows/desktop/aa904949) (to sign the resulting .exe and .msi)

##### Signing (Windows)
For more info on signing apps and packages on Windows, these step-by-step instructions seem pretty good:
- [Installing your signing certificate and private key](http://support.godaddy.com/help/article/2698/installing-a-code-signing-certificate-in-windows)
- [Signing Code with Microsoft Signtool](http://support.godaddy.com/help/article/4778/signing-windows-code-with-microsoft-signtool?locale=en)

#### OS X

- the last [**Java 6 release**](http://support.apple.com/kb/DL1572?viewlocale=en_US) is the only available 32-bit JDK
- PackageMaker is needed to build the installation package. Log into developer.apple.com with your free dev account. Then go here: https://developer.apple.com/downloads/index.action and search for PackageMaker. It should be listed under Auxillary Tools for XCode. Make sure to download the latest one.

## Building the installer

### Windows

After installing the pre-requisites, fire up your terminal. I use Git Bash, but it should also work with cmd.

- Make sure the JAVA_HOME environment variable is set to your 32-bit JDK:

`export JAVA_HOME="C:\Program Files (x86)\Java\jdk1.7.0_51"`

- (optional) If you want to sign the .exe, make sure to put signtool.exe on the PATH, import your key and certificate into your machine's keystore, and export the CERT_NAME enviroment variable with the name of your signing certificate:

```
export CERT_NAME="Concord Consortium"

```

- From the sensor-connector root, run in the console:

`ant fx-package`

- The **.exe** will be created as `dist/bundles/SensorConnector/SensorConnector.exe`
- The **.msi** will be created as `dist/bundles/SensorConnector-{TIMESTAMP}-1.0.msi`

### OS X (32-bit)

After installing the pre-requisites, fire up your terminal.

- Make sure the JAVA_HOME environment variable is set to your normal JDK:

``export JAVA_HOME=`/usr/libexec/java_home` ``

- Make sure the JRE_HOME environment variable is set to your Java 6 JDK:

`export JRE_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK`

- (optional) If you want to sign the resulting app, make sure your code signing certificate and key are in the keychain, and export the certificate name in the CERT_NAME environment variable:

`export CERT_NAME="Developer ID Application: Concord Consortium Inc (T8HS8WBPPQ)"`

- From the sensor-connector root, run in the console:

`ant mac-package`

- The **application** will be created as `dist/app/SensorConnector.app`
- The **.dmg** with the application will be created as `dist/SensorConnector-{TIMESTAMP}-1.dmg`

### OS X (64-bit)

After installing the pre-requisites, fire up your terminal.

- Make sure the JAVA_HOME environment variable is set to your normal JDK:

``export JAVA_HOME=`/usr/libexec/java_home` ``

- Make sure the JRE_HOME environment variable is set to your 64-bit JDK:

``export JRE_HOME=`/usr/libexec/java_home` ``

- (optional) If you want to sign the resulting app, make sure your code signing certificate and key are in the keychain, and export the certificate name in the CERT_NAME environment variable:

`export CERT_NAME="Developer ID Application: Concord Consortium Inc (T8HS8WBPPQ)"`

- From the sensor-connector root, run in the console:

`ant mac-package`

- The **application** will be created as `dist/app/SensorConnector.app`
- The **.dmg** with the application will be created as `dist/SensorConnector-{TIMESTAMP}-1.dmg`

## Updating the server certificate

- Generate the new certificate keystore using the tools in the [ssl-ca](https://github.com/concord-consortium/ssl-ca) project.
- Replace the existing keystore file: `src/main/resources/server.jks`
