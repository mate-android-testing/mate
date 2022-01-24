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

This will take care of building the `androidTest` APK and setting the appropiate `targetPackage` property in manifest.
Output can be found at `./representation/build/outputs/apk/androidTest/debug/representation-debug-androidTest.apk`

You can check that it has the proper target package name by executing:
`aapt dump xmltree ./representation/build/outputs/apk/androidTest/debug/representation-debug-androidTest.apk AndroidManifest.xml | grep targetPackage`