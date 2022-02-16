package org.mate.representation;

import android.app.Instrumentation;
import android.content.Context;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for providing basic information about the Device and the application
 * running at the moment.
 */
public class DeviceInfo {
    /**
     * Singleton instance of this class.
     */
    private static DeviceInfo instance;

    private final Instrumentation instrumentation;

    /**
     * The device instance provided by the instrumentation class to perform various actions.
     */
    private final UiDevice device;

    /**
     * The package name of the AUT.
     */
    private String targetPackageName;

    /**
     * Returns whether the emulator is in portrait mode or not.
     *
     * @return Returns {@code true} if the emulator is in portrait mode, otherwise {@code false}
     *         is returned.
     */
    public boolean isInPortraitMode() {
        return isInPortraitMode;
    }

    public void setInPortraitMode(boolean inPortraitMode) {
        isInPortraitMode = inPortraitMode;
    }

    public void toggleInPortraitMode() {
        isInPortraitMode = !isInPortraitMode;
    }

    public boolean isDisabledAutoRotate() {
        return disabledAutoRotate;
    }

    public void setDisabledAutoRotate(boolean disabledAutoRotate) {
        this.disabledAutoRotate = disabledAutoRotate;
    }

    /**
     * Keeps track whether the emulator is in portrait or landscape mode.
     */
    private boolean isInPortraitMode;

    /**
     * Keeps track whether auto rotation has been disabled.
     */
    private boolean disabledAutoRotate;

    private DeviceInfo() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);

        this.isInPortraitMode = true;
        this.disabledAutoRotate = false;
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

    public String executeShellCommand(String command) {
        try {
            return device.executeShellCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Context getAUTContext() {
        return instrumentation.getTargetContext();
    }

    /**
     * Returns the currently visible fragments.
     *
     * @return Returns the currently visible fragments.
     */
    public List<String> getCurrentFragments() {
        // https://stackoverflow.com/questions/24429049/get-info-of-current-visible-fragments-in-android-dumpsys
        try {
            String output = device.executeShellCommand("dumpsys activity " + getCurrentActivityName());
            List<String> fragments = extractFragments(output);
            MATELog.log_debug("Currently active fragments: " + fragments);
            return fragments;
        } catch (Exception e) {
            MATELog.log_warn("Couldn't retrieve currently active fragments: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Extracts the visible fragments from the given output.
     *
     * @param output The output of the command 'dumpsys activity <activity-name>'.
     * @return Returns the visible fragments.
     */
    private List<String> extractFragments(String output) {

        /*
         * A typical output of the command 'dumpsys activity <activity-name>' looks as follows:
         *
         *  Local FragmentActivity 30388ff State:
         *     Added Fragments:
         *       #0: MyFragment{b4f3bc} (642de726-ae2d-439c-a047-4a4a35a6f435 id=0x7f080071)
         *       #1: MySecondFragment{a918ac1} (12f5630f-b93c-40c8-a9fa-49b74745678a id=0x7f080071)
         *     Back Stack Index: 0 (this line seems to be optional!)
         *     FragmentManager misc state:
         */

        final String fragmentActivityState = output.split("Local FragmentActivity")[1];

        // If no fragment is visible, the 'Added Fragments:' line is missing!
        if (!fragmentActivityState.contains("Added Fragments:")) {
            return Collections.emptyList();
        }

        final String[] fragmentLines = fragmentActivityState
                .split("Added Fragments:")[1]
                .split("FragmentManager")[0]
                .split("Back Stack Index:")[0] // this line is not always present
                .split(System.lineSeparator());

        return Arrays.stream(fragmentLines)
                .filter(line -> !line.replaceAll("\\s+","").isEmpty())
                .map(line -> line.split(":")[1])
                .map(line -> line.split("\\{")[0])
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public String getTargetPackageName() {
        return targetPackageName;
    }
}
