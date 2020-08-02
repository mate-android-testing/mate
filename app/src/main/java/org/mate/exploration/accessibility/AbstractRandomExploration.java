package org.mate.exploration.accessibility;

import android.app.ActivityManager;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.IAccessibilityViolationChecker;
import org.mate.accessibility.check.wcag.AccessibilityViolationCheckerWCAG;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.state.executables.ActionsScreenState;
import org.mate.state.executables.RelatedState;
import org.mate.ui.ActionMeasure;
import org.mate.ui.ActionType;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import static org.mate.exploration.accessibility.StateUtils.takeScreenShotDetectProperties;

public abstract class AbstractRandomExploration {

    protected String packageName;
    protected UIAbstractionLayer uiAbstractionLayer;
    protected IAccessibilityViolationChecker wcagChecker;
    private Set<String> allDistinctViolations;
    private Set<String> allWidgetsWithIssues;
    private Hashtable<String,Integer> violationsByType;
    private String[] violationTypesChecked;
    private Hashtable<String, ActionMeasure> widgetActionMeasures;
    private List<String> visitedWidgets;
    private String projectName;

    public AbstractRandomExploration(UIAbstractionLayer uiAbstractionLayer){
        this.uiAbstractionLayer = uiAbstractionLayer;
        this.packageName = this.uiAbstractionLayer.getLastScreenState().getPackageName();
        wcagChecker = new AccessibilityViolationCheckerWCAG();
        allDistinctViolations = new HashSet<String>();
        allWidgetsWithIssues = new HashSet<String>();
        widgetActionMeasures = new Hashtable<String, ActionMeasure>();
        visitedWidgets = new ArrayList<String>();
        initializeViolationsByType();
        projectName = "";
    }

    public abstract WidgetAction nextAction(IScreenState state);

    public void run(){
        IScreenState currentState = MATE.uiAbstractionLayer.getLastScreenState();

        StateUtils.takeScreenShotDetectProperties(currentState);
        runAccessibilityChecks(currentState);

        long runningTime = new Date().getTime();
        long currentTime = new Date().getTime();
        int numberOfActions = 0;
        int totalNumberOfActions = 0;

        projectName = packageName;
        projectName = InstrumentationRegistry.getArguments().getString("projectName");
        String strTimeout = InstrumentationRegistry.getArguments().getString("timeBudget");
        if (strTimeout!=null){
            try {
                long parTimeout = Long.valueOf(strTimeout);
                MATE.TIME_OUT = parTimeout * 60 * 1000;
            }
            catch(Exception e){
                MATE.TIME_OUT = 15*60*1000;
            }
        }

        //MATE.TIME_OUT = 3*60*1000;
        while (currentTime - runningTime <= MATE.TIME_OUT && Registry.getEnvironmentManager().isActive()){
            //MATE.log(currentTime-runningTime + "   vs   " + MATE.TIME_OUT);
            try{

                //select one action randomly
                WidgetAction action = this.nextAction(currentState);
                //action = new WidgetAction(new Widget("","",""), ActionType.MANUAL_ACTION);
                //MATE.log("WAITING");
                //Thread.sleep(2000);
                //MATE.log("Done waiting");

                //update number of actions for statistics purpose
                numberOfActions++;
                totalNumberOfActions++;

                int visitedStates = uiAbstractionLayer.getRecordedScreenStates().size();
                UIAbstractionLayer.ActionResult result = uiAbstractionLayer.executeAction(action);
                action.setExecuted(true);
                currentState = uiAbstractionLayer.getLastScreenState();
                //takeScreenShotDetectProperties(currentState);
                //run acc checks

                if (result== UIAbstractionLayer.ActionResult.SUCCESS){

                    //get the package name of the app currently running
                    String currentPackageName = currentState.getPackageName();
                    //check whether it is an installation package
                    if (currentPackageName!=null && currentPackageName.contains("com.android.packageinstaller")) {
                        currentState=handleAuth(currentPackageName);
                        //takeScreenShotDetectProperties(currentState);
                        //run acc checks
                    }
                }

                if (result== UIAbstractionLayer.ActionResult.SUCCESS_OUTBOUND){
                    //MATE.log("SUCCESS OUTBOUND");
                    currentState = uiAbstractionLayer.getLastScreenState();
                    //takeScreenShotDetectProperties(currentState);
                    uiAbstractionLayer.restartApp();
                    currentState = uiAbstractionLayer.getLastScreenState();
                    //takeScreenShotDetectProperties(currentState);
                    numberOfActions=0;
                }


                if (result== UIAbstractionLayer.ActionResult.FAILURE_APP_CRASH){
                    //MATE.log("APP CRASH");
                }

                if (result == UIAbstractionLayer.ActionResult.FAILURE_EMULATOR_CRASH){
                    //MATE.log("EMULATOR CRASH");
                    return;
                }

                if (result == UIAbstractionLayer.ActionResult.FAILURE_UNKNOWN){

                }

                if (visitedStates<uiAbstractionLayer.getRecordedScreenStates().size()){
                    if (currentState.getPackageName().equals(packageName)) {
                        increaseCount(action,currentState);
                        //MATE.log("IS A NEW STATE");
                        takeScreenShotDetectProperties(currentState);
                        long beforeTime = new Date().getTime();
                        runAccessibilityChecks(currentState);
                        long afterTime = new Date().getTime();
                        //MATE.log("Time to run acc checks: " + (afterTime-beforeTime));
                        //COUNT component -> states
                    }
                }

            }
            catch(Exception e){
                MATE.log("UNKNOWN EXCEPTION");
                e.printStackTrace();
            }

            currentTime = new Date().getTime();
        }

        reportResults(currentTime - runningTime);
    }

    private void reportResults(long execTime){

        // Registry.getEnvironmentManager().tunnelLegacyCmd("FINISH"+ "_" + Registry.getEnvironmentManager().getEmulator()
        //       + selectedScreenState.getPackageName());

        int numberVisitedStates = this.getNumberOfVisitedStates();
        //MATE.log("Number of visited states: " + numberVisitedStates);

        int numberOfWidgetsAnalyzed = this.getNumberOfDistinctWidgetsAnalyzed();
        //MATE.log("Number of distinct widgets analyzed: " + numberOfWidgetsAnalyzed);

        String header = "";
        header+="projectname,packagename,algorithm,sessionID,activities_executed,states_visited,violations,";
        header+="widgets,widgets_with_issues,";
        for (int i=0; i<violationTypesChecked.length; i++){
            String type = violationTypesChecked[i];
            header+=type+",";
        }
        header+="exectime";


        int numberOfViolations = allDistinctViolations.size();
        int numberOfWidgetsWithIssues = allWidgetsWithIssues.size();

        Context context = InstrumentationRegistry.getTargetContext().getApplicationContext();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);


        String algorithm = this.getClass().getName();

        int numberOfDistinctActivities = this.getNumberOfDistinctActivitiesAnalyzed();
        String values = "";
        values+=projectName+","+packageName+","+algorithm+","+MATE.sessionID+","+numberOfDistinctActivities+","+numberVisitedStates+","+numberOfViolations+",";
        values+=numberOfWidgetsAnalyzed+","+numberOfWidgetsWithIssues+",";
        for (int i=0; i<violationTypesChecked.length; i++){
            String type = violationTypesChecked[i];
            int count = violationsByType.get(type);
            values+=count+",";
        }
        values+=execTime;


        Registry.getEnvironmentManager().generalReport(header,values);

        MATE.log(values);

        header = "package_name,sessionID,widget_type,action_type,new_states,new_widgets";

        for (String actionKey: widgetActionMeasures.keySet()){
            ActionMeasure measure = widgetActionMeasures.get(actionKey);
            MATE.log(actionKey+": new states: " + measure.getNewStateCount()+ ", new widgets: " + measure.getNewWidgetCount());
            String parts[] = actionKey.split("-");
            values = packageName+","+MATE.sessionID+","+parts[0]+","+parts[1]+","+measure.getNewStateCount()+","+measure.getNewWidgetCount();
            Registry.getEnvironmentManager().measureReport(header,values);
           // MATE.log(values);
        }
    }

    protected void initializeViolationsByType(){
        violationsByType = new Hashtable<String,Integer>();
        violationTypesChecked = new String[9];
        violationTypesChecked[0] = AccessibilityViolationType.NON_TEXT_CONTENT.getValue();
        violationTypesChecked[1] = AccessibilityViolationType.IDENTIFY_INPUT_PURPOSE.getValue();
        violationTypesChecked[2] = AccessibilityViolationType.CONSTRAST_MINUMUM.getValue();
        //violationTypesChecked[3] = AccessibilityViolationType.CONSTRAST_ENHANCED.getValue();
        //violationTypesChecked[4] = AccessibilityViolationType.NON_TEXT_CONTRAST.getValue();
        violationTypesChecked[3] = AccessibilityViolationType.TARGET_SIZE.getValue();
        violationTypesChecked[4] = AccessibilityViolationType.DUPLICATE_CONTENT_DESCRIPTION.getValue();
        violationTypesChecked[5] = AccessibilityViolationType.SPACING.getValue();
        violationTypesChecked[6] = AccessibilityViolationType.USE_OF_COLOR.getValue();
        violationTypesChecked[7] = AccessibilityViolationType.ORIENTATION.getValue();
        violationTypesChecked[8] = AccessibilityViolationType.PAGE_TITLED.getValue();

        for (String acctype: violationTypesChecked)
            violationsByType.put(acctype,0);
    }

    private int numberOfVisitedStatesOfDifferenceType(String type) {
        Set<String> statesID = new HashSet<String>();
        for (IScreenState state: uiAbstractionLayer.getRecordedScreenStates()){
            //if (state.getPackageName().equals(packageName)){
            if (((ActionsScreenState) state).getTypeOfNewState().contains(type))
                statesID.add(state.getId());
            //}
        }
        return statesID.size();
    }

    private void increaseCount(WidgetAction action, IScreenState state) {
        String actionKey = action.getWidget().getClazz()+"-"+action.getActionType();
        ActionMeasure measure = widgetActionMeasures.get(actionKey);
        if (measure==null) {
            measure = new ActionMeasure();
            widgetActionMeasures.put(actionKey,measure);
        }

        int count = 0;
        measure.setNewStateCount(measure.getNewStateCount()+1);
        for (Widget widget: state.getWidgets()){
            String widgetKey = state.getActivityName() + "-"+widget.getId();
            if (!visitedWidgets.contains(widgetKey)) {
                visitedWidgets.add(widgetKey);
                count++;
            }
        }
        measure.setNewWidgetCount(measure.getNewWidgetCount()+count);
        widgetActionMeasures.put(actionKey,measure);
    }

    private void addDistinctViolations(List<AccessibilityViolation> violations){
        for (AccessibilityViolation violation: violations){
            String activityName = violation.getState().getActivityName();
            String widgetId = violation.getWidget().getId();
            String violationType = violation.getType().getValue();
            String keyViolation = activityName+"-"+widgetId+"-"+violationType;
            String keyWidget = activityName+"-"+widgetId;
            if (!allDistinctViolations.contains(keyViolation)){
                allDistinctViolations.add(keyViolation);
                //collect info
                if (violationsByType.get(violationType)==null)
                    violationsByType.put(violationType,0);
                Integer count = violationsByType.get(violationType);
                count = count + 1;
                violationsByType.put(violationType,count);
            }

            allWidgetsWithIssues.add(keyWidget);
        }
    }

    private void runAccessibilityChecks(IScreenState state) {
        List<AccessibilityViolation> violations = wcagChecker.runAccessibilityChecks(state);
        addDistinctViolations(violations);

        /*
        MATE.log_acc("Amount of violations found: " + violations.size());
        for (AccessibilityViolation violation: violations){
            MATE.log_acc(violation.getType() + " " + violation.getWidget().getId() + ":"+violation.getWidget().getText()+ " -- " + violation.getInfo());
        }
         */
    }


    protected int getNumberOfDistinctActivitiesAnalyzed(){
        Set<String> distinctsAct = new HashSet<String>();
        for (IScreenState state: uiAbstractionLayer.getRecordedScreenStates()){
            if (state.getPackageName().equals(packageName)){
                distinctsAct.add(state.getActivityName());
            }
        }
        return distinctsAct.size();
    }


    protected int getNumberOfDistinctWidgetsAnalyzed(){
        Set<String> distinctsID = new HashSet<String>();
        for (IScreenState state: uiAbstractionLayer.getRecordedScreenStates()){
            if (state.getPackageName().equals(packageName)){
                for (Widget widget: state.getWidgets()){
                    String uniqueID = widget.getIdByActivity();
                    uniqueID += widget.isEmpty();
                    uniqueID += widget.getColor();
                    distinctsID.add(uniqueID);
                }
            }
        }
        return distinctsID.size();
    }

    private int getNumberOfVisitedStates(){
        Set<String> statesID = new HashSet<String>();
        for (IScreenState state: uiAbstractionLayer.getRecordedScreenStates()){
            if (state.getPackageName().equals(packageName)){
                statesID.add(state.getId());
            }
        }

        MATE.log("States visited");
        for (String stid: statesID){
            //MATE.log(stid);
        }

        MATE.log("missing");
        for (int i=0; i<statesID.size();i++){
            String str = "S"+i+"_";
            boolean found = false;
            for (String stid: statesID){
                if (stid.contains(str))
                    found = true;
            }
            if (!found)
                MATE.log("missing: " + str);
        }

        return statesID.size();
    }



    public IScreenState handleAuth(String currentPackage) {

        if (currentPackage.contains("com.android.packageinstaller")) {
            long timeA = new Date().getTime();


            boolean goOn = true;
            while (goOn) {

                IScreenState screenState = ScreenStateFactory.getScreenState("ActionsScreenState");
                List<WidgetAction> actions = screenState.getActions();
                for (WidgetAction action : actions) {
                    if (action.getWidget().getId().contains("allow")) {
                        uiAbstractionLayer.executeAction(action);
                    }
                }

                currentPackage = uiAbstractionLayer.getLastScreenState().getPackageName();
                long timeB = new Date().getTime();
                if (timeB - timeA > 30000)
                    goOn = false;
                if (!currentPackage.contains("com.android.packageinstaller"))
                    goOn = false;
            }

        }
        return uiAbstractionLayer.getLastScreenState();
    }
}
