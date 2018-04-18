package org.mate;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import org.mate.exceptions.AUTCrashException;
import org.mate.exploration.aco.ACO;
import org.mate.exploration.depthfirst.DepthFirst;
import org.mate.exploration.random.ManualExploration;
import org.mate.exploration.random.UniformRandom;
import org.mate.exploration.random.UniformRandomForAccessibility;
import org.mate.interaction.DeviceMgr;
import org.mate.model.IGUIModel;
import org.mate.model.graph.GraphGUIModel;
import org.mate.model.graph.StateGraph;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;

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
    public static long RANDOM_LENGH = 500;
    private long runningTime = new Date().getTime();
    public static long TIME_OUT = 30 * 60 * 1000;



    private int antGeneration = 1;
    private int antNumber = 1;
    private int antLength = 10;
    private GraphGUIModel completeModel;
//    private GraphGUIModel partialModelForOneGeneration;
    private StateGraph graphForOneAnt;

    public static String logMessage;


    //public static Vector<String> checkedWidgets = new Vector<String>();
    public static Set<String> visitedActivities = new HashSet<String>();

    public void testApp(String explorationStrategy){
        logMessage="";
        TIME_OUT = EnvironmentManager.getTimeout()*60*1000;
        MATE.log("TIMEOUT: " + TIME_OUT);
        //Defines the class that represents the device
        Instrumentation instrumentation =  getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        this.packageName = device.getCurrentPackageName();
        MATE.log("Package name: " + this.packageName);
        //Defines the class to handle the user interactions
        handleAuth(device);


        String emulator = EnvironmentManager.detectEmulator(this.packageName);
        MATE.log("EMULATOR: " + emulator);
        runningTime = new Date().getTime();
        try{
            if (emulator!=null && !emulator.equals("")){

                this.deviceMgr = new DeviceMgr(device,packageName);

                listActivities(instrumentation.getContext());
                //Analyses the screen and gets an object that represents the screen state (configuration of widgets)
                IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

                //gets the initial time
                long t1 = new Date().getTime();

                //starts exploring the app starting from the current node, which, in this case, is the main activity/initial state
                if (explorationStrategy.equals("DepthFirst")) {
                    //creates the graph that represents the GUI model
                    this.guiModel = new GraphGUIModel();
                    //first state (root node - action ==null)
                    this.guiModel.updateModel(null,state);
                    DepthFirst depthFirst = new DepthFirst(deviceMgr, packageName, guiModel);
                    depthFirst.startExploreDepthFirst(guiModel.getCurrentStateId(), runningTime);
                    MATE.log("total time: " + total_time);
                }

                if (explorationStrategy.equals("AccRandom")) {
                    //creates the graph that represents the GUI model
                    this.guiModel = new GraphGUIModel();
                    //first state (root node - action ==null)
                    this.guiModel.updateModel(null,state);
                    UniformRandomForAccessibility unirandomacc = new UniformRandomForAccessibility(deviceMgr,packageName,this,guiModel);
                    unirandomacc.startUniformRandomExploration(state,runningTime);
                }

                if (explorationStrategy.equals("UniformRandom")){
                    UniformRandom uniformRandomExploration = new UniformRandom(deviceMgr,packageName,this);
                    uniformRandomExploration.startUniformRandomExploration(state,runningTime);
                }


                if (explorationStrategy.equals("ManualExploration")) {
                    this.guiModel = new GraphGUIModel();
                    //first state (root node - action ==null)
                    this.guiModel.updateModel(null,state);
                    ManualExploration manualExploration = new ManualExploration(deviceMgr,packageName,this,guiModel);
                    manualExploration.startManualExploration(runningTime);
                }

                checkVisitedActivities(explorationStrategy);
                //AccessibilityChecker.checkAccessibility(packageName,guiModel);
            }
            else
                MATE.log("Emulator is null");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            EnvironmentManager.releaseEmulator();
            EnvironmentManager.deleteAllScreenShots(packageName);
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

        if (this.packageName.contains("com.android.packageinstaller")) {
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

    public void executeACO(){
        //Defines the class that represents the device
        Instrumentation instrumentation =  getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        packageName = device.getCurrentPackageName();
        deviceMgr = new DeviceMgr(device,packageName);
        //create a model for all ants in all generations
        completeModel = new GraphGUIModel();
        ACO aco = new ACO(deviceMgr,packageName,completeModel);
        aco.startExploreACO();
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

    /*
    private int getNumberOfActivitiesVisited(){
        Set<String> visitedActivities = new HashSet<String>();
        for (ScreenNode scnd: guiModel.getStateGraph().getScreenNodes().values()){
            visitedActivities.add(scnd.getActivityName());
        }
        return visitedActivities.size();
    }

    private void reportActivitiesVisited() {

        listActivities(getInstrumentation().getContext());

        log("*****");
        Set<String> visitedActivities = new HashSet<String>();
        for (ScreenNode scnd: guiModel.getStateGraph().getScreenNodes().values()){
            visitedActivities.add(scnd.getActivityName());
        }
        MATE.log("Number of activities: " + visitedActivities.size());
        for (String actName: visitedActivities){
            MATE.log("  "+actName);
        }
    }
*/
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

    public int getNumberOfActivities(Context context){
        PackageManager pm = (PackageManager) context.getPackageManager();
        ActivityInfo[] activities = null;
        try {
            PackageInfo pinfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            activities = pinfo.activities;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (activities!=null)
            return activities.length;
        else
            return 0;
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
