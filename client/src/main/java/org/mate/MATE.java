package org.mate;

import android.os.RemoteException;
import android.os.StrictMode;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.Algorithm;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.EnvironmentManager;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.utils.MersenneTwister;
import org.mate.utils.TimeoutRun;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class MATE {

    // TODO: make singleton
    public MATE(String packageName, IRepresentationLayerInterface representationLayer) {

        // should resolve android.os.FileUriExposedException
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // should resolve android.os.NetworkOnMainThreadException
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

        MATELog.log_acc("TIMEOUT: " + Properties.TIMEOUT());
        Registry.registerTimeout(Properties.TIMEOUT() * 60 * 1000);

        Registry.registerPackageName(packageName);
        MATELog.log_acc("Package name: " + Registry.getPackageName());

        configRepresentationLayer(representationLayer);

        final DeviceMgr deviceMgr = new DeviceMgr(representationLayer, Registry.getPackageName());
        Registry.registerDeviceMgr(deviceMgr);

        // internally checks for permission dialogs and grants permissions if required
        Registry.registerUiAbstractionLayer(new UIAbstractionLayer(deviceMgr, Registry.getPackageName()));

        // check whether the AUT could be successfully started
        String currentPackageName = deviceMgr.getCurrentPackageName();
        if (!Registry.getPackageName().equals(currentPackageName)) {
            MATELog.log_acc("Currently displayed app: " + currentPackageName);
            throw new IllegalStateException("Couldn't launch app under test!");
        }

        // try to allocate emulator
        String emulator = Registry.getEnvironmentManager().allocateEmulator(Registry.getPackageName());
        MATELog.log_acc("Emulator: " + emulator);

        if (emulator == null || emulator.isEmpty()) {
            throw new IllegalStateException("Emulator couldn't be properly allocated!");
        }

        if (Properties.GRAPH_TYPE() != null) {
            // initialise a graph
            MATELog.log_acc("Initialising graph!");
            Registry.getEnvironmentManager().initGraph();
        }
    }

    private void configRepresentationLayer(IRepresentationLayerInterface representationLayer) {
        try {
            representationLayer.setTargetPackageName(Registry.getPackageName());
        } catch (RemoteException e) {
            throw new IllegalStateException("Couldn't configure Representation Layer");
        }
    }

    /**
     * Executes the given algorithm in a timeout controlled loop.
     *
     * @param algorithm The algorithm to be executed, e.g. random exploration.
     */
    public void testApp(final Algorithm algorithm) {

        MATELog.log_acc("Activities:");
        for (String activity : Registry.getUiAbstractionLayer().getActivityNames()) {
            MATELog.log_acc("\t" + activity);
        }

        try {
            TimeoutRun.timeoutRun(() -> {
                algorithm.run();
                return null;
            }, Registry.getTimeout());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
                CoverageUtils.logFinalCoverage();
            }

            if (Properties.GRAPH_TYPE() != null) {
                Registry.getEnvironmentManager().drawGraph(Properties.DRAW_RAW_GRAPH());
            }

            Registry.getEnvironmentManager().releaseEmulator();
            // EnvironmentManager.deleteAllScreenShots(packageName);
            try {
                Registry.unregisterEnvironmentManager();
                Registry.unregisterUiAbstractionLayer();
                Registry.unregisterDeviceMgr();
                Registry.unregisterProperties();
                Registry.unregisterRandom();
                Registry.unregisterPackageName();
                Registry.unregisterTimeout();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
