package org.mate.representation;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import org.mate.commons.utils.MATELog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class is responsible for providing basic information about the Device.
 * It also provides access to Instrumentation and UiDevice.
 */
public class DeviceInfo {
    /**
     * Singleton instance of this class.
     */
    private static DeviceInfo instance;

    /**
     * Instrumentation instance gathered from the InstrumentationRegistry.
     */
    private final Instrumentation instrumentation;

    /**
     * The device instance provided by the instrumentation class to perform various actions.
     */
    private final UiDevice device;

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

    /**
     * @return the singleton instance of the DeviceInfo class.
     */
    public static DeviceInfo getInstance() {
        if (instance == null) {
            instance = new DeviceInfo();
        }

        return instance;
    }

    /**
     * Get the UiDevice to get access to state information about the device.
     * This class can also be used to simulate user actions on the device.
     *
     * @return the instance of UiDevice provided by the InstrumentationRegistry.
     */
    public UiDevice getUiDevice() {
        return device;
    }

    /**
     * @return the Context of the DynamicTest that started the Representation Layer.
     */
    public Context getRepresentationLayerContext() {
        return instrumentation.getContext();
    }

    /**
     * @return the Context of the AUT.
     */
    public Context getAUTContext() {
        return instrumentation.getTargetContext();
    }

    /**
     * @return the width of the display, in pixels. The width and height details are reported
     * based on the current orientation of the display.
     */
    public int getDisplayWidth() {
        return device.getDisplayWidth();
    }

    /**
     * @return the height of the display, in pixels. The width and height details are reported
     * based on the current orientation of the display.
     */
    public int getDisplayHeight() {
        return device.getDisplayHeight();
    }

    /**
     * Executes a shell command using shell user identity, and returns the standard output in
     * string.
     *
     * @param command to execute
     * @return the output of the command or null if an exception was found during execution.
     */
    public String executeShellCommand(String command) {
        try {
            return device.executeShellCommand(command);
        } catch (IOException e) {
            MATELog.log_error("An error occurred executing shell command: " + e.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();
            MATELog.log_error(stackTrace);

            return null;
        }
    }

    /**
     * Returns whether the emulator is in portrait mode or not.
     *
     * @return Returns {@code true} if the emulator is in portrait mode, otherwise {@code false}
     *         is returned.
     */
    public boolean isInPortraitMode() {
        return isInPortraitMode;
    }

    /**
     * Stores whether the emulator is in portrait mode or not.
     */
    public void setInPortraitMode(boolean inPortraitMode) {
        isInPortraitMode = inPortraitMode;
    }

    /**
     * Toggles the boolean indicating if emulator is in portrait mode or not.
     */
    public void toggleInPortraitMode() {
        isInPortraitMode = !isInPortraitMode;
    }

    /**
     * @return whether auto-rotate is disabled or not.
     */
    public boolean isDisabledAutoRotate() {
        return disabledAutoRotate;
    }

    /**
     * Sets whether auto-rotate is disabled or not.
     */
    public void setDisabledAutoRotate(boolean disabledAutoRotate) {
        this.disabledAutoRotate = disabledAutoRotate;
    }

    /**
     * Grants a runtime permission for the AUT.
     *
     * @param permission to grant.
     * @return whether the grant was successful or not.
     */
    public boolean grantRuntimePermission(String permission) {
        String packageName = ExplorationInfo.getInstance().getTargetPackageName();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            instrumentation.getUiAutomation().grantRuntimePermission(packageName, permission);
            return true;
        }

        try {
            final String grantedPermission
                    = device.executeShellCommand("pm grant " + packageName + " " + permission);

            // an empty response indicates success of the operation
            return grantedPermission.isEmpty();
        } catch (IOException e) {
            MATELog.log_error("Couldn't grant runtime permissions! " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks whether a crash dialog is visible on the current screen.
     *
     * @return Returns {@code true} if a crash dialog is visible, otherwise {@code false}
     *         is returned.
     */
    public boolean isCrashDialogPresent() {
        UiObject crashDialog1 = device.findObject(
                new UiSelector().packageName("android").textContains("keeps stopping"));
        UiObject crashDialog2 = device.findObject(
                new UiSelector().packageName("android").textContains("has stopped"));

        return crashDialog1.exists() || crashDialog2.exists();

    }
}
