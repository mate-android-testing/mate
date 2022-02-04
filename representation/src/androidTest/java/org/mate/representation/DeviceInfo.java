package org.mate.representation;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import org.mate.commons.utils.MATELog;
import org.mate.representation.util.MATERepLog;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for providing basic information about the Device and the application
 * running at the moment.
 */
public class DeviceInfo {
    private static DeviceInfo instance;

    /**
     * The device instance provided by the instrumentation class to perform various actions.
     */
    private final UiDevice device;
    private final Instrumentation instrumentation;
    private String targetPackageName;

    private DeviceInfo() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);

    }

    public static DeviceInfo getInstance() {
        if (instance == null) {
            instance = new DeviceInfo();
        }

        return instance;
    }

    /**
     * @return the name of the currently visible package.
     */
    public String getCurrentPackageName() {
       return device.getCurrentPackageName();
    }

    /**
     * Retrieves the name of the currently visible activity.
     *
     * @return Returns the name of the currently visible activity.
     */
    public String getCurrentActivityName() {
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                return getCurrentActivityAPI25();
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                return getCurrentActivityAPI28();
            } else {
                return null;
            }
        } catch (Exception e) {
            MATERepLog.warning("Couldn't retrieve current activity name via local shell!");
            MATERepLog.warning(e.getMessage());

            return null;
        }
    }

    /**
     * Returns the name of the current activity on an emulator running API 25.
     *
     * @return Returns the current activity name.
     */
    private String getCurrentActivityAPI25() throws IOException {
        String output = device.executeShellCommand("dumpsys activity top");
        return output.split("\n")[1].split(" ")[3];
    }

    /**
     * Returns the name of the current activity on an emulator running API 28.
     *
     * @return Returns the current activity name.
     */
    private String getCurrentActivityAPI28() throws IOException {
        String output = device.executeShellCommand("dumpsys activity activities");
        return output.split("mResumedActivity")[1].split("\n")[0].split(" ")[3];
    }

    public UiDevice getUiDevice() {
        return device;
    }

    public int getDisplayWidth() {
        return device.getDisplayWidth();
    }

    public int getDisplayHeight() {
        return device.getDisplayHeight();
    }

    public boolean isCrashDialogDisplayed() {
        UiObject crashDialog1 = device.findObject(
                new UiSelector().packageName("android").textContains("keeps stopping"));
        UiObject crashDialog2 = device.findObject(
                new UiSelector().packageName("android").textContains("has stopped"));

        return crashDialog1.exists() || crashDialog2.exists();
    }

    public List<String> getTargetPackageActivityNames() {
        if (targetPackageName == null) {
            return null;
        }

        try {
            // see: https://stackoverflow.com/questions/23671165/get-all-activities-by-using-package-name
            PackageInfo pi = instrumentation.getTargetContext().getPackageManager().getPackageInfo(
                    targetPackageName, PackageManager.GET_ACTIVITIES);

            return Arrays.stream(pi.activities).map(activity -> activity.name)
                    .collect(Collectors.toList());
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public void setTargetPackageName(String packageName) {
        targetPackageName = packageName;
    }

    public boolean clearTargetPackageData() {
        if (targetPackageName != null) {
            String output = this.executeShellCommand("pm clear " + targetPackageName);
            return output != null;
        }
        return false;
    }

    public String executeShellCommand(String command) {
        try {
            return device.executeShellCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean restartTargetPackage() {
        if (targetPackageName == null) {
            return false;
        }

        Context context = instrumentation.getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(targetPackageName);
        // Clear out any previous instances
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } catch (Exception e) {
            e.printStackTrace();
            MATELog.log("EXCEPTION CLEARING ACTIVITY FLAG");
            return false;
        }

        context.startActivity(intent);
        return true;
    }
}
