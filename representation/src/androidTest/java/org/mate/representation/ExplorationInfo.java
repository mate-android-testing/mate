package org.mate.representation;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.MersenneTwister;
import org.mate.commons.utils.Randomness;
import org.mate.representation.test.BuildConfig;
import org.mate.representation.util.MATERepLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class is responsible for providing basic information about the exploration being carried
 * out at the moment.
 */
public class ExplorationInfo {
    /**
     * Singleton instance of this class.
     */
    private static ExplorationInfo instance;

    /**
     * The package name of the AUT.
     */
    private final String targetPackageName = BuildConfig.TARGET_PACKAGE_NAME;

    /**
     * The Random object to use during exploration
     */
    private Random rnd;

    /**
     * Whether we are in replay mode. Default: Off
     */
    private boolean replayMode = false;

    /**
     * Whether we should use widget based actions instead of primitive actions.
     */
    private boolean widgetBasedActions = false;

    private ExplorationInfo() {}

    /**
     * @return the singleton instance of the ExplorationInfo class.
     */
    public static ExplorationInfo getInstance() {
        if (instance == null) {
            instance = new ExplorationInfo();
        }

        return instance;
    }

    /**
     * @return the package name of the AUT.
     */
    public String getTargetPackageName() {
        return targetPackageName;
    }

    /**
     * Sets the random seed to be used during exploration in the Representation Layer.
     * @param seed
     */
    public void setRandomSeed(long seed) {
        rnd = new MersenneTwister(seed);
        Randomness.setRnd(rnd);
    }

    /**
     * @return the Random instance to be used during exploration in the Representation Layer.
     */
    public Random getRandom() {
        return rnd;
    }

    /**
     * Sets replay mode to true.
     */
    public void setReplayMode() {
        replayMode = true;
    }

    /**
     * @return whether the exploration is running on replay mode or not.
     */
    public boolean isReplayMode() {
        return replayMode;
    }

    /**
     * Sets widget-based actions mode to true.
     */
    public void setWidgetBasedActions() {
        widgetBasedActions = true;
    }

    /**
     * @return whether the exploration is running on widget-based actions mode or not.
     */
    public boolean useWidgetBasedActions() {
        return widgetBasedActions;
    }

    /**
     * @return the name of the currently visible package (might be different than the AUT's
     * package name).
     */
    public String getCurrentPackageName() {
        return DeviceInfo.getInstance().getUiDevice().getCurrentPackageName();
    }

    /**
     * @return the name of the currently visible activity.
     */
    public String getCurrentActivityName() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                return getCurrentActivityAPI21to24();
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                return getCurrentActivityAPI25();
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                return getCurrentActivityAPI28();
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                return getCurrentActivityAPI29();
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
     * Returns the name of the current activity on an emulator running API 21 to 24.
     *
     * @return the current activity name.
     */
    private String getCurrentActivityAPI21to24() throws IOException {
        String output = DeviceInfo.getInstance().executeShellCommand("dumpsys activity activities");
        String mResumedActivityLine = output.split("mResumedActivity")[1].split("\n")[0].trim();
        return mResumedActivityLine.split(" ")[3];
    }

    /**
     * Returns the name of the current activity on an emulator running API 25.
     *
     * @return the current activity name.
     */
    private String getCurrentActivityAPI25() throws IOException {
        String output = DeviceInfo.getInstance().executeShellCommand("dumpsys activity top");
        return output.split("\n")[1].split(" ")[3];
    }

    /**
     * Returns the name of the current activity on an emulator running API 28.
     *
     * @return the current activity name.
     */
    private String getCurrentActivityAPI28() throws IOException {
        String output = DeviceInfo.getInstance().executeShellCommand("dumpsys activity activities");
        return output.split("mResumedActivity")[1].split("\n")[0].split(" ")[3];
    }

    /**
     * Returns the name of the current activity on an emulator running API 29.
     *
     * @return Returns the current activity name.
     */
    private String getCurrentActivityAPI29() throws IOException {
        String output = DeviceInfo.getInstance().executeShellCommand("dumpsys activity activities");
        return output.split("mResumedActivity")[1].split("\n")[0].split(" ")[3];
    }

    /**
     * @return the activity names of the AUT
     */
    public List<String> getTargetPackageActivityNames() {
        try {
            // see: https://stackoverflow.com/questions/23671165/get-all-activities-by-using-package-name
            PackageInfo pi =
                    DeviceInfo.getInstance().getAUTContext().getPackageManager().getPackageInfo(
                    targetPackageName, PackageManager.GET_ACTIVITIES);

            // TODO: Ensure that the short form '/.subpackage.activityName' is not used!!!
            List<String> activityNames = new ArrayList<>();

            for (ActivityInfo activity : pi.activities) {
                activityNames.add(activity.name);
            }

            return activityNames;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * @return the currently visible fragments.
     */
    public List<String> getCurrentFragments() {
        // https://stackoverflow.com/questions/24429049/get-info-of-current-visible-fragments-in-android-dumpsys
        try {
            String output =
                    DeviceInfo.getInstance().executeShellCommand("dumpsys activity " + getCurrentActivityName());
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
     * @return the visible fragments.
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

        List<String> result = new ArrayList<>();
        for (String line : fragmentLines) {
            if (!line.replaceAll("\\s+","").isEmpty()) {
                String aux = line.split(":")[1];
                aux = aux.split("\\{")[0];
                aux = aux.trim();

                result.add(aux);
            }
        }

        return result;
    }

    /**
     * Sends a broadcast to the tracer, which in turn dumps the collected traces to a file on
     * the external storage.
     */
    public void sendBroadcastToTracer() {
        Intent intent = new Intent("STORE_TRACES");
        intent.setComponent(new ComponentName(getTargetPackageName(),
                "de.uni_passau.fim.auermich.tracer.Tracer"));
        DeviceInfo.getInstance().getAUTContext().sendBroadcast(intent);
    }
}
