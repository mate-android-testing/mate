package org.mate.exploration.random;

import android.support.test.uiautomator.UiDevice;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityInfoChecker;
import org.mate.accessibility.ContrastRatioAccessibilityCheck;
import org.mate.accessibility.MultipleContentDescCheck;
import org.mate.accessibility.results.AccessibilitySummary;
import org.mate.exceptions.AUTCrashException;
import org.mate.interaction.DeviceMgr;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import java.util.Date;
import java.util.Random;
import java.util.Vector;

import static org.mate.MATE.TIME_OUT;
//import static org.mate.MATE.checkedWidgets;
import static org.mate.MATE.device;

/**
 * Created by geyan on 11/06/2017.
 */

public class UniformRandomForAccessibility {
    private DeviceMgr deviceMgr;
    private String packageName;
    private MATE mate;
    private IScreenState launchState;
    private Vector<Action> executableActions;
    private IGUIModel guiModel;
    public static String currentActivityName;


    public UniformRandomForAccessibility(DeviceMgr deviceMgr,
                                         String packageName, MATE mate, IGUIModel guiModel){
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        this.mate = mate;
        this.guiModel = guiModel;
        this.currentActivityName="";
    }

    public Action getActionSplash(Vector<Action> actions){
        Action action = null;
        for (Action act: actions){
            if (act.getWidget().getClazz().contains("ImageButton")) {
                if (act.getWidget().getId().contains("next") ||  act.getWidget().getId().contains("done"))
                    return act;
            }
        }
        return action;
    }

    public static int totalNumberOfChecks;
    public static int numberOfRedundantChecks;

    public void startUniformRandomExploration(IScreenState selectedScreenState, long runningTime) {


        MATE.log("TIMEOUT: "+MATE.TIME_OUT);
        long currentTime = new Date().getTime();
        this.launchState = selectedScreenState;
        int numberOfActions = 0;
        int totalNumberOfActions = 0;
        numberOfRedundantChecks=0;
        totalNumberOfChecks = 0;
        while (currentTime - runningTime <= MATE.TIME_OUT){

            try{
                //while (totalNumberOfActions<=MATE.TIME_OUT){
                //MATE.log(" " +totalNumberOfActions);
                //System.out.println(currentTime - runningTime+" gap");
                //get a list of all executable actions as long as this state is different from last state
                executableActions = selectedScreenState.getActions();
                //MATE.log(" time to get possible actions: " + (l2-l1));

                //for (Action execact: executableActions){
                // MATE.log("act: " + execact.getActionType() + "  "+execact.getWidget().getId()+ " " + execact.getWidget().getClazz());
                //}
                //select one action randomly
                Action action = null;// = getActionSplash(executableActions);
                if (action==null)
                    action = executableActions.get(selectRandomAction(executableActions.size()));

                try {
                    //execute this selected action
                    deviceMgr.executeAction(action);
                    //MATE.log(" time to execute action: " + (l2-l1));
                    numberOfActions++;
                    totalNumberOfActions++;
                    //MATE.log("total number of actions: " + totalNumberOfActions);

                    IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

                    long timeToWait = waitForProgressBar(state);
                    if (timeToWait>300)
                        timeToWait+=2000;
                    action.setTimeToWait(timeToWait);
                    state = ScreenStateFactory.getScreenState("ActionsScreenState");


                    String currentPackageName = state.getPackageName();
                    if (currentPackageName!=null && currentPackageName.contains("com.android.packageinstaller")) {
                        currentPackageName = handleAuth(deviceMgr, currentPackageName);
                    }
                    if (currentPackageName==null) {
                        MATE.logsum("CURRENT PACKAGE: NULL");
                        return;
                    }
                    //MATE.log(" time to get current package: " + (l2-l1));

                    //check the validity of current package after executing the selected action
                    if (!currentPackageName.equals(this.packageName)||numberOfActions>=MATE.RANDOM_LENGH) {
                        MATE.log("package name: " + this.packageName);
                        deviceMgr.restartApp();
                        state = ScreenStateFactory.getScreenState("ActionsScreenState");
                        numberOfActions=0;
                    }
                    else{
                        //creates a new node and or a new edge
                        //state.setId("S"+totalNumberOfActions);
                        boolean newState = guiModel.updateModel(action,state);
                        //EnvironmentManager.screenShot(currentPackageName,state.getId());
                        if (newState || numberOfActions==1){

                            this.currentActivityName=state.getActivityName();
                            MATE.log("start ACCESSIBILITY CHECKS: " );
                            int currentNumberOfChecks = totalNumberOfChecks;
                            MATE.logactivity(state.getActivityName());
                            AccessibilityInfoChecker accChecker = new AccessibilityInfoChecker();
                            AccessibilitySummary.currentActivityName=state.getActivityName();
                            AccessibilitySummary.currentPackageName=state.getPackageName();
                            accChecker.runAccessibilityTests(state);
                            //MATE.log_acc("CHECK CONTRAST");
                            MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(state);


                            ContrastRatioAccessibilityCheck contrastChecker = new ContrastRatioAccessibilityCheck(state.getPackageName(),state.getActivityName(),state.getId(),device
                                    .getDisplayWidth(),device.getDisplayHeight());

                            for (Widget widget: state.getWidgets()) {

                                boolean contrastRatioOK = contrastChecker.check(widget);

                                if (!contrastRatioOK)
                                    AccessibilitySummary.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",widget,String.valueOf(contrastChecker.contratio));

                                boolean multDescOK = multDescChecker.check(widget);
                                UniformRandomForAccessibility.totalNumberOfChecks++;
                                if (!multDescOK)
                                    AccessibilitySummary.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",widget,"");

                            }

                            int roundNumberOfChecks = totalNumberOfChecks - currentNumberOfChecks;
                            if (!newState){
                                numberOfRedundantChecks+=roundNumberOfChecks;
                            }
                            MATE.log("finish ACCESSIBILITY CHECKS: " );
                        }
                    }

                    selectedScreenState = state;
                    //MATE.log(" time to get new state: " + (l2-l1));

                } catch (AUTCrashException e) {
                    deviceMgr.handleCrashDialog();
                }
            }
            catch(Exception e){
                MATE.log("UNKNOWN EXCEPTION");
            }


            currentTime = new Date().getTime();
        }
        MATE.log_acc("NUMBER_OF_ACTIONS: " + totalNumberOfActions);
        //MATE.log_acc("NUMBER_OF_CHECKS: " + totalNumberOfChecks+","+numberOfRedundantChecks);
        //MATE.log_acc("NUMBER_OF_CHECKS: " + checkedWidgets.size());


    }

    public int selectRandomAction(int executionActionSize){
        Random rand = new Random();
        return rand.nextInt(executionActionSize);
    }

    private long waitForProgressBar(IScreenState state) {
        long ini = new Date().getTime();
        long end = new Date().getTime();
        boolean hasProgressBar = true;
        while (hasProgressBar && (end-ini)<22000) {
            hasProgressBar=false;
            for (Widget widget : state.getWidgets()) {
                //if (widget.getClazz().contains("ProgressBar"))
                //  MATE.log("__"+widget.getId()+" _ " + widget.isEnabled() + " _ cd: " + widget.getContentDesc());
                if (widget.getClazz().contains("ProgressBar") && widget.isEnabled() && widget.getContentDesc().contains("Loading")) {
                    MATE.log("WAITING PROGRESS BAR TO FINISH");
                    hasProgressBar = true;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    state = ScreenStateFactory.getScreenState(state.getType());
                }
            }
            end = new Date().getTime();
        }
        return end-ini;
    }

    public String handleAuth(DeviceMgr dmgr,String currentPackage) {

        if (currentPackage.contains("com.android.packageinstaller")) {
            long timeA = new Date().getTime();


            boolean goOn = true;
            while (goOn) {

                IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
                Vector<Action> actions = screenState.getActions();
                for (Action action : actions) {
                    if (action.getWidget().getId().contains("allow")) {
                        try {
                            dmgr.executeAction(action);
                        } catch (AUTCrashException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                currentPackage = device.getCurrentPackageName();
                MATE.log("new package name: " + currentPackage);
                long timeB = new Date().getTime();
                if (timeB - timeA > 30000)
                    goOn = false;
                if (!currentPackage.contains("com.android.packageinstaller"))
                    goOn = false;
            }
            return currentPackage;
        } else
            return currentPackage;
    }
}
