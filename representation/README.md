# MATE's Representation Layer

This project allows MATE to gather and execute available actions in the Application Under Test (AUT).

It works as an special instrumented test running in same process as the AUT.
To achieve this, we have to met two conditions:

- The `androidTest` Representation's APK and AUT's APK must be signed with the same signature.
- The `androidTest` Representation's APK must have the AUT's package name as the `targetPackage` in the AndroidManifest 

After it has started, it will communicate with MATE's Client using Android RPC.

## How to build

We will assume that the AUT's package name is `con.example`. 
From the root of this repository run the following:

`./gradlew -PtargetPackage=com.example :representation:clean :representation:assembleDebugAndroidTest`

This will take care of building the `androidTest` APK and setting the appropriate `targetPackage` property in manifest.
Output can be found at `./representation/build/outputs/apk/androidTest/debug/representation-debug-androidTest.apk`

You can check that it has the proper target package name by executing:
`aapt dump xmltree ./representation/build/outputs/apk/androidTest/debug/representation-debug-androidTest.apk AndroidManifest.xml | grep targetPackage`

## How to change AUT's APK signature

You can use the `apksigner` provided in the Android SDK and the default Android debug key.
This command changes the APK in-place, so you might want to make a copy first.

`apksigner sign --ks ~/.android/debug.keystore --ks-key-alias androiddebugkey --ks-pass pass:android --key-pass pass:android <path-to-APK>`

## How to run

- Install AUT's APK: `adb install <path-toAPK>`
- Install the `androidTest` APK: `adb install ./representation/build/outputs/apk/androidTest/debug/representation-debug-androidTest.apk`
- Run DynamicTest: `adb shell am instrument -w -e class 'org.mate.representation.DynamicTest' org.mate.representation.test/androidx.test.runner.AndroidJUnitRunner`