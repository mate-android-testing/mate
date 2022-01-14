# Automated Android Testing with MATE
MATE is a tool for automated android app testing featuring various (genetic)
algorithms. The project is divided into two parts: the MATE framework and unit
tests for android managed in this repository and a server that runs on a host
machine which is responsible for some analysis that can not be performed by the
instrumentation tests themselves. The repository containing the MATE server can be found
[here](https://github.com/mate-android-testing/mate-server).

## Supported Devices:

We have tested MATE on the following devices:

* Nexus 5 (API 25 & 28)
* Pixel C (API 25 & 28)
* Pixel XL (API 25 & 28)

You have to employ on the emulator a *x86* image that is not provided by *Google*, i.e. don't use
neither a *Google Play* nor a *Google API* image. This is due to recent changes that otherwise lead
to permission issues.

## How to run MATE
### 1) Start the device
Setup the emulator or attach your device via USB and enable USB-Debugging.
Install and open(!) the app you want to test.

### 2) Build and run MATE server
Refer to the [instructions](https://github.com/mate-android-testing/mate-server/blob/master/README.md)
from the MATE server repository.

### 3) Installing and running MATE
#### a) Android Studio (for Developers)
Open Android Studio. Select "Check out project from Version Control" and click
through the wizard (use git with url https://github.com/mate-android-testing/mate.git).
Install Android Sdk when prompted.

To run a test use the project browser to find the unit tests (either
`app -> java -> org.mate (androidTest)` or
`mate -> app -> src -> androidTest -> java -> org.mate`) and
`rightclick -> run '<testName>'`.
Note, you have to provide the package name of the AUT as
instrumentation argument (adjust the run configuration).

#### b) Gradle
Install the Android Sdk and set the $ANDROID_HOME environment variable.
Clone the git repository
```
git clone https://github.com/mate-android-testing/mate.git
```
Change into the project folder
```
cd mate
```
Install MATE and MATE unit tests (use gradlew.bat instead when using Windows):
```
./gradlew installDebug
./gradlew installDebugAndroidTest
```
Execute test over adb:
```
adb shell am instrument -w -r -e debug false -e packageName <package-name-of-aut> -e class \
    'org.mate.ExecuteMATERandomExploration' \
    org.mate.test/android.support.test.runner.AndroidJUnitRunner
```
Replace `ExecuteMATERandomExploration` with the desired testing strategy and `<package-name-of-aut>` with the package name of the AUT. In order to retrieve the package name of an APK, you have two points:
1) Run the command `aapt dump badging <path-to-apk> | grep package:\ name`.
2) Extract the package name from the `AndroidManifest.xml`.

### 4) Further configurations

Most properties necessary for the various algorithms can be now specified dynamically by providing a file called `mate.properties`.
For instance, you can run an arbitrary genetic algorithm by
specifying `org.mate.ExecuteMATEGeneticAlgorithm`
as testing strategy (see above) and provide the following properties file:
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
