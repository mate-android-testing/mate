# Automated Android Testing with MATE
MATE is a tool for automated android app testing featuring various (genetic)
algorithms. The project is divided into two parts: the MATE framework and unit
tests for android managed in this repository and a server that runs on a host
machine which is responsible for some analysis that can not be performed by the
unit tests themselves. The repository containing the MATE server can be found
[here](https://gitlab.infosun.fim.uni-passau.de/fraser/mate-server).

## How to run MATE
### 1) Start the device
Setup the emulator or attach your device via USB and enable USB-Debugging. Install and open(!) the app you want to test.

### 2) Build and run MATE server
Refer to the [instructions](https://gitlab.infosun.fim.uni-passau.de/fraser/mate-server/blob/master/README.md) from the MATE server repository.

### 3) Installing and running MATE
#### a) Android Studio (for Developers)
Open Android Studio. Select "Check out project from Version Control" and click through the wizard. Install Android Sdk when prompted. 

To run a test use the project browser to find the unit tests (either `app -> java -> org.mate (androidTest)` or `mate -> app -> src -> androidTest -> java -> org.mate`) and `rightclick -> run '<testName'`.

#### b) Gradle
Install the Android Sdk and set the $ANDROID_HOME environment variable.
Clone the git repository
```git clone https://gitlab.infosun.fim.uni-passau.de/fraser/mate.git```
Change into the project folder
```cd mate```
Install MATE and MATE unit tests (use gradlew.bat instead when using Windows):
```./gradlew installDebug```
```./gradlew installDebugAndroidTest```
Execute test over adb:
```adb shell am instrument -w -r -e debug false -e class 'org.mate.ExecuteMATEAccTestingRandom' org.mate.test/android.support.test.runner.AndroidJUnitRunner```
Replace `ExecuteMATEAccTestingRandom` with desired test.
