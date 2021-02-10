package org.mate.exploration.deprecated.evolutionary;

import org.mate.MATE;
import org.mate.exceptions.AUTCrashException;
import org.mate.interaction.DeviceMgr;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.model.TestCase;
import org.mate.model.deprecated.graph.GraphGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.state.ScreenStateType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.mate.MATE.device;
import static org.mate.Properties.EVO_ITERATIONS_NUMBER;
import static org.mate.Properties.GREEDY_EPSILON;
import static org.mate.Properties.MAX_NUMBER_EVENTS;


@Deprecated
public class OnePlusOne {
    private int TCcounter;
    private int maxNumIterations = EVO_ITERATIONS_NUMBER();
    private final int maxNumTCs;
    private final int maxNumEvents;
    public static LinkedHashMap<String, TestCase> testsuite;
    private List<TestCase> crashArchive;

    private DeviceMgr deviceMgr;
    private String packageName;
    private List<WidgetAction> executableActions;
    private GraphGUIModel guiModel;
    public static String currentActivityName;
    private boolean isApp = true;
    private IScreenState selectedScreenState;

    public static HashMap<String,Set<String>> coverageArchive;


    public OnePlusOne(DeviceMgr deviceMgr, String packageName, IGUIModel guiModel) {

        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        this.guiModel = (GraphGUIModel) guiModel;
        this.currentActivityName = "";

        this.maxNumTCs = 1;
        this.maxNumEvents = MAX_NUMBER_EVENTS();
        testsuite = new LinkedHashMap<>();
        this.TCcounter = 0;

        this.crashArchive = new ArrayList<>();
        OnePlusOne.coverageArchive = new HashMap<>();
    }

    public void startEvolutionaryExploration(IScreenState selectedScreenState) {
        MATE.log_acc("Step 1: Initialization\n");
        testsuite = initialize(selectedScreenState);
        MATE.log_acc("original testcase: \n");
        MATE.log_acc("Visited Activities: " + testsuite.get("0").getVisitedActivities().toString());
        MATE.log_acc("Visited States" + testsuite.get("0").getVisitedStates().toString());
        int iterationsCounter = 0;
        while (iterationsCounter < maxNumIterations) {
            MATE.log_acc("Evolutionary Iteration Number: " + iterationsCounter);
            TestCase currentTC = testsuite.get("0");
            TestCase mutantTC = null;
            try {
                mutantTC = mutate(currentTC);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MATE.log_acc("Performance at evolution " + iterationsCounter);
            MATE.log_acc("Visited Activities: " + mutantTC.getVisitedActivities().toString());
            MATE.log_acc("Visited States" + mutantTC.getVisitedStates().toString());
            MATE.log_acc("Best score: " + currentTC.getVisitedStates().size());
            MATE.log_acc("Mutant score: " + mutantTC.getVisitedStates().size());

            if (mutantTC.getVisitedStates().size() > currentTC.getVisitedStates().size()) {
                MATE.log_acc("Mutant chosen as current population");
                testsuite.remove(String.valueOf(0));
                testsuite.put(String.valueOf(0), mutantTC);
            }
            iterationsCounter++;
        }


    }

    //create the initial population
    public LinkedHashMap<String, TestCase> initialize(IScreenState selectedScreenState) {

        this.selectedScreenState = selectedScreenState;
        int numberOfTCs = 0;

        while (numberOfTCs < maxNumTCs) {
            testsuite.put(String.valueOf(TCcounter), new TestCase(String.valueOf(TCcounter)));
            TestCase testcase = testsuite.get(String.valueOf(TCcounter));

            //Get the first state
            selectedScreenState = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
            guiModel.updateModelEVO(null, selectedScreenState);

            int numberOfActions = 0;
            isApp = true;

            while ((numberOfActions < this.maxNumEvents) && (isApp)) {

                testcase.updateVisitedActivities(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getActivityName());
                testcase.updateVisitedStates(this.guiModel.getStateById(this.guiModel.getCurrentStateId()));
                updateCoverageArchive(this.guiModel.getCurrentStateId(), testcase.getId());
                testcase.updateStatesMap(this.guiModel.getCurrentStateId(), String.valueOf(numberOfActions));


                try {
                    //get a list of all executable actions as long as this state is different from last state
                    executableActions = selectedScreenState.getActions();

                    //select one action randomly
                    WidgetAction action = executableActions.get(selectRandomAction(executableActions.size()));
                    testcase.addEvent(action);
                    numberOfActions++;
                    //MATE.log("EVENT: " + action.getActionType() + "; GUI OBJECT: " + action.getWidget().getClazz());

                    try {
                        //execute this selected action
                        deviceMgr.executeAction(action);

                        //TODO: testing sleep
                        //Thread.sleep(2500);

                        //create an object that represents the screen
                        //using type: ActionScreenState
                        IScreenState state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

                        //check whether there is a progress bar on the screen
                        long timeToWait = waitForProgressBar(state);
                        //if there is a progress bar
                        if (timeToWait > 0) {
                            //add 2 sec just to be sure
                            timeToWait += 2000;
                            //set that the current action needs to wait before new action
                            action.setTimeToWait(timeToWait);
                            //get a new state
                            state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                        }

                        //get the package name of the app currently running
                        String currentPackageName = state.getPackageName();
                        //check whether it is an installation package
                        if (currentPackageName != null && currentPackageName.contains("com.android.packageinstaller")) {
                            currentPackageName = handleAuth(deviceMgr, currentPackageName);
                        }
                        //if current package is null, emulator has crashed/closed
                        if (currentPackageName == null) {
                            MATE.log_acc("CURRENT PACKAGE: NULL");
                            return null;
                            //TODO: what to do when the emulator crashes?
                        }

                        //check whether the package of the app currently running is from the app under test
                        //if it is not, restart app
                        if (!currentPackageName.equals(this.packageName)) {
                            MATE.log_acc("current package different from app package: " + currentPackageName);

                            isApp = false;

                            deviceMgr.reinstallApp();
                            Thread.sleep(5000);
                            deviceMgr.restartApp();
                            Thread.sleep(2000);
                            //TODO: get state when restarts the app
                            state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                        } else {
                            //if the app under test is running
                            //try to update GUI model with the current screen state
                            //it the current screen is a screen not explored before,
                            //   then a new state is created (newstate = true)
                            //TODO: is this useless?
                            state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);


                            //update model with new state
                            boolean newState = guiModel.updateModel(action, state);
                            testcase.updateVisitedActivities(state.getActivityName());
                            testcase.updateVisitedStates(state);
                            updateCoverageArchive(state.getId(), testcase.getId());


                            //if it is a new state or the initial screen (first action)
                            //    then check the accessibility properties of the screen
                            if (newState || numberOfActions == 1) {
                                MATE.log("New State found:" + state.getId());

                                //runAccessibilityChecks(state, selectedScreenState);
                            }
                        }

                        selectedScreenState = state;

                    } catch (AUTCrashException e) {
                        MATE.log_acc("CRASH MESSAGE" + e.getMessage());
                        testcase.setCrashDetected();
                        crashArchive.add(testcase);
                        deviceMgr.pressHome();
                        isApp = false;

                        deviceMgr.reinstallApp();
                        Thread.sleep(5000);
                        deviceMgr.restartApp();
                    }
                } catch (Exception e) {
                    MATE.log("UNKNOWN EXCEPTION");
                }
            }

            MATE.log_acc("Test Case number: " + numberOfTCs + ", number of events: " + numberOfActions);
            numberOfTCs++;
            this.TCcounter++;
            if (numberOfTCs < maxNumTCs) {
                try {
                    deviceMgr.reinstallApp();
                    Thread.sleep(5000);
                    deviceMgr.restartApp();
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                selectedScreenState = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
            }
        }

        return testsuite;
    }


    private int selectRandomAction(int executionActionSize) {
        Random rand = new Random();
        return rand.nextInt(executionActionSize);
    }

    private long waitForProgressBar(IScreenState state) {
        long ini = new Date().getTime();
        long end = new Date().getTime();
        boolean hadProgressBar = false;
        boolean hasProgressBar = true;
        while (hasProgressBar && (end - ini) < 22000) {
            hasProgressBar = false;
            for (Widget widget : state.getWidgets()) {
                if (widget.getClazz().contains("ProgressBar") && widget.isEnabled() && widget.getContentDesc().contains("Loading")) {
                    MATE.log("WAITING PROGRESS BAR TO FINISH");
                    hasProgressBar = true;
                    hadProgressBar = true;
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
        return end - ini;
    }

    public String handleAuth(DeviceMgr dmgr, String currentPackage) {

        if (currentPackage.contains("com.android.packageinstaller")) {
            long timeA = new Date().getTime();


            boolean goOn = true;
            while (goOn) {

                IScreenState screenState = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                List<WidgetAction> actions = screenState.getActions();
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

    private TestCase mutate(TestCase originalTestCase) throws InterruptedException {

        MATE.log("REINSTALL APP");
        deviceMgr.reinstallApp();
        Thread.sleep(5000);
        deviceMgr.restartApp();
        Thread.sleep(2000);

        TestCase mutantTestCase = new TestCase(String.valueOf(this.TCcounter));


        selectedScreenState = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
        guiModel.updateModelEVO(null, selectedScreenState);


        int cutpoint = chooseCutpointEpsilonGreedy(originalTestCase);
        MATE.log_acc("Cut point chosen: " + cutpoint);


        isApp = true;
        int numberOfActions = 0;
        while ((numberOfActions < MAX_NUMBER_EVENTS()) && (isApp)) {

            mutantTestCase.updateVisitedActivities(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getActivityName());
            mutantTestCase.updateVisitedStates(this.guiModel.getStateById(this.guiModel.getCurrentStateId()));
            updateCoverageArchive(this.guiModel.getCurrentStateId(), mutantTestCase.getId());
            mutantTestCase.updateStatesMap(this.guiModel.getCurrentStateId(), String.valueOf(numberOfActions));

            WidgetAction action;

            if (numberOfActions < cutpoint) {
                MATE.log("Replay Action number: " + numberOfActions);
                action = (WidgetAction) originalTestCase.getEventSequence().get(numberOfActions);
            } else {
                MATE.log("Random Action number: " + numberOfActions);
                //get a list of all executable actions as long as this state is different from last state
                executableActions = selectedScreenState.getActions();
                //select one action randomly
                action = executableActions.get(selectRandomAction(executableActions.size()));
                MATE.log("EVENTO: " + action.getActionType() + "; GUI OBJECT: " + action.getWidget().getClazz());
            }

            try {
                numberOfActions++;
                mutantTestCase.addEvent(action);

                try {
                    //execute this selected action
                    deviceMgr.executeAction(action);

                    //TODO: trying sleep
                    Thread.sleep(2500);

                    IScreenState state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

                    //check whether there is a progress bar on the screen
                    long timeToWait = waitForProgressBar(state);
                    //if there is a progress bar
                    if (timeToWait > 0) {
                        //add 2 sec just to be sure
                        timeToWait += 2000;
                        //set that the current action needs to wait before new action
                        action.setTimeToWait(timeToWait);
                        //get a new state
                        state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                    }

                    //get the package name of the app currently running
                    String currentPackageName = state.getPackageName();
                    //check whether it is an installation package
                    if (currentPackageName != null && currentPackageName.contains("com.android.packageinstaller")) {
                        currentPackageName = handleAuth(deviceMgr, currentPackageName);
                    }
                    //if current package is null, emulator has crashed/closed
                    if (currentPackageName == null) {
                        MATE.log_acc("CURRENT PACKAGE: NULL");
                        return null;
                    }

                    //check whether the package of the app currently running is from the app under test
                    //if it is not, restart app
                    //check also whether the limit number of actions before restart has been reached
                    if (!currentPackageName.equals(this.packageName)) {
                        MATE.log_acc("current package different from app package: " + currentPackageName);

                        isApp = false;
                        deviceMgr.reinstallApp();
                        Thread.sleep(5000);
                        deviceMgr.restartApp();
                        Thread.sleep(2000);
                        state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

                    } else {
                        //if the app under test is running
                        //try to update GUI model with the current screen state
                        //it the current screen is a screen not explored before,
                        //   then a new state is created (newstate = true)
                        state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                        boolean newState = guiModel.updateModel(action, state);
                        mutantTestCase.updateVisitedActivities(state.getActivityName());
                        mutantTestCase.updateVisitedStates(state);
                        updateCoverageArchive(state.getId(), mutantTestCase.getId());



                        //if it is a new state or the initial screen (first action)
                        if (newState || numberOfActions == 1) {
                            MATE.log("New State found:" + state.getId());

                            //runAccessibilityChecks(state, selectedScreenState);

                        }
                    }

                    selectedScreenState = state;

                } catch (AUTCrashException e) {
                    MATE.log_acc("CRASH MESSAGE" + e.getMessage());
                    mutantTestCase.setCrashDetected();
                    crashArchive.add(mutantTestCase);
                    deviceMgr.pressHome();
                    isApp = false;

                    deviceMgr.reinstallApp();
                    Thread.sleep(5000);
                    deviceMgr.restartApp();
                }
            } catch (Exception e) {
                MATE.log("UNKNOWN EXCEPTION");
            }


        }

        return mutantTestCase;
    }

    private void updateCoverageArchive(String state, String tc) {
        if (!coverageArchive.containsKey(state)){
            Set<String> coveredStates = new HashSet<>();
            coveredStates.add(tc);
            coverageArchive.put(state, coveredStates);
        }
        else {
            coverageArchive.get(state).add(tc);
        }
    }

    private int chooseCutpointEpsilonGreedy(TestCase tc) {
        Random rand = new Random();
        int cutpoint = 0;
        if(rand.nextDouble()<=1-GREEDY_EPSILON()){
            MATE.log_acc("choose best cutpoint");
            Map<String, String> statesMap = tc.getStatesMap();
            float bestSparseness = 0;
            String bestCutpoint = null;
            //MATE.log_acc("Possibile bug, tc: "+tc.getId()+", covered states: "+statesMap.keySet());
            for(String state : statesMap.keySet()){
                float sparseness = (float)1/coverageArchive.get(state).size();
                //MATE.log_acc("State: "+state+", covering tc: "+coverageArchive.get(state).size()+", actual best sparseness: "+bestSparseness+", state sparseness:"+sparseness);
                // TODO: The earliest sparse point as cutpoint? (> OR >=) in order to assure more variation, problem initial state
                if(sparseness>=bestSparseness){
                    bestSparseness = sparseness;
                    bestCutpoint = statesMap.get(state);
                    //MATE.log_acc("cutpoint update: "+bestCutpoint);
                }
            }
            cutpoint = Integer.parseInt(bestCutpoint);
        }
        else {
            MATE.log_acc("choose random cutpoint");
            int eventsNumber = tc.getEventSequence().size();
            cutpoint = rand.nextInt(eventsNumber);
        }

        MATE.log_acc("Mutant number: "+tc.getId()+", cut point chosen: "+cutpoint);
        return cutpoint;
    }

}
