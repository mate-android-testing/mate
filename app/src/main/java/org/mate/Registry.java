package org.mate;

import android.app.Instrumentation;
import android.support.test.uiautomator.UiDevice;

import org.mate.interaction.EnvironmentManager;
import org.mate.interaction.UIAbstractionLayer;

import java.io.IOException;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class Registry {

    private static EnvironmentManager environmentManager;
    private static Properties properties;
    private static Random random;

    /**
     * An abstraction of the app screen enabling the execution
     * of actions and various other tasks.
     */
    private static UIAbstractionLayer uiAbstractionLayer;

    /**
     * The package name of the AUT.
     */
    private static String packageName;

    /**
     * The time out in milli seconds.
     */
    private static Long timeout;

    public static void registerUiAbstractionLayer(UIAbstractionLayer uiAbstractionLayer) {
        Registry.uiAbstractionLayer = uiAbstractionLayer;
    }

    public static void unregisterUiAbstractionLayer() {
        uiAbstractionLayer = null;
    }

    public static UIAbstractionLayer getUiAbstractionLayer() {
        if (uiAbstractionLayer == null) {
            throw new IllegalStateException("No UiAbstractionLayer registered!");
        }
        return uiAbstractionLayer;
    }

    public static void registerTimeout(long timeout) {
        Registry.timeout = timeout;
    }

    public static void unregisterTimeout() {
        timeout = null;
    }

    public static long getTimeout() {
        if (timeout == null) {
            throw new IllegalStateException("No UiAbstractionLayer registered!");
        }
        return timeout;
    }

    public static void registerPackageName(String packageName) {
        Registry.packageName = packageName;
    }

    public static void unregisterPackageName() {
        packageName = null;
    }

    public static String getPackageName() {
        if (packageName == null) {
            throw new IllegalStateException("No packageName registered!");
        }
        return packageName;
    }

    public static EnvironmentManager getEnvironmentManager() {
        if (environmentManager == null) {
            throw new IllegalStateException("No EnvironmentManger registered!");
        }
        return environmentManager;
    }

    public static void registerEnvironmentManager(EnvironmentManager environmentManager) {
        Registry.environmentManager = environmentManager;
    }

    public static void unregisterEnvironmentManager() throws IOException {
        environmentManager.close();
        environmentManager = null;
    }

    public static Properties getProperties() {
        if (properties == null) {
            throw new IllegalStateException("No Properties registered!");
        }
        return properties;
    }

    public static void registerProperties(Properties properties) {
        Registry.properties = properties;
    }

    public static void unregisterProperties() {
        properties = null;
    }

    public static Random getRandom() {
        if (random == null) {
            throw new IllegalStateException("No Random registered!");
        }
        return random;
    }

    public static void registerRandom(Random random) {
        Registry.random = random;
    }

    public static void unregisterRandom() {
        random = null;
    }

    /**
     * Retrieves the name of the currently visible activity.
     *
     * @return Returns the name of the currently visible activity.
     */
    public static String getCurrentActivity() {

        Instrumentation instrumentation = getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        try {
            // TODO: check whether the command is reliable on different images (APIs)
            String output = device.executeShellCommand("dumpsys activity top");
            return output.split("\n")[1].split(" ")[3];
        } catch (IOException e) {
            MATE.log_warn("Couldn't retrieve current activity name via local shell!");
            MATE.log_warn(e.getMessage());

            // fall back mechanism (slow)
            return Registry.getEnvironmentManager().getCurrentActivityName();
        }
    }
}
