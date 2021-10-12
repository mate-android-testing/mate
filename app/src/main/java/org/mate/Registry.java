package org.mate;

import android.app.Instrumentation;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
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
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                return getCurrentActivityAPI25(device);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                return getCurrentActivityAPI28(device);
            } else {
                // fall back mechanism (slow)
                return Registry.getEnvironmentManager().getCurrentActivityName();
            }
        } catch (Exception e) {
            MATE.log_warn("Couldn't retrieve current activity name via local shell!");
            MATE.log_warn(e.getMessage());

            // fall back mechanism (slow)
            return Registry.getEnvironmentManager().getCurrentActivityName();
        }
    }

    /**
     * Returns the name of the current activity on an emulator running API 25.
     *
     * @param device The ui device.
     * @return Returns the current activity name.
     */
    private static String getCurrentActivityAPI25(UiDevice device) throws IOException {
        String output = device.executeShellCommand("dumpsys activity top");
        return output.split("\n")[1].split(" ")[3];
    }

    /**
     * Returns the name of the current activity on an emulator running API 28.
     *
     * @param device The ui device.
     * @return Returns the current activity name.
     */
    private static String getCurrentActivityAPI28(UiDevice device) throws IOException {
        String output = device.executeShellCommand("dumpsys activity activities");
        return output.split("mResumedActivity")[1].split("\n")[0].split(" ")[3];
    }

    /**
     * Grants the AUT the read and write runtime permissions for the external storage.
     * <p>
     * Depending on the API level, we can either use the very fast method grantRuntimePermissions()
     * (API >= 28) or the slow routine executeShellCommand(). Right now, however, we let the
     * MATE-Server granting those permissions directly before executing a privileged command in
     * order to avoid unnecessary requests.
     * <p>
     * In order to verify that the runtime permissions got granted, check the output of the
     * following command:
     * device.executeShellCommand("dumpsys package " + packageName);
     *
     * @return Returns {@code true} when operation succeeded, otherwise {@code false} is returned.
     */
    @SuppressWarnings("unused")
    public static boolean grantRuntimePermissions() {

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        final String readPermission = "android.permission.READ_EXTERNAL_STORAGE";
        final String writePermission = "android.permission.WRITE_EXTERNAL_STORAGE";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            instrumentation.getUiAutomation().grantRuntimePermission(packageName, readPermission);
            instrumentation.getUiAutomation().grantRuntimePermission(packageName, writePermission);
            return true;
        }

        try {
            /*
             * The operation executeShellCommand() is costly, but unfortunately it is not possible
             * to concatenate two commands yet.
             */
            final String grantedReadPermission
                    = device.executeShellCommand("pm grant " + packageName + " " + readPermission);
            final String grantedWritePermission
                    = device.executeShellCommand("pm grant " + packageName + " " + writePermission);

            // an empty response indicates success of the operation
            return grantedReadPermission.isEmpty() && grantedWritePermission.isEmpty();
        } catch (IOException e) {
            MATE.log_error("Couldn't grant runtime permissions!");
            throw new IllegalStateException(e);
        }
    }
}
