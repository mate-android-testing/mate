package org.mate.representation;

import android.app.Instrumentation;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.mate.representation.util.MATERepLog;

import java.io.IOException;

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
}
