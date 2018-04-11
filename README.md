# SensorConnector

SensorConnector is a desktop application which connects to sensors made by Vernier and Pasco and serves up the collected data so that it is accessible to browser-based applications written in JavaScript. It is written in Java and runs natively on Mac OS and Windows machines. See http://sensorconnector.concord.org/ for more information and to download the application.

Note that currently SensorConnector can only connect to one interface at a time, e.g. one LabQuest or one Go! device. Multiple sensors are supported for interfaces such as the LabQuest that support multiple sensors.

## Supported Interfaces

### Vernier

- LabQuest Mini/LabQuest Stream?/LabQuest 2?
- Go!Link, Go!Temp, Go!Motion
- LabPro

### Pasco

- ?

## Usage

The SensorConnector application responds to http/https requests to the following addresses/ports:
- http://127.0.0.1:11180
- https://127.0.0.1:11181

The server responds to 'GET' requests at the following API endpoints. Where responses are provided, they are JSON-formatted UTF8-encoded.

On Mac OS and Windows the SensorConnector application also registers `ccsc` (**C**oncord **C**onsortium **S**ensor**C**onnector) as a custom URL scheme and file extension.

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

### /exit

Disconnect from any connected device and quit the SensorConnector application. Quitting and restarting the SensorConnector application is sometimes useful as a troubleshooting technique. Note that this sends a request to the SensorConnector application. The SensorConnector application must be running and processing incoming requests for it to have the desired effect.

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
- PackageMaker is needed to build the installation package. Log into developer.apple.com with your free dev account. Then go here: https://developer.apple.com/downloads/index.action and search for PackageMaker. It should be listed under Auxiliary Tools for XCode. Make sure to download the latest one.

## October 2017 Update

Previously, building the SensorConnector application required the use of a Concord Consortium-hosted Maven repository which provided access to various Concord Consortium-developed JARs as well as some third-party JARs. Since Concord Consortium no longer runs its own Maven repository, the required JARs must be installed into the local Maven repository to build the SensorConnector application.

### Install SensorProjects

[SensorProjects](https://github.com/concord-consortium/sensor-projects) is a separate Concord Consortium project which builds JARs that are used by the SensorConnector application. To build and install the SensorProjects JARs into the local Maven repository, cd to an appropriate directory (e.g. the parent of the sensor-connector directory, so that sensor-projects will be next to sensor-connector) and run:
```
git clone https://github.com/concord-consortium/sensor-projects.git
cd sensor-projects
mvn install
``` 

### Install Tungsten-FSM

The SensorConnector application also requires a third-party finite state machine library named `Tungsten-FSM`. This is a component of a larger open source project call the [Tungsten Replicator](https://github.com/continuent/tungsten-replicator). As I write this, the shipping version of SensorConnector was built with version 1.0 of Tungsten-FSM, which was available from the Concord Consortium-hosted Maven repository. I was able to track down version 1.2 of Tungsten-FSM, which is now included in the `lib` folder and which appears to have the same API, but this combination will require further testing.

To build and install the local Tungsten-FSM version 1.1, run
```
./script/install-local.sh
```
### LSOpenURLsWithRole() failed ... with error -10810

With these changes, the 32-bit SensorConnector application builds and launches successfully. The 64-bit SensorConnector build also completes successfully, but attempting to launch the application results in the following error message:
```
LSOpenURLsWithRole() failed for the application </path/to/application> with error -10810.
```
From https://www.osstatus.com, `-10810` corresponds to `kLSUnknownErr`, which is not particularly helpful.

This [blog post](http://dclunie.blogspot.com/2014/10/keeping-up-with-mac-java-bundling-into.html) suggests that one possibility is that the packaging requirements for Java applications has changed. In fact, while Oracle's [Java 7 instructions](http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/packagingAppsForMac.html) for packaging applications refers to `AppBuilder`, the [Java 8 instructions](https://docs.oracle.com/javase/8/docs/technotes/guides/deploy/self-contained-packaging.html) refer to `fx:deploy` instead.

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
- The **.dmg** with the application will be created as `dist/SensorConnector-{TIMESTAMP}.dmg`

To build the application without packaging the .dmg, run in the console:

` ant mac-stage`

- The **application** will be created as `dist/app/SensorConnector.app`

### OS X (64-bit)

After installing the pre-requisites, fire up your terminal.

- Make sure the JAVA_HOME environment variable is set to your normal JDK:

``export JAVA_HOME=`/usr/libexec/java_home` ``

- Make sure the JRE_HOME environment variable is set to your 64-bit JDK:

``export JRE_HOME=`/usr/libexec/java_home` ``

- (optional) If you want to sign the resulting app, make sure your code signing certificate and key are in the keychain, and export the certificate name in the CERT_NAME environment variable:

`export CERT_NAME="Developer ID Application: Concord Consortium Inc (T8HS8WBPPQ)"`

- From the sensor-connector root, run in the console:

`ant mac-package-x64`

- The **application** will be created as `dist/app/SensorConnector.app`
- The **.dmg** with the application will be created as `dist/SensorConnector-x64-{TIMESTAMP}.dmg`

To build the application without packaging the .dmg, run in the console:

` ant mac-stage-x64`

- The **application** will be created as `dist/app/SensorConnector.app`

### mac-build.sh script

The `mac-build.sh` script will build the 32-bit and 64-bit packages, setting the `JAVA_HOME` and `JRE_HOME` environment variables appropriately for each build, and copy the resulting .dmg files into the project directory. To run it:
```
./script/mac-build.sh
```

## Updating the server certificate

- Generate the new certificate keystore using the tools in the [ssl-ca](https://github.com/concord-consortium/ssl-ca) project.
- Replace the existing keystore file: `src/main/resources/server.jks`
