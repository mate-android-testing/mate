package org.mate;

import org.mate.interaction.DeviceMgr;
import org.mate.interaction.EnvironmentManager;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.utils.manifest.Manifest;

import java.io.IOException;
import java.util.Random;

public class Registry {

    private static EnvironmentManager environmentManager;
    private static Properties properties;
    private static Random random;

    /**
     * An abstraction of the app screen enabling the execution of actions and various other tasks.
     */
    private static UIAbstractionLayer uiAbstractionLayer;

    /**
     * Enables the interaction with the device (emulator).
     */
    private static DeviceMgr deviceMgr;

    /**
     * The package name of the AUT.
     */
    private static String packageName;

    /**
     * The time out in milli seconds.
     */
    private static Long timeout;

    /**
     * Whether we are in replay mode. Default: Off
     */
    private static boolean replayMode;

    /**
     * Represents an abstraction of the AndroidManifest.xml file.
     */
    private static Manifest manifest;

    /**
     * The name of the main activity.
     */
    private static String mainActivity;

    public static void registerReplayMode() {
        replayMode = true;
    }

    public static boolean isReplayMode() {
        return replayMode;
    }

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

    public static DeviceMgr getDeviceMgr() {
        return deviceMgr;
    }

    public static void registerDeviceMgr(final DeviceMgr deviceMgr) {
        Registry.deviceMgr = deviceMgr;
    }

    public static void unregisterDeviceMgr() {
        deviceMgr = null;
    }

    public static Manifest getManifest() {
        return manifest;
    }

    public static void registerManifest(Manifest manifest) {
        Registry.manifest = manifest;
    }

    public static void unregisterManifest() {
        Registry.manifest = null;
    }

    public static String getMainActivity() {
        return mainActivity;
    }

    public static void registerMainActivity(String mainActivity) {
        Registry.mainActivity = mainActivity;
    }

    public static void unregisterMainActivity() {
        Registry.mainActivity = null;
    }
}
