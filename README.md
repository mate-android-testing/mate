# Automated Android Testing with MATE
MATE is a tool for automated Android app testing featuring various algorithms. 
The project is divided into two parts:

- The MATE framework: a special Android app and instrumentation test in charge of performing the exploration using the various algorithms.
- The MATE Server: a server that runs on the host machine. It is responsible for some analysis that can not be performed by the exploration itself.

The MATE framework (this repository) is composed of three different modules:

- MATE Client: an Android app containing the code of the different algorithms available for test generation.
- MATE Representation Layer: a special Android instrumentation test, used for retrieving the state of the Application Under Test, and executing actions on it.
- MATE Commons: an Android library, with code used in both Client and Representation Library modules.

The repository containing the MATE server can be found
[here](https://github.com/mate-android-testing/mate-server).

## Supported Devices:

We have tested MATE on the following devices:

* Nexus 5 (API 25 & 28 & 29 & 30)
* Pixel C (API 25 & 28 & 29)
* Pixel XL (API 25 & 28 & 29)

You have to employ on th emulator either a *x86* image or a *Google API* image, but not a
*Google Play* image. This is due to recent changes that otherwise lead to permission issues.

## How to run MATE

The simplest option is to make use of the supplied `mate-commander`:
* [Windows](https://github.com/mate-android-testing/mate-commander/tree/mate-commander-windows) 
* [Linux](https://github.com/mate-android-testing/mate-commander) 

If you however want to run MATE and MATE Server manually follow the instructions below:

### 0) Prepare the APK

First, we are going to need the package name of the Application Under Test (AUT).
If you need to get this information from an APK, you can run: `aapt dump badging <path-to-apk> | grep "package: name"`

Second, the AUT's APK needs to be signed with the Android debug key.
To achieve this for an existing APK, you can use the `apksigner` provided in the Android SDK and the default Android debug key.
This command changes the APK in-place, so you might want to make a copy first.
```
apksigner sign --ks ~/.android/debug.keystore --ks-key-alias androiddebugkey --ks-pass pass:android --key-pass pass:android <path-to-apk>
```

### 1) Start the device
Setup the emulator or attach your device via USB and enable USB-Debugging.
Install the re-signed APK of the app you want to test.
```
adb install <path-to-re-signed-apk>
```

### 2) Build and run MATE Server
Refer to the [instructions](https://github.com/mate-android-testing/mate-server/blob/master/README.md)
from the MATE server repository.

### 3) Installing and running MATE
Install the Android SDK and set the $ANDROID_HOME environment variable.

Clone the git repository
```
git clone https://github.com/mate-android-testing/mate.git
```

Change into the project folder
```
cd mate
```

Build and install MATE Client (use gradlew.bat instead when using Windows)
```
./gradlew :client:clean :client:installDebug
```

Build and install MATE's Representation Layer (use gradlew.bat instead when using Windows)
```
./gradlew -PtargetPackage=<package-name-of-aut> :representation:clean :representation:installDebugAndroidTest
```

Start the MATE Client with the desired algorithm.
The available algorithm names can be found in the [MATE Runner class](client/src/main/java/org/mate/service/MATERunner.java).
```
adb shell am start-foreground-service -n org.mate/.service.MATEService -e packageName <package-name-of-aut> -e algorithm <mate-algorithm>
```

In previous versions of the Android emulator, the command is `startservice`, instead of `start-foreground-service`.

### 4) Debug MATE

To debug the execution of the MATE Client, add the following flag to the previous command: `--ez waitForDebugger true`.
Then in the Android Studio IDE, click on `Run -> 'Attach Debugger to Android Process' -> 'org.mate'` once the log "Waiting for debugger..." appears in the Android logcat output.

### 5) Further configurations

Most properties necessary for the various algorithms can be now specified dynamically by providing a file called `mate.properties`.
For instance, you can run an arbitrary genetic algorithm by
specifying `GeneticAlgorithm`
as testing strategy in the above `am` command, and provide the following properties file:

```
# the time out in minutes
timeout=5

# which type of coverage should be recorded
coverage=ACTIVITY_COVERAGE

# how many actions per test case
max_number_events=10

# whether to record test cases for possible replaying
record_test_case=true

# genetic properties (test case chromosome)
population_size=5
big_population_size=10
algorithm=STANDARD_GA
fitness_function=NUMBER_OF_ACTIVITIES
selection_function=FITNESS_PROPORTIONATE_SELECTION
mutation_function=TEST_CASE_CUT_POINT_MUTATION
crossover_function=TEST_CASE_MERGE_CROSS_OVER
termination_condition=NEVER_TERMINATION
chromosome_factory=ANDROID_RANDOM_CHROMOSOME_FACTORY
```
The available properties can be looked up in the class
`org.mate.Properties`.

## Algorithm Documentation
For more information on grammatical evolution refer to the [documentation](doc/GrammaticalEvolution.md).
