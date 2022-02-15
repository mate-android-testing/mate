# Pre Requisites
First, we are going to need the package name of the Application Under Test (AUT).
If you need to get this information from an APK, you can run: `aapt dump badging <path-to-apk> | grep "package: name"`

Second, the AUT's APK needs to be signed with the Android debug key.
To achieve this for an existing APK, you can use the `apksigner` provided in the Android SDK and the default Android debug key.
This command changes the APK in-place, so you might want to make a copy first.
```
apksigner sign --ks ~/.android/debug.keystore --ks-key-alias androiddebugkey --ks-pass pass:android --key-pass pass:android <path-to-apk>
```

# How to run MATE

Install the re-signed AUT on the device.
```
adb install <path-to-re-signed-apk>
```

Build and install MATE Client
```
./gradlew :client:clean :client:installDebug
```

Build and install MATE's Representation Layer
```
./gradlew -PtargetPackage=<package-name-of-aut> :representation:clean :representation:installDebugAndroidTest
```

Build and run MATE Server. 
Refer to the [instructions](https://github.com/mate-android-testing/mate-server/blob/master/README.md) from the MATE Server repository.

On another shell terminal, start the MATE Client with the desired algorithm.
The available algorithm names can be found in the [MATE Runner class](client/src/main/java/org/mate/service/MATERunner.java).
```
adb shell am start-foreground-service -n org.mate/.service.MATEService -e packageName <package-name-of-aut> -e algorithm <mate-algorithm>
```
