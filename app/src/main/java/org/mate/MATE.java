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
import org.mate.exploration.genetic.algorithm.RandomSearch;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.exploration.intent.IntentChromosomeFactory;
import org.mate.exploration.manual.CheckCurrentScreen;
import org.mate.exploration.manual.ManualExploration;
import org.mate.exploration.deprecated.random.UniformRandomForAccessibility;
import org.mate.exploration.genetic.algorithm.NSGAII;
import org.mate.exploration.genetic.algorithm.RandomWalk;
import org.mate.exploration.genetic.algorithm.StandardGeneticAlgorithm;
import org.mate.exploration.genetic.chromosome_factory.PrimitiveAndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.crossover.PrimitiveTestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.fitness.ActivityFitnessFunction;
import org.mate.exploration.genetic.fitness.AmountCrashesFitnessFunction;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.fitness.AndroidStateFitnessFunction;
import org.mate.exploration.genetic.chromosome_factory.AndroidSuiteRandomChromosomeFactory;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.mutation.PrimitiveTestCaseShuffleMutationFunction;
import org.mate.exploration.genetic.selection.FitnessProportionateSelectionFunction;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.termination.IterTerminationCondition;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.exploration.genetic.algorithm.MOSA;
import org.mate.exploration.genetic.algorithm.Mio;
import org.mate.exploration.genetic.selection.RandomSelectionFunction;
import org.mate.exploration.genetic.mutation.SapienzSuiteMutationFunction;
import org.mate.exploration.genetic.fitness.StatementCoverageFitnessFunction;
import org.mate.exploration.genetic.crossover.TestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.fitness.TestLengthFitnessFunction;
import org.mate.exploration.genetic.crossover.UniformSuiteCrossoverFunction;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.exploration.heuristical.HeuristicExploration;
import org.mate.exploration.heuristical.RandomExploration;
import org.mate.interaction.DeviceMgr;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.IGUIModel;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.model.graph.GraphGUIModel;
import org.mate.serialization.TestCaseSerializer;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.WidgetAction;
import org.mate.utils.Coverage;
import org.mate.utils.CoverageUtils;
import org.mate.utils.MersenneTwister;
import org.mate.utils.TestCaseOptimizer;
import org.mate.utils.TestCaseStatistics;
import org.mate.utils.TimeoutRun;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.mate.Properties.MAX_NUMBER_EVENTS;

public class MATE {

    public static UiDevice device;
    public static UIAbstractionLayer uiAbstractionLayer;
    public static String packageName;
    public static IGUIModel guiModel;
    private List<Action> actions;
    private DeviceMgr deviceMgr;
    public static long total_time;
    public static long RANDOM_LENGH;
    private long runningTime = new Date().getTime();
    public static long TIME_OUT;
    public Instrumentation instrumentation;

    private GraphGUIModel completeModel;

    public static String logMessage;


    //public static Vector<String> checkedWidgets = new Vector<String>();
    public static Set<String> visitedActivities = new HashSet<String>();

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
            //ignore: use default port if file does not exists
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

        //get timeout from server using EnvironmentManager
        long timeout = Registry.getEnvironmentManager().getTimeout();
        if (timeout == 0)
            timeout = 30; //set default - 30 minutes
        MATE.TIME_OUT = timeout * 60 * 1000;
        MATE.log("TIMEOUT : " + timeout);

        //get random length = number of actions before restarting the app
        long rlength = Registry.getEnvironmentManager().getRandomLength();
        if (rlength == 0)
            rlength = 1000; //default
        MATE.RANDOM_LENGH = rlength;
        MATE.log("RANDOM length by server: " + MATE.RANDOM_LENGH);

        logMessage = "";

        //Defines the class that represents the device
        //Instrumentation instrumentation =  getInstrumentation();
        instrumentation = getInstrumentation();
        device = UiDevice.getInstance(instrumentation);

        //checks whether user needs to authorize access to something on the device/emulator
        UIAbstractionLayer.clearScreen(new DeviceMgr(device, ""));

        //get the name of the package of the app currently running
        this.packageName = device.getCurrentPackageName();
        MATE.log("Package name: " + this.packageName);

        //list the activities of the app under test
        listActivities(instrumentation.getContext());

        String emulator = Registry.getEnvironmentManager().detectEmulator(this.packageName);

        if (emulator != null && !emulator.equals("")) {
            this.deviceMgr = new DeviceMgr(device, packageName);
            uiAbstractionLayer = new UIAbstractionLayer(deviceMgr, packageName);
        }

    }

    public void testApp(final Algorithm algorithm) {

        runningTime = new Date().getTime();

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
        logMessage += msg + "\n";

    }

    public static void log_acc(String msg) {
        Log.e("acc", msg);
        logMessage += msg + "\n";
    }

    public static void log_vin(String msg) {
        Log.i("vinDebug", msg);
        logMessage += msg + "\n";
    }

    public void listActivities(Context context) {

        //list all activities of the application being executed
        PackageManager pm = (PackageManager) context.getPackageManager();
        try {
            PackageInfo pinfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activities = pinfo.activities;
            for (int i = 0; i < activities.length; i++) {
                //log("Activity " + (i + 1) + ": " + activities[i].name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
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
