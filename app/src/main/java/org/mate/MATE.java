package org.mate;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import org.mate.exploration.Algorithm;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.EnvironmentManager;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.deprecated.graph.GraphGUIModel;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.utils.Coverage;
import org.mate.utils.CoverageUtils;
import org.mate.utils.MersenneTwister;
import org.mate.utils.TimeoutRun;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class MATE {

    // TODO: remove this field once its references are resolved
    public static UiDevice device;
    // TODO: make private
    public static UIAbstractionLayer uiAbstractionLayer;
    public static String packageName;
    public static IGUIModel guiModel;
    private DeviceMgr deviceMgr;
    public static long TIME_OUT;
    public Instrumentation instrumentation;

    public MATE() {

        // should resolve android.os.FileUriExposedException
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Integer serverPort = null;
        try (FileInputStream fis = InstrumentationRegistry.getTargetContext().openFileInput("port");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            serverPort = Integer.valueOf(reader.readLine());
            MATE.log_acc("Using server port: " + serverPort);
        } catch (IOException e) {
            MATE.log_acc("Couldn't read server port, fall back to default port!");
        }

        EnvironmentManager environmentManager;
        try {
            if (serverPort == null) {
                environmentManager = new EnvironmentManager();
            } else {
                environmentManager = new EnvironmentManager(serverPort);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to setup EnvironmentManager", e);
        }
        Registry.registerEnvironmentManager(environmentManager);
        Registry.registerProperties(new Properties(environmentManager.getProperties()));
        Random rnd;
        if (Properties.RANDOM_SEED() != null) {
            rnd = new MersenneTwister(Properties.RANDOM_SEED());
        } else {
            rnd = new MersenneTwister();
        }
        Registry.registerRandom(rnd);

        MATE.log_acc("TIMEOUT: " + Properties.TIMEOUT());
        MATE.TIME_OUT = Properties.TIMEOUT() * 60 * 1000;

        //Defines the class that represents the device
        //Instrumentation instrumentation =  getInstrumentation();
        instrumentation = getInstrumentation();
        device = UiDevice.getInstance(instrumentation);

        //checks whether user needs to authorize access to something on the device/emulator
        UIAbstractionLayer.clearScreen(new DeviceMgr(device, ""));

        //get the name of the package of the app currently running
        this.packageName = device.getCurrentPackageName();
        MATE.log_acc("Package name: " + this.packageName);

        String emulator = Registry.getEnvironmentManager().allocateEmulator(this.packageName);
        MATE.log_acc("Emulator: " + emulator);

        if (emulator != null && !emulator.equals("")) {
            this.deviceMgr = new DeviceMgr(device, packageName);
            uiAbstractionLayer = new UIAbstractionLayer(deviceMgr, packageName);
        } else {
            throw new IllegalStateException("Emulator couldn't be properly allocated!");
        }
    }

    public void testApp(final Algorithm algorithm) {

        MATE.log_acc("Activities:");
        for (String s : Registry.getEnvironmentManager().getActivityNames()) {
            MATE.log_acc("\t" + s);
        }

        if (Properties.GRAPH_TYPE() != null) {
            // initialise a graph
            MATE.log_acc("Initialising graph!");
            Registry.getEnvironmentManager().initGraph();
        }

        try {
            TimeoutRun.timeoutRun(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    algorithm.run();
                    return null;
                }
            }, MATE.TIME_OUT);

            if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
                CoverageUtils.logFinalCoverage();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (Properties.GRAPH_TYPE() != null) {
                Registry.getEnvironmentManager().drawGraph(Properties.DRAW_RAW_GRAPH());
            }

            Registry.getEnvironmentManager().releaseEmulator();
            //EnvironmentManager.deleteAllScreenShots(packageName);
            try {
                Registry.unregisterEnvironmentManager();
                Registry.unregisterProperties();
                Registry.unregisterRandom();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkVisitedActivities(String explorationStrategy) {
        Set<String> visitedActivities = new HashSet<String>();
        for (IScreenState scnd : guiModel.getStates()) {
            visitedActivities.add(scnd.getActivityName());
        }

        MATE.log(explorationStrategy + " visited activities " + visitedActivities.size());
        for (String act : visitedActivities)
            MATE.log("   " + act);
    }

    public static void log(String msg) {
        Log.i("apptest", msg);
    }

    public static void logsum(String msg) {
        Log.e("acc", msg);
    }

    public static void log_acc(String msg) {
        Log.e("acc", msg);
    }

    public static void log_vin(String msg) {
        Log.i("vinDebug", msg);
    }

    public String getPackageName() {
        return packageName;
    }

    public IGUIModel getGuiModel() {
        return guiModel;
    }

    public UiDevice getDevice() {
        return device;
    }

    public static UIAbstractionLayer getUiAbstractionLayer() {
        return uiAbstractionLayer;
    }

    public static void logactivity(String activityName) {
        Log.i("acc", "ACTIVITY_VISITED: " + activityName);
    }
}
