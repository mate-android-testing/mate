package org.mate;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import org.mate.exceptions.AUTCrashException;
import org.mate.exploration.evolutionary.OnePlusOne;
import org.mate.exploration.genetic.IGeneticAlgorithm;
import org.mate.exploration.novelty.NoveltyBased;
import org.mate.exploration.random.UniformRandomForAccessibility;
import org.mate.interaction.DeviceMgr;
import org.mate.model.IGUIModel;
import org.mate.model.graph.GraphGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.UIAbstractionLayer;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

/**
 * Created by marceloe on 07/03/17.
 */

public class MATE {

    public static UiDevice device;
    private String packageName;
    private IGUIModel guiModel;
    private Vector<Action> actions;
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

    public MATE(){
        //get timeout from server using EnvironmentManager
        long timeout = EnvironmentManager.getTimeout();
        if (timeout==0)
            timeout = 30; //set default - 30 minutes
        MATE.TIME_OUT = timeout * 60 * 1000;
        MATE.log("TIMEOUT : " + timeout);

        //get random length = number of actions before restarting the app
        long rlength = EnvironmentManager.getRandomLength();
        if (rlength==0)
            rlength = 1000; //default
        MATE.RANDOM_LENGH = rlength;
        MATE.log("RANDOM length by server: " + MATE.RANDOM_LENGH);

        logMessage="";

        //Defines the class that represents the device
        //Instrumentation instrumentation =  getInstrumentation();
        instrumentation =  getInstrumentation();
        device = UiDevice.getInstance(instrumentation);

        //get the name of the package of the app currently running
        this.packageName = device.getCurrentPackageName();
        MATE.log("Package name: " + this.packageName);

        //checks whether user needs to authorize access to something on the device/emulator
        handleAuth(device);

        //list the activities of the app under test
        listActivities(instrumentation.getContext());

    }

    public void testApp(String explorationStrategy){

        String emulator = EnvironmentManager.detectEmulator(this.packageName);
        MATE.log("EMULATOR: " + emulator);

        runningTime = new Date().getTime();
        try{
            if (emulator!=null && !emulator.equals("")){
                this.deviceMgr = new DeviceMgr(device,packageName);

                //TODO: reinstall the app to make it start from scratch
                deviceMgr.reinstallApp();
                Thread.sleep(5000);
                deviceMgr.restartApp();
                Thread.sleep(5000);



                //Analyses the screen and gets an object that represents the screen state (configuration of widgets)
                IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

                if (explorationStrategy.equals("AccRandom")) {
                    //creates the graph that represents the GUI model
                    this.guiModel = new GraphGUIModel();
                    //first state (root node - action ==null)
                    this.guiModel.updateModel(null,state);
                    UniformRandomForAccessibility unirandomacc = new UniformRandomForAccessibility(deviceMgr,packageName,guiModel,true);
                    unirandomacc.startUniformRandomExploration(state,runningTime);
                }


                else if (explorationStrategy.equals("Novelty")) {
                    //creates the graph that represents the GUI model
                    this.guiModel = new GraphGUIModel();
                    //first state (root node - action ==null)

                    boolean updated = this.guiModel.updateModel(null, state);

                    NoveltyBased noveltyExploration = new NoveltyBased(deviceMgr,packageName,guiModel);
                    noveltyExploration.startEvolutionaryExploration(state);
                } else if (explorationStrategy.equals("OnePlusOne")) {
                    this.guiModel = new GraphGUIModel();

                    boolean updated = this.guiModel.updateModel(null, state);

                    OnePlusOne onePlusOne = new OnePlusOne(deviceMgr,packageName,guiModel);
                    onePlusOne.startEvolutionaryExploration(state);
                } else if (explorationStrategy.equals("OnePlusOneNew")) {
                    this.guiModel = new GraphGUIModel();
                    UIAbstractionLayer uiAbstractionLayer = new UIAbstractionLayer(deviceMgr, packageName, (GraphGUIModel) guiModel);

                    boolean updated = this.guiModel.updateModel(null, state);

                    IGeneticAlgorithm onePlusOneNew = new org.mate.exploration.genetic.OnePlusOne(uiAbstractionLayer);
                    onePlusOneNew.run();
                }

                checkVisitedActivities(explorationStrategy);
                EnvironmentManager.releaseEmulator();
            }
            else
                MATE.log("Emulator is null");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            EnvironmentManager.releaseEmulator();
            //EnvironmentManager.deleteAllScreenShots(packageName);
        }

    }

    private void checkVisitedActivities(String explorationStrategy) {
        Set<String> visitedActivities = new HashSet<String>();
        for (IScreenState scnd: guiModel.getStates()){
            visitedActivities.add(scnd.getActivityName());
        }

        MATE.log(explorationStrategy + " visited activities " + visitedActivities.size());
        for (String act: visitedActivities)
            MATE.log("   "+act);
    }

    public void handleAuth(UiDevice device){

        if (this.packageName != null && this.packageName.contains("com.android.packageinstaller")) {
            long timeA = new Date().getTime();


            boolean goOn = true;
            while (goOn){

                DeviceMgr dmgr = new DeviceMgr(device,"");
                IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
                Vector<Action> actions = screenState.getActions();
                for (Action action: actions){
                    if(action.getWidget().getId().contains("allow")){
                        try {
                            dmgr.executeAction(action);
                        } catch (AUTCrashException e) {
                            MATE.log_acc(e.getStackTrace().toString());
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                this.packageName = device.getCurrentPackageName();
                MATE.log("new package name: " + this.packageName);
                long timeB = new Date().getTime();
                if (timeB-timeA>30000)
                    goOn=false;
                if (!this.packageName.contains("com.android.packageinstaller"))
                    goOn=false;
            }
        }


    }

    public static void log(String msg) {
        Log.i("apptest", msg);
    }

    public static void logsum(String msg) {
        Log.e("acc", msg);
        logMessage+=msg+"\n";

    }

    public static void log_acc(String msg){
        Log.e("acc",msg);
        logMessage+=msg+"\n";
    }

    public static void log_vin(String msg){
        Log.i("vinDebug",msg);
        logMessage+=msg+"\n";
    }

    public void listActivities(Context context){

        //list all activities of the application being executed
        PackageManager pm = (PackageManager) context.getPackageManager();
        try {
            PackageInfo pinfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activities = pinfo.activities;
            for (int i=0; i<activities.length; i++){
                log("Activity "+(i+1)+": " + activities[i].name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getPackageName(){
        return packageName;
    }

    public IGUIModel getGuiModel(){
        return guiModel;
    }

    public static void logactivity(String activityName) {
        Log.i("acc","ACTIVITY_VISITED: " + activityName);
    }
}
