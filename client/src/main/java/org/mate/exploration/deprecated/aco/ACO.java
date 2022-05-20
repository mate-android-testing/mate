package org.mate.exploration.deprecated.aco;

import org.mate.Properties;
import org.mate.commons.exceptions.AUTCrashException;
import org.mate.exploration.deprecated.Fitness.ActivityCoverage;
import org.mate.interaction.DeviceMgr;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.WidgetAction;
import org.mate.model.deprecated.graph.EventEdge;
import org.mate.model.deprecated.graph.GraphGUIModel;
import org.mate.model.deprecated.graph.ScreenNode;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.state.ScreenStateType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by marceloeler on 21/06/17.
 */

@Deprecated
public class ACO {

    private DeviceMgr deviceMgr;
    private String packageName;
    private GraphGUIModel wholeModel;
    private List<String> statesVisited;
    private int indexOfBestAction = 0;
    private int currentGeneration = 0;
    private List<Ant> allAntsInOneGeneration;
    private boolean isFirstNodeOfCompleteModel = true;
    private IScreenState screenState = null;
    private String currentNodeId = null;
    private boolean isFirstExplored = false;
    private ActivityCoverage activityCoverage;
    private boolean isExitApp = false;
    private WidgetAction exitAction = null;

    public ACO(DeviceMgr deviceMgr,String packageName,GraphGUIModel completeModel){
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        this.wholeModel = completeModel;
        statesVisited = new ArrayList<>();
        activityCoverage = new ActivityCoverage();
    }

    public void startExploreACO(){

        for (int g = 0; g< Properties.ANT_GENERATION(); g++){
            currentGeneration = g;
            IScreenState state = null;
            //create a model for all ants in one generation
            allAntsInOneGeneration = new ArrayList<>();
            for (int n = 0;n<Properties.ANT_NUMBER();n++){
                Ant ant = new Ant();
                deviceMgr.restartApp();
                //only design for the first node of this whole model
                    for (int l = 0;l<Properties.ANT_LENGTH();l++){
                        state = handleFirstNodeOfAnt(state, ant, l);
                        wholeEvolveProcess(state, ant,isExitApp);
                    }
                //store all ant objects
                allAntsInOneGeneration.add(ant);
            }
            //select best ants
            selectBestAnts(allAntsInOneGeneration,new Ant());
            //update pheromone
            searchEventEdgeInAllAnts();
        }
    }

    public void wholeEvolveProcess(IScreenState state, Ant ant,boolean isExitApp) {
        if (isFirstNodeOfCompleteModel){
            screenState = evolveFirstAnt(state,ant);
            isFirstNodeOfCompleteModel = false;
        } else {
            String stateId = null;
            if (!isExitApp){
                stateId = wholeModel.getStateId();
            }else {
                //if this app is restarted due to exiting from this app,
                //we need to "scan" this state and get this launching activity.
                state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                stateId = wholeModel.getNewNodeName(state);
                if (stateId.equals("")){
                    //if this app is restarted and the launching state is new
                    screenState = wholeModel.updateModelForACO(exitAction,state,ant);
                    //update fitness for each ant fitness of an ant doesn't include repetitive activity
                    activityCoverage.updateAntFitness(state, ant);
                    System.out.println("--launching a state--");
                }else {
                    //screenState needs to be replaced with latest state before
                    //the second ant start its travelling
                    ScreenNode node = wholeModel.getStateGraph().getScreenNodes().get(stateId);
                    screenState = node.getScreenState();
                }
                this.isExitApp = false;
            }
            evolve(screenState, ant, stateId,isFirstExplored);
        }
    }

    public IScreenState handleFirstNodeOfAnt(IScreenState state, Ant ant, int l) {
        //if it is the first event of an ant
        if (l==0){
            state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
            screenState = state;
            if (!isFirstNodeOfCompleteModel){
                ant.getCoveredActivity().add(state.getActivityName());
                if (wholeModel.getNewNodeNameForACO(state,true)){
                    isFirstExplored = true;
                }else {
                    isFirstExplored = false;
                }
            }
        }
        return state;
    }

    public void evolve(IScreenState state,Ant ant,String currentStateId,boolean isFirstExplored) {

        WidgetAction action = null;
            // We will select the action randomly if this explored node is new or
            // this is the first generation
            if (isFirstExplored|| currentGeneration == 0){
                List<WidgetAction> executableActions = state.getWidgetActions();
                //random select
                int randNum = selectRandomAction(state.getActions().size());
                action = executableActions.get(randNum);
                System.out.println(currentGeneration+" Ant random--"+action+" Pheromone "+action.getPheromone());
                try {
                    deviceMgr.executeAction(action);
                } catch (AUTCrashException e) {
                    e.printStackTrace();
                }
            }else {
                //find the node from history
                IScreenState historyScreenState = wholeModel.getStateById(currentStateId);
                List<WidgetAction> executableActions = historyScreenState.getWidgetActions();

                //select the best action at this state
                action = selectBestWidget(executableActions);
                if (Math.random()<=Properties.PROBABILITY_SELECT_BEST_ACTION()){
                    try {
                        //fill all forms first
//                        if (action.getWidget().isEditable()){
//                            action = fillFrom(action, executableActions);
//                        }
                        deviceMgr.executeAction(action);
                        System.out.println(currentGeneration+" Ant bestAction--"+action+" Pheromone "+action.getPheromone());
                    } catch (AUTCrashException e) {
                        e.printStackTrace();
                    }
                }else {
                    List<WidgetAction> copyOfExecutableActions = null;
                    copyOfExecutableActions = new ArrayList<>(executableActions);
                    //if only one action is available, we won't delete any action
                    if (executableActions.size()>1){
                        //we don't want to remove this best action from real screen state,
                        //so a state is cloned from this real state, and remove this best action from it.
                        copyOfExecutableActions.remove(indexOfBestAction);
                    }
                    try {
                        //pseudo random proportional selection
                        action = proportionalSelection(copyOfExecutableActions);
                        //fill all forms first
//                        if (action.getWidget().isEditable()){
//                            action = fillFrom(action, executableActions);
//                        }
                        deviceMgr.executeAction(action);
                        System.out.println(currentGeneration+" Ant Proportion--"+action+" Pheromone "+action.getPheromone());
                    } catch (AUTCrashException e) {
                        e.printStackTrace();
                    }
                }
            }
            //renew a state, thus all of data about this state will be clean.
            state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
            String newPackageName = state.getPackageName();
        //TODO:it's a unknown error about newPackageName is null
        if (newPackageName!=null){
            if (newPackageName.equals(this.packageName)) {
                //If this state has been stored in history, we retrieve.By doing this,
                //the pheromone value will be remained.
                state = wholeModel.updateModelForACO(action,state,ant);
                currentStateId = wholeModel.getCurrentStateId();
                if (!statesVisited.contains(currentStateId)){
                    statesVisited.add(currentStateId);
                    this.isFirstExplored = true;
                }else {
                    this.isFirstExplored = false;
                }
                //update fitness for each ant fitness of an ant doesn't include repetitive activity
                activityCoverage.updateAntFitness(state, ant);
                screenState = state;
                this.currentNodeId = wholeModel.getCurrentStateId();
            } else {
                //restarts ACO if the exploration left the app
                restartACO(action);
            }
        }else {
            restartACO(action);
        }
    }

    public void restartACO(WidgetAction action) {
        deviceMgr.restartApp();
        exitAction = action;
        System.out.println("restart on ACO");
        isExitApp = true;
    }

    public Action fillFrom(WidgetAction action, List<WidgetAction> executableActions) throws AUTCrashException {
        int eacount = this.numberOfEditableWidgets(executableActions);
        int count = 0;
        //let the last action out because it is executed after this block of instructions
        //the last action also keeps information on the other type text actions that needs to be performed together
        while (count<eacount-1){
            this.deviceMgr.executeAction(action);
            action = executableActions.get(++count);
        }
        return action;
    }


    public IScreenState evolveFirstAnt(IScreenState state, Ant ant) {
        wholeModel.updateModel(null,state);
        ant.getCoveredActivity().add(state.getActivityName());
        currentNodeId = wholeModel.getCurrentStateId();
        statesVisited.add(currentNodeId);
        return state;
    }

    public int selectRandomAction(int executionActionSize){
        Random rand = new Random();
        return rand.nextInt(executionActionSize);
    }

    public WidgetAction selectBestWidget(List<WidgetAction> executableActions){
        float tempPheromone = 0;
        float bestPheromone = 0;
        WidgetAction bestAction = null;
        for (WidgetAction action:executableActions){
            tempPheromone = action.getPheromone();
            if (tempPheromone >= bestPheromone){
                bestAction = action;
                bestPheromone = tempPheromone;
                indexOfBestAction = executableActions.indexOf(action);
            }
        }
        return bestAction;
    }

    public WidgetAction proportionalSelection(List<WidgetAction> executableActions){
        float sumPheromone = 0;
        for (WidgetAction action:executableActions){
            sumPheromone+=action.getPheromone();
        }

        for (WidgetAction action:executableActions){
            action.setProportionalPheromone(action.getPheromone()/sumPheromone);
        }
        float sum1 = 0.f;
        double f = Math.random();

        for (WidgetAction action:executableActions){
            sum1 += action.getProportionalPheromone();
            if (sum1>=f){
                return action;
            }
        }
        return null;
    }

    public void searchEventEdgeInAllAnts(){
        //all of the pheromone value of EventEdges should be decrease firstly
        for (Map.Entry<String,EventEdge> entry:wholeModel.getStateGraph().getEventEdges().entrySet()){
            entry.getValue().setPheromone((entry.getValue().getPheromone())*(1 - Properties.EVAPORATION_RATE()));
        }

        //iterate each best ants
        for (int indexOfCurrentAnt = 0;indexOfCurrentAnt<allAntsInOneGeneration.size();indexOfCurrentAnt++){
            Ant ant = allAntsInOneGeneration.get(indexOfCurrentAnt);
            //get a set of EventEdge from current ant
            List<EventEdge> BenefitForFitnessEventEdge = ant.getBenefitForFitnessEventEdge();

            for (int i = 0;i<BenefitForFitnessEventEdge.size();i++){
                //store one type of eventEdge in one ant
                List<EventEdge> oneTypeEdgeInAllAnts = new ArrayList<>();

                EventEdge currentComparedEventEdge = BenefitForFitnessEventEdge.get(i);
                oneTypeEdgeInAllAnts.add(currentComparedEventEdge);
                //compare with the next eventEdge
                for (int j = i+1;j<BenefitForFitnessEventEdge.size();j++){
                    EventEdge followEventEdgeOfCurrentCompared = BenefitForFitnessEventEdge.get(j);
                    if (followEventEdgeOfCurrentCompared.equals(currentComparedEventEdge)){
                        oneTypeEdgeInAllAnts.add(BenefitForFitnessEventEdge.get(j));
                        BenefitForFitnessEventEdge.remove(followEventEdgeOfCurrentCompared);
                    }
                }
                //now,oneTypeEdgeInAllAnts stores all same EventEdges that have the
                //same id in one ant, but their fitness may be not same.
                int numOfAnts = searchEventEdgeInOtherAnts(allAntsInOneGeneration,oneTypeEdgeInAllAnts,currentComparedEventEdge,indexOfCurrentAnt);
                updatePheromone(oneTypeEdgeInAllAnts,numOfAnts,currentComparedEventEdge);
            }
        }
    }

    public void selectBestAnts(List<Ant> allAntsInOneGeneration,Ant ant){
        Collections.sort(allAntsInOneGeneration,ant.compareAnt());
        List<Ant> copyAllAntOneGeneration = new ArrayList<>(allAntsInOneGeneration);
        for (int i = 0;i<copyAllAntOneGeneration.size()-Properties.BEST_ANT();i++){
            this.allAntsInOneGeneration.remove(allAntsInOneGeneration.size()-1);
        }
    }

    public int searchEventEdgeInOtherAnts(List<Ant> allAntsInOneGeneration,List<EventEdge> oneTypeEdgeInAllAnts,EventEdge currentComparedEventEdge,int indexOfCurrentAnt){
        int numberOfAnt = 0;
        //search next ant
        for (int i = indexOfCurrentAnt+1;i<allAntsInOneGeneration.size();i++){
            //preventing concurrency exception
            List<EventEdge> removedEventEdges = new ArrayList<>();
            Ant ant = allAntsInOneGeneration.get(i);
            boolean isFound = true;
            //iterate all eventEdges of ant
            for (EventEdge eventEdge:ant.getBenefitForFitnessEventEdge()){
                if (eventEdge.equals(currentComparedEventEdge)){
                    oneTypeEdgeInAllAnts.add(eventEdge);
                    removedEventEdges.add(eventEdge);
                    if (isFound){
                        numberOfAnt+=1;
                        isFound = false;
                    }
                }
            }
            ant.getBenefitForFitnessEventEdge().removeAll(removedEventEdges);
        }
        //plus this ant itself
        numberOfAnt+=1;
        return numberOfAnt;
    }

    public void updatePheromone(List<EventEdge> oneTypeEdgeInAllAnts,int numOfAnts,EventEdge currentComparedEventEdge){
        float accumulatedFitnessOfEventEdge = 0.f;
        for (EventEdge eventEdge:oneTypeEdgeInAllAnts){
            accumulatedFitnessOfEventEdge += eventEdge.getFitness();
        }
        float actualPheromone = accumulatedFitnessOfEventEdge/numOfAnts;
        ScreenNode source = currentComparedEventEdge.getSource();
        ScreenNode target = currentComparedEventEdge.getTarget();
        //search corresponding eventEdge in completeModel
        EventEdge updatedEventEdge = wholeModel.getStateGraph().getEdge(source,target);
        updatedEventEdge.setPheromone(updatedEventEdge.getPheromone()+actualPheromone);
    }
    int numberOfEditableWidgets(List<WidgetAction> actions){
        int count=0;
        for (WidgetAction action: actions)
            if (action.getWidget().isEditable())
                count++;
        return count;
    }

}
