# Setting Up Windows for SensorConnector Development on VirtualBox

This document describes the steps involved in setting up a Windows SensorConnector environment to create 32 and 64 bits builds of the SensorConnector program on Windows using VirtualBox. Most of the instructions should be the same for a non-VirtualBox Windows environment (other than the VirtualBox-specific steps, of course).  Non-VirtualBox Windows environment has been tested on a 64 bit machine using Windows 10 Home Edition.

## Create/configure VirtualBox instance

1. Download VirtualBox if necessary
1. Get an appropriate Windows ISO image and license (e.g. 32-bit Windows 7)
   - Windows ISO images can be downloaded from Microsoft in some cases or may be easier to get from your IT department directly.
1. Configure RAM, CPU, storage, etc. (e.g. 2 CPUs max 80%, 4GM RAM, 64GB dynamically sized VDI HD)
1. Configure optical to load downloaded Windows ISO image
1. Start the Virtual Machine

## Install/configure Windows

1. Set up account (a name without spaces makes life a bit easier)
1. Run Windows Update (or equivalent) to install all appropriate updates, service packs, etc.
   - For some systems (e.g. Windows 7), this can require a ridiculous number of Windows Update - reboot - Windows Update - reboot - Windows Update cycles.
   - Note that you can proceed with downloading/installing additional applications and development tools while Windows Update is running in the background.
1. Download Google Chrome or Mozilla Firefox (unless you prefer using IE/Edge for the remaining steps)

## Install Development Tools
1. Install [Visual Studio Code](https://code.visualstudio.com) (or another editor) to use as Git's default editor (VS Code v1.25.1 in this case)
1. Install [Git](https://git-scm/download/win) (v2.18 at this writing)
1. Create a folder for SensorConnector development (e.g. `sensors`)
1. Launch Git Bash
   - Git clone the [sensor-connector](https://github.com/concord-consortium/sensor-connector) and [sensor-projects](https://github.com/concord-consortium/sensor-projects) projects
1. Install 32-bit Java SE JDK (v1.8.0_161 in this case) (http://www.oracle.com/technetwork/java/javase/downloads)
   - Add/set `JAVA_HOME` to point to the JDK in System Environment Variables in Control Panel
   - Add/set `JRE_HOME` to point to the JRE in System Environment Variables in Control Panel
   - Note: In Git Bash, you can use `ls "${JAVA_HOME}"` to verify that an environment variable path is set correctly.
   - Note: be sure any existing references to Java installations are removed from System Environment Variables including the `Path` System Environment Variable
1. Install 64-bit Java SE JDK (v1.8.0_161 in this case) (http://www.oracle.com/technetwork/java/javase/downloads)
   - Note: will be needed later to make 64 bit build, will update System Environment Variables when we make 64 bit build
   - Note: can skip this step if only making 32 bit build 
1. Install [Apache Maven](https://maven.apache.org) (v3.5.4 at this writing)
   - Add `MAVEN_HOME` and `M2_HOME` (both pointing at install directory) to System Environment Variables in Control Panel
   - Add the Apache Maven `bin` directory to the `Path` System Environment Variable in Control Panel
1. Install [Apache Ant](https://ant.apache.org) (v1.10.5 at this writing)
   - Add `ANT_HOME` to System Environment Variables in Control Panel
   - Add the Apache Ant `bin` directory to the `Path` System Environment Variable in Control Panel
1. Install the [WiX Toolset](https://wixtoolset.org) (v3.11.1 at this time)
   - Add the WiX Toolset `bin` directory to the `Path` System Environment Variable in Control Panel
1. Install the [Windows Development Kit](https://msdn.microsoft.com/en-us/microsoft-sdks-msdn.aspx#windows) appropriate for the installed version of Windows
   - Add the Windows Development Kit `Bin` directory to the `Path` System Environment Variable in Control Panel
   - If installing 64 bit verson of SDK, add the Windows Development Kit 64 bit `Bin` directory to the `Path` System Environment Variable in Control Panel (installed directory\Windows Kits\10\bin\10.0.17134.0\x64 in this case)   
1. [Optional] Install the [SysinternalsSuite](https://docs.microsoft.com/en-us/sysinternals/downloads/sysinternals-suite) which provides useful utilities for things like inspecting binaries.
   - Add the [SysinternalsSuite] to the `Path` System Environment Variable in Control Panel

## Test the Hardware
1. Install Vernier's [Logger Lite](https://www.vernier.com/products/software/logger-lite/) application
1. Run Logger Lite to verify that the hardware connection is working (Logger Lite v. 1.9.4 in this case)
   - Note that you have to explicitly capture/release the USB device for VirtualBox

## Test SensorProjects Build
1. cd to the sensor-projects directory
1. Type `mvn install` in the Git Bash console to start the build
   - build should complete successfully

## Run SensorProjects Unit Tests
1. Enable the individual unit test(s) of interest
   - For Vernier Unit Tests, comment out the appropriate exclusion line in `sensor-projects/sensor-vernier/pom.xml`. For example, comment out the GoIO line to run the unit tests for the Go!Link.
1. Type `mvn install` in the Git Bash console to run the unit test
   - Note that you have to explicitly capture/release the USB device for VirtualBox
1. Repeat the previous steps for each interface to be tested
1. When done with unit tests, return pom.xml files to their original state

## Build SensorProjects (32 bit version)
1. cd to the sensor-projects directory
1. Type `mvn install` in the Git Bash console to start the build
   - build should complete successfully

## Build SensorConnector (32 bit version)
1. cd to the sensor-connector directory
1. Install dependencies (including Tungsten). In bash console type `./script/install-local.sh`
1. For Windows, in bash console, type `./script/win-build.sh -32`
1. A `SensorConnector-*.msi` and a `SensorConnector-*.exe` should be created

## Modifying setup to create a 64 bit build
1. Modify Java environment variables
   - Modify `JAVA_HOME` to point to the 64 bit JDK in System Environment Variables in Control Panel
   - Modify `JRE_HOME` to point to the 64 bit JRE in System Environment Variables in Control Panel
   - Note: In Git Bash, you can use `ls "${JAVA_HOME}"` to verify that an environment variable path is set correctly.
   - Note: be sure any existing references to the 32 bit Java installation are removed from System Environment Variables including the `Path` System Environment Variable 
   - Note: be sure to close and reopen any command prompts or Git Bash windows after changing environment variables
1. Rebuild SensorProjects, in bash console type `mvn install` (be sure to open new command prompt or Git Bash console)
1. Rebuild SensorConnector, in bash console, type `./script/win-build.sh -64` (be sure to open new Git Bash console)
1. A `SensorConnector-*.msi` and a `SensorConnector-*.exe` should be created
