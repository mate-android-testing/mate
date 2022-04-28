package org.mate.exploration.deprecated.random;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationChecker;
import org.mate.accessibility.check.bbc.widgetbased.MultipleContentDescCheck;
import org.mate.accessibility.check.bbc.widgetbased.TextContrastRatioAccessibilityCheck;
import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.interaction.action.ui.WidgetAction;
import org.mate.commons.utils.MATELog;
import org.mate.interaction.DeviceMgr;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.state.ScreenStateType;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Deprecated
public class UniformRandomForAccessibility {
    private DeviceMgr deviceMgr;
    private String packageName;
    private List<WidgetAction> executableActions;
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
        while (currentTime - runningTime <= Registry.getTimeout()){

            try{
                //get a list of all executable actions as long as this state is different from last state
                executableActions = selectedScreenState.getWidgetActions();

                //select one action randomly
                WidgetAction action = executableActions.get(selectRandomAction(executableActions.size()));

                try {
                    //execute this selected action
                    deviceMgr.executeAction(action);

                    //update number of actions for statistics purpose
                    numberOfActions++;
                    totalNumberOfActions++;

                    //create an object that represents the screen
                    //using type: ActionScreenState
                    IScreenState state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

                    //check whether there is a progress bar on the screen
                    long timeToWait = waitForProgressBar(state);
                    //if there is a progress bar
                    if (timeToWait>0) {
                        //get a new state
                        state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                    }

                    //get the package name of the app currently running
                    String currentPackageName = state.getPackageName();
                    //check whether it is an installation package
                    if (currentPackageName!=null && currentPackageName.contains("com.android.packageinstaller")) {
                        currentPackageName = handleAuth(deviceMgr, currentPackageName);
                    }
                    //if current package is null, emulator has crashed/closed
                    if (currentPackageName==null) {
                        MATELog.log_debug("CURRENT PACKAGE: NULL");
                        return;
                    }

                    //check whether the package of the app currently running is from the app under test
                    //if it is not, restart app
                    //check also whether the limit number of actions before restart has been reached
                    if (!currentPackageName.equals(this.packageName)||numberOfActions>= Properties.MAX_NUMBER_EVENTS()) {
                        MATELog.log("package name: " + this.packageName);
                        deviceMgr.restartApp();
                        state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
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
                    deviceMgr.pressHome();
                }
            }
            catch(Exception e){
                MATELog.log("UNKNOWN EXCEPTION");
            }

            currentTime = new Date().getTime();
        }

        Registry.getEnvironmentManager().tunnelLegacyCmd("FINISH"+ "_" + Registry.getEnvironmentManager().getEmulator()
                + selectedScreenState.getPackageName());
        MATELog.log_acc("NUMBER_OF_ACTIONS: " + totalNumberOfActions);
    }

    private void runAccessibilityChecks(IScreenState state, IScreenState selectedScreenState) {


        Registry.getEnvironmentManager().takeScreenshot(state.getPackageName(),state.getId());

        //updates the current activity name
        currentActivityName = state.getActivityName();
        MATELog.log("start ACCESSIBILITY CHECKS: " );
        MATELog.log_acc("ACTIVITY_VISITED: " + state.getActivityName());


        //prepare for collecting results
        AccessibilitySummaryResults.currentActivityName = state.getActivityName();
        AccessibilitySummaryResults.currentPackageName = state.getPackageName();

        //run accessibility checks implemented by Google ATF / eyes free:
        //   EditableContentDesc
        //   SpeakableTextPresent
        //   ClickableSpan
        //   TouchTargetSize
        //   DuplicateClickableBounds
        AccessibilityViolationChecker.runAccessibilityChecks(state);

        //run accessibility checks implemented by MATE team

        //create checker for multiple (duplicate) content description
        MultipleContentDescCheck multDescChecker = new MultipleContentDescCheck();
        //create checker for contrast issues
        TextContrastRatioAccessibilityCheck contrastChecker =
                new TextContrastRatioAccessibilityCheck();

        //run checks for each widget on the screen
        for (Widget widget: state.getWidgets()) {

            //run constrast check
            AccessibilityViolation contrastRatioViolationFound = contrastChecker.check(state, widget);

            if (contrastRatioViolationFound!=null) {
                //report accessibility flaw found
                MATELog.log("ADD CONTRAST FLAW");
                AccessibilitySummaryResults.addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",
                        widget, contrastRatioViolationFound.getInfo());



               // EnvironmentManager.markScreenshot(widget, selectedScreenState.getPackageName(),
                 //       selectedScreenState.getId(), "ACCESSIBILITY_CONTRAST_FLAW",
                    //    String.valueOf(contrastChecker.contratio));
            }

            //run multiple desc check
            AccessibilityViolation multViolation = multDescChecker.check(state,widget);
            if (multViolation!=null) {
                //report accessibility flaw found
                AccessibilitySummaryResults.addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW", widget, "");

                //EnvironmentManager.markScreenshot(widget, selectedScreenState.getPackageName(),
                  //      selectedScreenState.getId(), "DUPLICATE_SPEAKABLE_TEXT_FLAW",
                    //    "");
            }
        }
        MATELog.log("finish ACCESSIBILITY CHECKS: " );
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
                    MATELog.log("WAITING PROGRESS BAR TO FINISH");
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

                IScreenState screenState = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                List<WidgetAction> actions = screenState.getWidgetActions();
                for (WidgetAction action : actions) {
                    if (action.getWidget().getId().contains("allow")) {
                        try {
                            dmgr.executeAction(action);
                        } catch (AUTCrashException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }

                currentPackage = Registry.getDeviceMgr().getCurrentPackageName();
                MATELog.log("new package name: " + currentPackage);
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
