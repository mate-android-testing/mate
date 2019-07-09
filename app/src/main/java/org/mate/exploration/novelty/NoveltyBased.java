package org.mate.exploration.novelty;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exceptions.AUTCrashException;
import org.mate.interaction.DeviceMgr;
import org.mate.model.IGUIModel;
import org.mate.model.TestCase;
import org.mate.model.graph.GraphGUIModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;
import org.mate.utils.MersenneTwister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import static org.mate.MATE.device;
import static org.mate.Properties.EVO_ITERATIONS_NUMBER;
import static org.mate.Properties.GREEDY_EPSILON;
import static org.mate.Properties.MAX_NUMBER_EVENTS;
import static org.mate.Properties.NUMBER_TESTCASES;

public class NoveltyBased {
    private int TCcounter;
    private int maxNumIterations=EVO_ITERATIONS_NUMBER;


    private final int maxNumTCs;
    private final int maxNumEvents;
    public static LinkedHashMap<String, TestCase> testsuite;
    private Vector<TestCase> crashArchive;
    private Vector<TestCase> noveltyArchive;
    public static HashMap<String,Set<String>> coverageArchive;
    private LinkedHashMap<String, TestCase> archive;

    private DeviceMgr deviceMgr;
    private String packageName;
    private Vector<Action> executableActions;
    private GraphGUIModel guiModel;
    public static String currentActivityName;
    private boolean isApp = true;
    private IScreenState selectedScreenState;

    public NoveltyBased(DeviceMgr deviceMgr, String packageName, IGUIModel guiModel){
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        this.guiModel = (GraphGUIModel) guiModel;
        this.currentActivityName="";

        this.maxNumTCs = NUMBER_TESTCASES;
        this.maxNumEvents= MAX_NUMBER_EVENTS;
        testsuite = new LinkedHashMap<>();
        this.TCcounter = 0;

        //TODO: does the crash archive need to take into account also any information of the crash in order to detect different crashes?
        this.crashArchive= new Vector<>();
        NoveltyBased.coverageArchive = new HashMap<>();
        this.archive = new LinkedHashMap<>();
    }


    public void startEvolutionaryExploration(IScreenState selectedScreenState){
        MATE.log_acc("Novelty Android v.4.0\n");
        MATE.log_acc("START Step 1 - Initialization\n");
        //1. Initialize population
        testsuite=initialize(selectedScreenState);
        MATE.log_acc("END Step 1 - Initial Population size: "+testsuite.size());

        for(String tcid:testsuite.keySet()){
            MATE.log_acc("Test Case: "+tcid+", covered GUI states: "+testsuite.get(tcid).getVisitedStates());
        }

        //update information about the states covered by each test case (the feature vector)
        for(String tcid:testsuite.keySet()){
            testsuite.get(tcid).updateFeatureVector(guiModel);
            //MATE.log_acc("TEST CASE "+tcid+" FEATURES: "+testsuite.get(tcid).getFeatureVector());
        }

        //MATE.log_acc("TEST measure distance between TC0 e TC1 = "+measureDistance(testsuite.get("0"), testsuite.get("1")));
        //measureDistanceFromKNNs(testsuite.get("0"));
        //for(String tck:testsuite.keySet()){
        //    calculateSparseness(testsuite.get(tck));
        //    MATE.log_acc("Test Case: "+tck+", sparseness = "+testsuite.get(tck).getSparseness());
        //}

        //2. evolution
        MATE.log_acc("START Step 2 - Evolution\n");
        int iterationsCounter=0;
        while(iterationsCounter<maxNumIterations){
            MATE.log_acc("START Evolutionary Iteration Number: "+iterationsCounter);
            //3. mutation
            //TODO: select candidate with highest sparseness to mutate
            //TODO: evaluate novelty of the candidates


            for(String tcid : testsuite.keySet()){
                TestCase tc = testsuite.get(tcid);
                this.calculateSparseness(tc);
                MATE.log_acc("Sparseness of tc: "+tcid+" = "+tc.getSparseness());
                }


            //String bestCandidateId = selectIndividualToMutate();

            String mutationCandidateId = getMutationCandidate();



            //TestCase currentTC = selectForMutation();
            // for now it mutates always the one with highest sparseness of the first population
            TestCase currentTC = testsuite.get(mutationCandidateId);
            MATE.log_acc("Candidate to mutate is Test Case number: "+mutationCandidateId);

            TestCase mutantTC = null;
            try {
                mutantTC = mutate(currentTC);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MATE.log_acc("Performance at evolution "+iterationsCounter);
            MATE.log_acc("Visited Activities: "+mutantTC.getVisitedActivities().toString());
            MATE.log_acc("Visited States"+mutantTC.getVisitedStates().toString());
            //MATE.log_acc("Best score: "+currentTC.getVisitedStates().size());
            //MATE.log_acc("Mutant score: "+mutantTC.getVisitedStates().size());


            //TODO: improve
            //Recalculate the feature vectors according to the new test cases
            for(String tcid:testsuite.keySet()){
                testsuite.get(tcid).updateFeatureVector(guiModel);
                //MATE.log_acc("TEST CASE "+tcid+" FEATURES: "+testsuite.get(tcid).getFeatureVector());
            }

            for(String tcid:archive.keySet()){
                archive.get(tcid).updateFeatureVector(guiModel);
                //MATE.log_acc("TEST CASE "+tcid+" FEATURES: "+testsuite.get(tcid).getFeatureVector());
            }

            mutantTC.updateFeatureVector(guiModel);



            //TODO: Put in the archive the candidate with the highest sparseness, if it is the first iteration // NO ARCHIVED TESTCASES
            //if(iterationsCounter==0){
            //    this.addToArchive(testsuite.get(bestCandidateId));
            //}


            MATE.log_acc("Added to the population the test case number :"+TCcounter);
            //add the mutant to the population
            testsuite.put(String.valueOf(TCcounter),mutantTC);


            //TODO: Selection operator, should eliminate the worst Test Case of the population
            //4. compare mutant with the population, decides whether to store the mutant
            // FOR NOW It takes the mutant if it visits more GUI states, else it keeps the original test case
            //TODO: one plus one comparison
            /// //if(mutantTC.getVisitedStates().size()>=currentTC.getVisitedStates().size()){
                //testsuite.remove("0");


            //TODO: at each iteration the worst individual is discarded from the population test suite
            //NOTE: when an individual is discarded from population/archive the coverage archive should be updated
            String discardedIndividualId = selectIndividualToDiscard();
            MATE.log_acc("Discarded from the population the test case number: "+discardedIndividualId);
            //TODO: bisogna fare questo controllo dopo update dell'archivio?
            if(!archive.keySet().contains(discardedIndividualId)){
                //TODO: BUGGY
                //removeFromCoverageArchive(discardedIndividualId);
            }
            //To not forget important explorations the ones that satisfy a threshold are kept in the archive
            addToArchive(testsuite.get(discardedIndividualId));
            //The worst member of the archive is then removed
            testsuite.remove(discardedIndividualId);





            //addToArchive(mutantTC);

                //testsuite.remove(0);
                //testsuite.add(0,mutantTC);
            //}
            //TODO: update sparseness
            //TODO: evaluate whether to move to archive (threshold)

            //remove one individual from the test suite


            //After the mutant generation, update the feature vectors of the population and of the archive




            iterationsCounter++;
            TCcounter++;
        }

        //for(TestCase crashedTC : crashArchive){
        //    MATE.log_acc("Crashed TC: "+crashedTC.getVisitedStates().toString());
        //}


        MATE.log_acc("Final crashes: "+crashArchive.size());
        //Collection<TestCase> provatestcases = this.testsuite.values();
        //for(TestCase t : provatestcases){
        //       MATE.log_acc(t.getVisitedActivities().toString());
        //       MATE.log_acc(t.getVisitedStates().toString());
        //}

        for(String k : coverageArchive.keySet()){
            Set<String> provacollection = NoveltyBased.coverageArchive.get(k);
            //for (String s : provacollection){
            MATE.log_acc("State: "+k+" covered by: "+provacollection.toString());
        }


        MATE.log_acc("Archived Test Case number: "+archive.size());
        //for(String k : archive.keySet()){
        //    TestCase archivedTestcases = this.archive.get(k);
            //for (String s : provacollection){
        //}

        //Set<String> provacollection = this.coverageArchive.get("S0");
        //for (String s : provacollection){
            //MATE.log_acc("S0 covered by: "+provacollection.toString());
        //}

        //Vector<TestCase> resultTCsVector = new Vector<>(this.testsuite.values());
        //resultTCsVector.addAll(this.testsuite.values());
        //return resultTCsVector;

        //TODO: last evaluation of the archive is missing

    }



    //create the initial population
    public LinkedHashMap<String, TestCase> initialize(IScreenState selectedScreenState) {

        //TODO: HERE
        this.selectedScreenState=selectedScreenState;
        //long currentTime = new Date().getTime();
        int numberOfTCs = 0;

        while (numberOfTCs < maxNumTCs) {
            this.testsuite.put(String.valueOf(TCcounter),new TestCase(String.valueOf(TCcounter)));
            TestCase testcase = this.testsuite.get(String.valueOf(TCcounter));
            //Vin
            //TODO: heuristic, due to tempification issues with launcher activities, it seems a good solution to add the root state of the GUI model to each test case
            //testcase.updateVisitedStates(this.guiModel.getStateById(this.guiModel.getStateById("S0").getId()));
            //testcase.updateVisitedActivities(this.guiModel.getStateById(this.guiModel.getStateById("S0").getId()).getActivityName());

            //updateCoverageArchive("S0",testcase.getId());



            //Gets the first state
            selectedScreenState = ScreenStateFactory.getScreenState("ActionsScreenState");
            guiModel.updateModelEVO(null,selectedScreenState);
            //IScreenState statedebug = ScreenStateFactory.getScreenState("ActionsScreenState");
            //guiModel.updateModelEVO(null,statedebug);
            //MATE.log_acc("DEBUG: MATE STATE "+guiModel.getNewNodeName(statedebug)+" state updated? ");
            //MATE.log_acc("DEBUG: MATE STATE "+guiModel.getCurrentStateId()+" state updated? ");

                //IScreenState s = ScreenStateFactory.getScreenState("ActionsScreenState");
                //guiModel.updateModel(null,s);
                //MATE.log_acc("DEBUG: INITIAL STATE "+s.getId());

                //testcase.updateVisitedActivities(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getActivityName());
                //testcase.updateVisitedStates(this.guiModel.getStateById(this.guiModel.getCurrentStateId()));


            //FUNZIONA
            //EnvironmentManager.screenShot(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getPackageName(),this.guiModel.getStateId()+"_TC"+numberOfTCs+"_initial");

            //EnvironmentManager.screenShot(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getPackageName(),this.guiModel.getCurrentStateId()+"_TC"+numberOfTCs+"_initial");
            //EnvironmentManager.screenShot((ScreenStateFactory.getScreenState("ActionsScreenState")).getPackageName(),ScreenStateFactory.getScreenState("ActionsScreenState").getId()+"_TC"+numberOfTCs+"_initial_difference");

            int numberOfActions = 0;
            isApp=true;

            while ((numberOfActions < this.maxNumEvents)&&(isApp)) {

                /*MATE.log_acc("Iteration number: "+numberOfActions+
                        ", Starting State: "+this.guiModel.getCurrentStateId()+
                        ", Starting Activity: "+this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getActivityName());

                        */


                //EnvironmentManager.screenShot(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getPackageName(),"_TC"+numberOfTCs+"_EVENT"+numberOfActions+"_before_"+selectedScreenState.getId());
                EnvironmentManager.screenShot(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getPackageName(),this.guiModel.getCurrentStateId()+"_TC"+numberOfTCs+"_EVENT"+numberOfActions+"_before_");

                //TODO: is it necessary?
                testcase.updateVisitedActivities(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getActivityName());
                testcase.updateVisitedStates(this.guiModel.getStateById(this.guiModel.getCurrentStateId()));
                updateCoverageArchive(this.guiModel.getCurrentStateId(),testcase.getId());

                //MATE.log_acc("DEBUG DEBUG: "+this.TCcounter+",event: "+numberOfActions+", state: "+this.guiModel.getCurrentStateId());

                testcase.updateStatesMap(this.guiModel.getCurrentStateId(), String.valueOf(numberOfActions));


                try {
                    //TODO:Verify it
                    //get a list of all executable actions as long as this state is different from last state
                    executableActions = selectedScreenState.getActions();

                    //select one action randomly
                    Action action = executableActions.get(selectRandomAction(executableActions.size()));
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
                        IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

                        //check whether there is a progress bar on the screen
                        long timeToWait = waitForProgressBar(state);
                        //if there is a progress bar
                        if (timeToWait > 0) {
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
                            //if(!currentPackageName.equals(this.packageName)){
                            MATE.log_acc("current package different from app package: "+currentPackageName);
                            //}

                            //MATE.log_acc("Test Case: "+numberOfTCs+"; number of actions: "+numberOfActions);
                            //MATE.log_acc("package name: " + this.packageName);

                            isApp=false;

                            deviceMgr.reinstallApp();
                            Thread.sleep(5000);
                            deviceMgr.restartApp();
                            Thread.sleep(2000);
                            //TODO: get state when restarts the app
                            state = ScreenStateFactory.getScreenState("ActionsScreenState");
                            //numberOfActions = 0;
                            //numberOfTCs++;
                            //if(numberOfTCs < maxNumTCs){
                            //    this.testsuite.add(new TestCase(numberOfTCs));
                            //}

                        } else {
                            //if the app under test is running
                            //try to update GUI model with the current screen state
                            //it the current screen is a screen not explored before,
                            //   then a new state is created (newstate = true)
                            //TODO: is this useless?
                            state = ScreenStateFactory.getScreenState("ActionsScreenState");

                            //testcase.updateVisitedActivities(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getActivityName());
                            //testcase.updateVisitedStates(this.guiModel.getStateById(this.guiModel.getCurrentStateId()));


                            //update model with new state
                            boolean newState = guiModel.updateModel(action, state);
                            testcase.updateVisitedActivities(state.getActivityName());
                            testcase.updateVisitedStates(state);
                            updateCoverageArchive(state.getId(),testcase.getId());

                            //EnvironmentManager.screenShot(state.getPackageName(),"_TC"+numberOfTCs+"_EVENT"+String.valueOf(numberOfActions-1)+"_zafter_"+this.guiModel.getCurrentStateId());
                            EnvironmentManager.screenShot(state.getPackageName(),this.guiModel.getCurrentStateId()+"_TC"+numberOfTCs+"_EVENT"+String.valueOf(numberOfActions-1)+"_zafter");



                            //if it is a new state or the initial screen (first action)
                            //    then check the accessibility properties of the screen
                            if (newState || numberOfActions == 1) {
                                MATE.log("New State found:" + state.getId());

                                //runAccessibilityChecks(state, selectedScreenState);

                            }
                            //testcase.updateVisitedStates(state);

                        }

                        selectedScreenState = state;

                    } catch (AUTCrashException e) {
                        MATE.log_acc("CRASH MESSAGE"+e.getMessage());
                        testcase.setCrashDetected();
                        crashArchive.add(testcase);
                        deviceMgr.handleCrashDialog();
                        isApp=false;

                        deviceMgr.reinstallApp();
                        Thread.sleep(5000);
                        deviceMgr.restartApp();
                    }
                } catch (Exception e) {
                    MATE.log("UNKNOWN EXCEPTION");
                }

                //currentTime = new Date().getTime();
            }

            //this.testsuite.add(new TestCase(numberOfTCs));
            MATE.log_acc("Test Case number: " + numberOfTCs+", number of events: "+numberOfActions);
            numberOfTCs++;
            this.TCcounter++;
            if(numberOfTCs<maxNumTCs){
                try {
                    deviceMgr.reinstallApp();
                    Thread.sleep(5000);
                    deviceMgr.restartApp();
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                selectedScreenState=ScreenStateFactory.getScreenState("ActionsScreenState");
            }
        }

        return this.testsuite;
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

    private TestCase mutate(TestCase originalTestCase) throws InterruptedException {
        //Reinstall the app
        //String emulator = EnvironmentManager.detectEmulator(packageName);
        //Instrumentation instrumentation = getInstrumentation();
        //UiDevice device = UiDevice.getInstance(instrumentation);
        //DeviceMgr deviceMgr = new DeviceMgr(device, packageName);

        MATE.log("REINSTALL APP");
        deviceMgr.reinstallApp();
        Thread.sleep(5000);
        deviceMgr.restartApp();
        Thread.sleep(2000);

        //TestCase originalTestCase = testcase;
        //TestCase mutantTestCase = new TestCase(originalTestCase.getId());
        TestCase mutantTestCase = new TestCase(String.valueOf(this.TCcounter));



        selectedScreenState = ScreenStateFactory.getScreenState("ActionsScreenState");
        guiModel.updateModelEVO(null,selectedScreenState);

        //add initial state to mutant test case
        //mutantTestCase.updateVisitedStates(this.guiModel.getStateById(this.guiModel.getStateById("S0").getId()));
        //mutantTestCase.updateVisitedActivities(this.guiModel.getStateById(this.guiModel.getStateById("S0").getId()).getActivityName());
        //updateCoverageArchive("S0",mutantTestCase.getId());


        //Select Random Cutpoint
        //int eventsNumber = originalTestCase.getEventSequence().size();
        //Random rand = new Random();
        //int cutpoint = rand.nextInt(eventsNumber);
        //if (cutpoint > 0) {
        //    MATE.log("REPLAY");
        //}

        //Select the best cutpoint

        int cutpoint = chooseCutpointEpsilonGreedy(originalTestCase);
        MATE.log_acc("Cut point chosen: " + cutpoint);
        //TODO: replay until cutpoint
        //for (int i = 0; i < cutpoint; i++) {
        //    Action a = originalTestCase.getEventSequence().get(i);
        //    MATE.log_acc("Action number = " + i + "; widget type: " + a.getWidget() + " ; " + a.getActionType());
        //    try {
        //        deviceMgr.executeAction(a);
        //        } catch (AUTCrashException e) {
        //            e.printStackTrace();
        //        }
        //        Thread.sleep(1000);
        //        mutantTestCase.addEvent(a);

        //}




        isApp=true;
        int numberOfActions=0;

        //MATE.log_acc("Current number of TEST CASES: "+numberOfTCs);
        //while (currentTime - runningTime <= MATE.TIME_OUT){
        while ((numberOfActions < MAX_NUMBER_EVENTS)&&(isApp)) {

            //EnvironmentManager.screenShot(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getPackageName(),"_TC"+TCcounter+"_EVENT"+numberOfActions+"_before_"+selectedScreenState.getId());
            EnvironmentManager.screenShot(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getPackageName(),this.guiModel.getCurrentStateId()+"_TC"+TCcounter+"_EVENT"+String.valueOf(numberOfActions)+"_before");


            //MATE.log("Mutation Iteration number: " + numberOfActions +
            //        ", Starting State: " + this.guiModel.getCurrentStateId() +
            //        ", Starting Activity: " + this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getActivityName());


            mutantTestCase.updateVisitedActivities(this.guiModel.getStateById(this.guiModel.getCurrentStateId()).getActivityName());
            mutantTestCase.updateVisitedStates(this.guiModel.getStateById(this.guiModel.getCurrentStateId()));
            updateCoverageArchive(this.guiModel.getCurrentStateId(),mutantTestCase.getId());

            mutantTestCase.updateStatesMap(this.guiModel.getCurrentStateId(), String.valueOf(numberOfActions));

            Action action;

            if (numberOfActions < cutpoint) {
                MATE.log("Replay Action number: "+numberOfActions);
                action = originalTestCase.getEventSequence().get(numberOfActions);
                //TODO: Add an event to the test case before its execution?
            } else {
                MATE.log("Random Action number: "+numberOfActions);
                //get a list of all executable actions as long as this state is different from last state
                executableActions = selectedScreenState.getActions();
                //select one action randomly
                action = executableActions.get(selectRandomAction(executableActions.size()));
                MATE.log("EVENTO: " + action.getActionType() + "; GUI OBJECT: " + action.getWidget().getClazz());
            }


            try {
                numberOfActions++;
                //TODO: Add an event to the test case before its execution?
                mutantTestCase.addEvent(action);

                try {
                    //execute this selected action
                    deviceMgr.executeAction(action);

                    //TODO: trying sleep
                    Thread.sleep(2500);

                    //update number of actions for statistics purpose
                    //numberOfActions++;
                    //TODO: Add an event to the test case after its execution?
                    //mutantTestCase.addEvent(action);

                    //create an object that represents the screen
                    //using type: ActionScreenState
                    IScreenState state = ScreenStateFactory.getScreenState("ActionsScreenState");

                    //check whether there is a progress bar on the screen
                    long timeToWait = waitForProgressBar(state);
                    //if there is a progress bar
                    if (timeToWait > 0) {
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
                    //if (!currentPackageName.equals(this.packageName) || numberOfActions >= MATE.RANDOM_LENGH) {
                    //if (!currentPackageName.equals(this.packageName) || numberOfActions == this.maxNumEvents) {

                    if (!currentPackageName.equals(this.packageName)) {
                        //if(!currentPackageName.equals(this.packageName)){
                        MATE.log_acc("current package different from app package: " + currentPackageName);
                        //}

                        //MATE.log_acc("Test Case: "+numberOfTCs+"; number of actions: "+numberOfActions);
                        //MATE.log_acc("package name: " + this.packageName);

                        isApp = false;
                        deviceMgr.reinstallApp();
                        Thread.sleep(5000);
                        deviceMgr.restartApp();
                        Thread.sleep(2000);
                        state = ScreenStateFactory.getScreenState("ActionsScreenState");
                        //numberOfActions = 0;
                        //numberOfTCs++;
                        //if(numberOfTCs < maxNumTCs){
                        //    this.testsuite.add(new TestCase(numberOfTCs));
                        //}

                    } else {
                        //if the app under test is running
                        //try to update GUI model with the current screen state
                        //it the current screen is a screen not explored before,
                        //   then a new state is created (newstate = true)
                        //TODO: non mi trovo
                        state = ScreenStateFactory.getScreenState("ActionsScreenState");
                        boolean newState = guiModel.updateModel(action, state);
                        mutantTestCase.updateVisitedActivities(state.getActivityName());
                        mutantTestCase.updateVisitedStates(state);
                        updateCoverageArchive(state.getId(),mutantTestCase.getId());

                        //EnvironmentManager.screenShot(state.getPackageName(),"_TC"+TCcounter+"_EVENT"+String.valueOf(numberOfActions-1)+"_zafter_"+this.guiModel.getCurrentStateId());
                        EnvironmentManager.screenShot(state.getPackageName(),this.guiModel.getCurrentStateId()+"_TC"+TCcounter+"_EVENT"+String.valueOf(numberOfActions-1)+"_zafter_"+this.guiModel.getCurrentStateId());


                        //if it is a new state or the initial screen (first action)
                        if (newState || numberOfActions == 1) {
                            MATE.log("New State found:" + state.getId());

                            //runAccessibilityChecks(state, selectedScreenState);

                        }
                        //testcase.updateVisitedStates(state);

                    }

                    selectedScreenState = state;

                } catch (AUTCrashException e) {
                    MATE.log_acc("CRASH MESSAGE" + e.getMessage());
                    mutantTestCase.setCrashDetected();
                    crashArchive.add(mutantTestCase);
                    deviceMgr.handleCrashDialog();
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

    private void removeFromCoverageArchive(String discardedIndividualId) {
        for(String state : coverageArchive.keySet()){
            Set<String> coveringTCs = coverageArchive.get(state);
            coveringTCs.remove(discardedIndividualId);
            if(coveringTCs.size() == 0){
                MATE.log_acc("State removed from Coverage Archive: "+state);
                coverageArchive.remove(state);
            }

        }
    }

    private double measureDistance(TestCase p1, TestCase p2){
        /*
        * Returns the distance between points p1 and p2 which are assumed to be lists or tuples of equal length.
        * To calculate distance, sum up the squared differences between each component of the points.
        * Return the square root of this sum. This method should work for points of any length.
        */

        double totalDistance = 0;
        for(String feature : p1.getFeatureVector().keySet()){
            totalDistance = totalDistance + Math.abs(p1.getFeatureVector().get(feature) - p2.getFeatureVector().get(feature));
            //distance in R2
            //totalDistance = totalDistance + Math.pow(p1.getFeatureVector().get(feature) - p2.getFeatureVector().get(feature),2);
        }
        return totalDistance;

        //Set<String> A = new HashSet<>();
        //A.addAll(p1.getVisitedStates());
        //A.removeAll(p2.getVisitedStates());
        //MATE.log_acc("stati visitati solo da TC "+p1.getId()+": "+A);

        //Set<String> B = new HashSet<>();
        //B.addAll(p2.getVisitedStates());
        //B.removeAll(p1.getVisitedStates());
        //MATE.log_acc("stati visitati solo da TC "+p2.getId()+": "+B);

        //A.addAll(B);
        //MATE.log_acc("stati di distanza: "+A+", distanza: "+A.size());


        //return A.size();


    }

    private double measureDistanceFromKNNs(TestCase p){
        /* TODO
        * Returns the distance of a point p from its k-nearest neighbors in the archive.
        * The simplest, though very inefficient, way to implement this is:
         * to calculate the distance of p from every point in the archive,
         * sort these distances,
         * and then sum up and return the first k (which will be the closest).
         */


        ArrayList<Double> distances = new ArrayList<>();
        double sumDistances = 0;

        //measure distance from the elements of the population
        for (String t: testsuite.keySet()){
            if(!t.equals(p.getId())){
                distances.add(measureDistance(p,testsuite.get(t)));
                //MATE.log_acc("distance between: "+p.getId()+" and "+t+" is = "+measureDistance(p,testsuite.get(t)));
            }

        }

        //measure distance from the elements of the archive
        for (String t : archive.keySet()){
            if(!t.equals(p.getId())){
                distances.add(measureDistance(p,archive.get(t)));
                //MATE.log_acc("distance between: "+p.getId()+" and "+t+" is = "+measureDistance(p,testsuite.get(t)));
            }
        }

        //sort distances in ascending order
        Collections.sort(distances);

        //get the distance from the knn only
        for(int i=0;i<Properties.K_VALUE;i++){
            sumDistances+=distances.get(i);
        }
        //MATE.log_acc("distances of: "+p.getId()+" = "+distances+", sum = "+sumDistances+" distance from KNNs: "+sumDistances/Properties.K_VALUE);

        return sumDistances;
    }

    private void calculateSparseness(TestCase p) {
        //if(archive.isEmpty()){
            //TODO: compare with population only
        //} else {
            //TODO: compare with archive (and population?)
        //}
        /*
        * TODO
        * Returns the sparseness of the given point p as defined by equation 1 on page 13 of the paper.
        * Recall that sparseness is a measure of how unique this point is relative to the archive of saved examples.
        * Use the method distFromkNearest as a helper in calculating this value.
        *
        *  In addition,  if the evolutionary algorithm is steady state (i.e. one individual is replaced at a time)
        *  then the current population can also supplement the archive
        *  by representing the most recently visited points.
         */
        double distance = measureDistanceFromKNNs(p);
        p.setSparseness(distance/Properties.K_VALUE);
    }

    /**
     * If the size of the archive is less than the limit, then always add the point p.
     * Otherwise, when the archive is full, check if the given sparseness of point p is greater than the threshold.
     * If so, add this point and also remove the oldest point in the archive.
     */
    private void addToArchive(TestCase tc) {


        if(archive.size()< Properties.ARCHIVE_SIZE){
            MATE.log_acc("Added to Archive the test case number: "+tc.getId());
            this.archive.put(tc.getId(), tc);
        }
        else{
            String worstArchivedTc = returnWorstArchived();
            //double threshold = calculateSparseness(archive.get(worstArchivedTc));
            //double sparseness = calculateSparseness(tc);
            double threshold = archive.get(worstArchivedTc).getSparseness();
            //TODO: when has to be computed the sparseness of tc?
            double sparseness = tc.getSparseness();
            if(sparseness > threshold){
                //Add the test case to the archive
                MATE.log_acc("Added to Archive the test case number: "+tc.getId());
                this.archive.put(tc.getId(), tc);
                //Remove the worst individual from the archive
                MATE.log_acc("Removed from the Archive the test case number: "+worstArchivedTc);
                archive.remove(worstArchivedTc);
                //if(!testsuite.keySet().contains(worstArchivedTc)){
                    //TODO: buggy and maybe useless
                    //removeFromCoverageArchive(worstArchivedTc);
                //}

            }
            //compare to threshold
            //if greater add to archive
            //then remove one from the archive
            //if(!testsuite.keySet().contains(...)){
             //   removeFromCoverageArchive(...);
            //}
        }

    }

    private String selectIndividualToMutate() {
        //TODO: select the one with best sparseness: rank-based or novelty based?
        double bestSparseness = 0;
        String bestCandidateId = null;
        for(String tcid : testsuite.keySet()){
            TestCase tc = testsuite.get(tcid);
            //TODO: is it good to calculate sparseness now?
            this.calculateSparseness(tc);
            MATE.log_acc("Sparseness of tc: "+tcid+" = "+tc.getSparseness());
            if(tc.getSparseness()>=bestSparseness){
                bestSparseness = tc.getSparseness();
                bestCandidateId = tcid;
            }
        }
        MATE.log_acc("Best Sparsness is = "+bestSparseness+", choose candidate "+bestCandidateId+" with sparseness "+testsuite.get(bestCandidateId).getSparseness());
        return bestCandidateId;
    }

    private String selectIndividualToDiscard() {
        double worstSparseness = 1000000000;
        String worstCandidateId = null;
        for(String tcid : testsuite.keySet()){
            TestCase tc = testsuite.get(tcid);
            //TODO: now the sparseness is not calculated against the new individual
            calculateSparseness(tc);
            double sparseness = tc.getSparseness();
            if(sparseness<worstSparseness){
                worstSparseness = sparseness;
                worstCandidateId = tcid;
            }
        }
        return worstCandidateId;
    }

    private String returnWorstArchived() {
        double worstSparseness = 1000000000;
        String worstCandidateId = null;
        for(String tcid : archive.keySet()){
            TestCase tc = archive.get(tcid);
            //double sparseness = this.calculateSparseness2(tc);
            calculateSparseness(tc);
            double sparseness = tc.getSparseness();
            if(sparseness<worstSparseness){
                worstSparseness = sparseness;
                worstCandidateId = tcid;
            }
        }
        return worstCandidateId;
    }

    private void provaSparseness(){
        for(String i : coverageArchive.keySet()){
            if (coverageArchive.get(i).size()==1){
                MATE.log_acc("ELEMENTO "+i+" COPERTO SOLO DA UN TC: "+coverageArchive.get(i));
            }
        }
    }

    public int getIndex(List<Object> population) {
        Long seed_parameter = Properties.RANDOM_SEED;
        long seed = 0;
        if (seed_parameter != null) {
            seed = seed_parameter;
        } else {
            seed = System.currentTimeMillis();
        }
        //Random random = new Random(seed);

        Random random = new MersenneTwister(seed);

        double r = random.nextDouble();
        double d = Properties.RANK_BIAS
                - Math.sqrt((Properties.RANK_BIAS * Properties.RANK_BIAS)
                - (4.0 * (Properties.RANK_BIAS - 1.0) * r));

        int length = population.size();

        d = d / 2.0 / (Properties.RANK_BIAS - 1.0);

        //this is not needed because population is sorted based on Maximization
        //if(maximize)
        //	d = 1.0 - d; // to do that if we want to have Maximisation


        int index = (int) (length * d);
        //MATE.log_acc("Mutant candidate "+index+" with sparseness "+testsuite.get(index).getSparseness());
        return index;
    }

    private String getMutationCandidate() {
        HashMap<String, Double> ts = new HashMap<>();
        //obtain a map with key = testcase ID, value = testcase novelty
        for(String tc : testsuite.keySet()){
            ts.put(tc, testsuite.get(tc).getSparseness());
        }
        //MATE.log_acc("UNSORTED TESTSUITE: "+ts);
        List sortedTs = new LinkedList(ts.entrySet());
        // Defined Custom Comparator here
        Comparator comp =  new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        };

        //sort the list in descending order
        Collections.sort(sortedTs, Collections.reverseOrder(comp));

        //MATE.log_acc("SORTED TESTSUITE: "+sortedTs);

        //rank select the test case
        int index = getIndex(sortedTs);

        //MATE.log_acc("INDEX: "+index);

        //get the selected test case id
        String tcid = String.valueOf(((Map.Entry) sortedTs.get(index)).getKey());

        //MATE.log_acc("ID: "+tcid);

        // copy the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        //HashMap sortedHashMap = new LinkedHashMap();
        //for (Iterator it = sortedTs.iterator(); it.hasNext();) {
        //    Map.Entry entry = (Map.Entry) it.next();
        //    sortedHashMap.put(entry.getKey(), entry.getValue());
        //}
        //MATE.log_acc("SORTED TESTSUITE: "+sortedHashMap);
        //List<String> prova = new ArrayList<>(sortedHashMap.keySet());
        //MATE.log_acc("SORTED LIST: "+prova);
        //int index = getIndex(prova);
        //MATE.log_acc("INDEX: "+index);
        return tcid;
    }

    private int chooseCutpoint(TestCase tc) {
        HashMap<String, String> statesMap = tc.getStatesMap();
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
        MATE.log_acc("Mutant number: "+tc.getId()+", cut point chosen: "+bestCutpoint);
        return Integer.parseInt(bestCutpoint);
    }

    private int chooseCutpointEpsilonGreedy(TestCase tc) {
        Random rand = new Random();
        int cutpoint = 0;
        if(rand.nextDouble()<=1-GREEDY_EPSILON){
            MATE.log_acc("choose best cutpoint");
            HashMap<String, String> statesMap = tc.getStatesMap();
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
