package org.mate.exploration.random;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityInfoChecker;
import org.mate.accessibility.check.ContrastRatioAccessibilityCheck;
import org.mate.accessibility.check.MultipleContentDescCheck;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.exceptions.AUTCrashException;
import org.mate.interaction.DeviceMgr;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.mate.MATE.device;

public class UniformRandomForAccessibility {
    private DeviceMgr deviceMgr;
    private String packageName;
    private List<Action> executableActions;
    private IGUIModel guiModel;
    public static String currentActivityName;
    private boolean runAccChecks;
    public static int totalNumberOfChecks;


    public UniformRandomForAccessibility(DeviceMgr deviceMgr,
                                         String packageName, IGUIModel guiModel, boolean runAccChecks){
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        this.guiModel = guiModel;
        currentActivityName="";
        this.runAccChecks = runAccChecks;
    }



    public void startUniformRandomExploration(IScreenState selectedScreenState, long runningTime) {

        long currentTime = new Date().getTime();
        int numberOfActions = 0;
        int totalNumberOfActions = 0;
        while (currentTime - runningTime <= MATE.TIME_OUT){

            try{
                //get a list of all executable actions as long as this state is different from last state
                executableActions = selectedScreenState.getActions();

                //select one action randomly
                Action action = executableActions.get(selectRandomAction(executableActions.size()));

                try {
                    //execute this selected action
                    deviceMgr.executeAction(action);

                    //update number of actions for statistics purpose
                    numberOfActions++;
                    totalNumberOfActions++;

                    //create an object that represents the screen
                    //using type: ActionScreenState
                    IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

                    //check whether there is a progress bar on the screen
                    long timeToWait = waitForProgressBar(state);
                    //if there is a progress bar
                    if (timeToWait>0) {
                        //add 2 sec just to be sure
                        timeToWait += 2000;
                        //set that the current action needs to wait before new action
                        action.setTimeToWait(timeToWait);
                        //get a new state
                        state = ScreenStateFactory.getScreenState("ActionsScreenState");
                    }

                    //get the package name of the app currently running
                    String currentPackageName = state.getPackageName();
                    //check whether it is an installation package
                    if (currentPackageName!=null && currentPackageName.contains("com.android.packageinstaller")) {
                        currentPackageName = handleAuth(deviceMgr, currentPackageName);
                    }
                    //if current package is null, emulator has crashed/closed
                    if (currentPackageName==null) {
                        MATE.logsum("CURRENT PACKAGE: NULL");
                        return;
                    }

                    //check whether the package of the app currently running is from the app under test
                    //if it is not, restart app
                    //check also whether the limit number of actions before restart has been reached
                    if (!currentPackageName.equals(this.packageName)||numberOfActions>=MATE.RANDOM_LENGH) {
                        MATE.log("package name: " + this.packageName);
                        deviceMgr.restartApp();
                        state = ScreenStateFactory.getScreenState("ActionsScreenState");
                        numberOfActions=0;
                    }
                    else{
                        //if the app under test is running
                        //try to update GUI model with the current screen state
                        //it the current screen is a screen not explored before,
                        //   then a new state is created (newstate = true)
                        boolean newState = guiModel.updateModel(action,state);


                        if (this.runAccChecks) {
                            //if it is a new state or the initial screen (first action)
                            //    then check the accessibility properties of the screen
                            if (newState || numberOfActions == 1) {

                                runAccessibilityChecks(state, selectedScreenState);

                            }
                        }
                    }

                    selectedScreenState = state;

                } catch (AUTCrashException e) {
                    deviceMgr.handleCrashDialog();
                }
            }
            catch(Exception e){
                MATE.log("UNKNOWN EXCEPTION");
            }

            currentTime = new Date().getTime();
        }

        EnvironmentManager.sendCommandToServer("FINISH"+ "_" + EnvironmentManager.emulator
                + selectedScreenState.getPackageName());
        MATE.log_acc("NUMBER_OF_ACTIONS: " + totalNumberOfActions);
    }

    private void runAccessibilityChecks(IScreenState state, IScreenState selectedScreenState) {


        EnvironmentManager.screenShot(state.getPackageName(),state.getId());

        //updates the current activity name
        currentActivityName = state.getActivityName();
        MATE.log("start ACCESSIBILITY CHECKS: " );
        MATE.logactivity(state.getActivityName());


        //prepare for collecting results
        AccessibilitySummaryResults.currentActivityName = state.getActivityName();
        AccessibilitySummaryResults.currentPackageName = state.getPackageName();

        //run accessibility checks implemented by Google ATF / eyes free:
        //   EditableContentDesc
        //   SpeakableTextPresent
        //   ClickableSpan
        //   TouchTargetSize
        //   DuplicateClickableBounds
        AccessibilityInfoChecker accChecker = new AccessibilityInfoChecker();
        accChecker.runAccessibilityTests(state);

        //run accessibility checks implemented by MATE team

        //create checker for multiple (duplicate) content description
        MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck(state);
        //create checker for contrast issues
        ContrastRatioAccessibilityCheck contrastChecker =
                new ContrastRatioAccessibilityCheck(state.getPackageName(),
                        state.getActivityName(),
                        state.getId(),
                        device.getDisplayWidth(),
                        device.getDisplayHeight());

        //run checks for each widget on the screen
        for (Widget widget: state.getWidgets()) {

            //run constrast check
            boolean contrastRatioOK = contrastChecker.check(widget);

            if (!contrastRatioOK) {
                //report accessibility flaw found
                AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",
                        widget, String.valueOf(contrastChecker.contratio));

                EnvironmentManager.markScreenshot(widget, selectedScreenState.getPackageName(),
                        selectedScreenState.getId(), "ACCESSIBILITY_CONTRAST_FLAW",
                        String.valueOf(contrastChecker.contratio));
            }

            //run multiple desc check
            boolean multDescOK = multDescChecker.check(widget);
            if (!multDescOK) {
                //report accessibility flaw found
                AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW", widget, "");

                EnvironmentManager.markScreenshot(widget, selectedScreenState.getPackageName(),
                        selectedScreenState.getId(), "DUPLICATE_SPEAKABLE_TEXT_FLAW",
                        "");
            }
        }
        MATE.log("finish ACCESSIBILITY CHECKS: " );
    }

    public int selectRandomAction(int executionActionSize){
        Random rand = new Random();
        return rand.nextInt(executionActionSize);
    }

    private long waitForProgressBar(IScreenState state) {
        long ini = new Date().getTime();
        long end = new Date().getTime();
        boolean hadProgressBar = false;
        boolean hasProgressBar = true;
        while (hasProgressBar && (end-ini)<22000) {
            hasProgressBar=false;
            for (Widget widget : state.getWidgets()) {
                if (widget.getClazz().contains("ProgressBar") && widget.isEnabled() && widget.getContentDesc().contains("Loading")) {
                    MATE.log("WAITING PROGRESS BAR TO FINISH");
                    hasProgressBar = true;
                    hadProgressBar=true;
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
        if (!hadProgressBar)
            return 0;
        return end-ini;
    }

    public String handleAuth(DeviceMgr dmgr,String currentPackage) {

        if (currentPackage.contains("com.android.packageinstaller")) {
            long timeA = new Date().getTime();


            boolean goOn = true;
            while (goOn) {

                IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
                List<Action> actions = screenState.getActions();
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
