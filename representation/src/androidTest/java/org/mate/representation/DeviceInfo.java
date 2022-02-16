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
import org.mate.commons.utils.MersenneTwister;
import org.mate.commons.utils.Randomness;
import org.mate.representation.util.MATERepLog;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This class is responsible for providing basic information about the Device.
 * It also provides access to Instrumentation and UiDevice.
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

    public UiDevice getUiDevice() {
        return device;
    }

    public Context getAUTContext() {
        return instrumentation.getTargetContext();
    }

    public int getDisplayWidth() {
        return device.getDisplayWidth();
    }

    public int getDisplayHeight() {
        return device.getDisplayHeight();
    }

    public String executeShellCommand(String command) {
        try {
            return device.executeShellCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
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
}
