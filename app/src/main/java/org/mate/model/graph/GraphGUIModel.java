package org.mate.model.graph;

import org.mate.MATE;
import org.mate.exceptions.InvalidScreenStateException;
import org.mate.exploration.aco.Ant;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;

import java.io.File;
import java.util.Date;
import java.util.Vector;

/**
 * Created by marceloeler on 22/06/17.
 */

public class GraphGUIModel implements IGUIModel {

    private StateGraph stateGraph;

    private ScreenNode currentScreenNode;

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    String stateId = null;
    ScreenNode newScreenNode = null;
    private EventEdge currentEventEdge;
    public static int stateCount;
    private int nodeCount;
    //need to know the number of node for complete model and partial model
//    private int nodeCount = 0;
    private boolean isFirstNode = false;
    public GraphGUIModel(){
        stateGraph = new StateGraph();
        stateCount = 0;
        nodeCount = 0;
    }

    public void addRootNode(IScreenState screenState){
        stateId = "S"+String.valueOf(nodeCount);
        screenState.setId(stateId);
        ScreenNode rnd = new ScreenNode(stateId,screenState);
        stateGraph.setRootNode(rnd);
        stateGraph.addScreenNode(rnd);
        currentScreenNode = rnd;
        nodeCount +=1;
    }

    public String getCurrentStateId() {
        if (currentScreenNode==null)
            return "";
        return
                currentScreenNode.getId();
    }

    public IScreenState getStateById(String id){
        ScreenNode node = stateGraph.getScreenNodes().get(id);
        if (node!=null)
            return node.getScreenState();
        else
            return null;
    }

    private String findScreenNodeByState(IScreenState screenState){
        if (screenState==null)
            return "";
        for (ScreenNode scNode: stateGraph.getScreenNodes().values()){
            //MATE.log("comparing with " + scNode.getId());
            if (scNode.getScreenState().equals(screenState)){
                return scNode.getId();
            }

            //activity granularity
            //if (scNode.getScreenState().getActivityName().equals(screenState.getActivityName()))
            //    return scNode.getId();
        }
        return "";
    }

    public void moveToState(IScreenState screenState) throws InvalidScreenStateException {
        String stid = findScreenNodeByState(screenState);
        ScreenNode screenNode = stateGraph.getScreenNodes().get(stid);
        if(screenNode!=null){
            currentScreenNode = screenNode;
        }
        else{
            //System.out.println("Throw execption - There is no such state in the state model");
            stateId = "S" + String.valueOf(nodeCount++);
            screenState.setId(stateId);
            newScreenNode = new ScreenNode(stateId, screenState);
            stateGraph.addScreenNode(newScreenNode);
            currentScreenNode = newScreenNode;
            throw new InvalidScreenStateException("There is no such state in the state model");
        }
    }

    public boolean updateModel(Action event, IScreenState screenState){
        boolean updated = false;

        if (event == null) {
            addRootNode(screenState);
            updated=true;
        }
        else {
            if (screenState != null) {
                String stateId = getNewNodeName(screenState);
                MATE.log("STATEID found: " + stateId);
                EventEdge eventEdge = null;
                ScreenNode newScreenNode;
                if (stateId.equals("")) {
                    //new state
                    stateId = "S" + String.valueOf(nodeCount++);
                    screenState.setId(stateId);
                    newScreenNode = new ScreenNode(stateId, screenState);
                    stateGraph.addScreenNode(newScreenNode);
                    updated=true;
                    eventEdge = new EventEdge(currentScreenNode, newScreenNode, event);
                    stateGraph.addEventEdge(eventEdge);
                } else {
                    //System.out.println("Existent state");
                    screenState.setId(stateId);
                    newScreenNode = stateGraph.getScreenNodes().get(stateId);
                    eventEdge = stateGraph.getEdge(currentScreenNode, newScreenNode);
                    if (eventEdge == null) {
                        eventEdge = new EventEdge(currentScreenNode,newScreenNode,event);
                        stateGraph.addEventEdge(eventEdge);
                    }
                }
                currentScreenNode = newScreenNode;
            } else {
                //outside the scope of the application
                String stateid = "OUTAPP";
            }
        }
        return updated;
    }


    public IScreenState updateModelForACO(Action event, IScreenState screenState, Ant ant){
        IScreenState historyScreenState = null;
        if (event == null)
            addRootNode(screenState);
        else {
            if (screenState != null) {

                EventEdge eventEdge = null;
                boolean isNewNode = getNewNodeNameForACO(screenState,false);
                if (isNewNode) {
                    stateGraph.addScreenNode(newScreenNode);
                    historyScreenState = newScreenNode.getScreenState();
                    eventEdge = new EventEdge(currentScreenNode, newScreenNode, event);
                    stateGraph.addEventEdge(eventEdge);
                } else {
                    //System.out.println("Existent state");
                    newScreenNode = stateGraph.getScreenNodes().get(stateId);
                    //get the pheromone from history
                    historyScreenState = newScreenNode.getScreenState();
                    eventEdge = stateGraph.getEdge(currentScreenNode, newScreenNode);
                    if (eventEdge == null) {
                        eventEdge = new EventEdge(currentScreenNode,newScreenNode,event);
                        stateGraph.addEventEdge(eventEdge);
                    }
                }
                //the pheromone of an eventEdge is the value of action that is included in this EventEdge
                eventEdge.setPheromone(event.getPheromone());
                try {
                    ant.setCurrentEventEdge((EventEdge) eventEdge.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                currentEventEdge = eventEdge;
                currentScreenNode = newScreenNode;
            } else {
                //outside the scope of the application
                String stateid = "OUTAPP";
            }
        }
        return historyScreenState;
    }

    public Vector<Vector<Action>> pathFromTo(String source, String target){
        return stateGraph.pathFromTo(source,target);
    }

    public ScreenNode getCurrentScreenNode() {
        return currentScreenNode;
    }

    public void setCurrentScreenNode(ScreenNode currentScreenNode) {
        this.currentScreenNode = currentScreenNode;
    }

    public String getNewNodeName(IScreenState screenState) {
        //return "";
        return findScreenNodeByState(screenState);
    }

    public boolean getNewNodeNameForACO(IScreenState screenState,boolean isFirstNodeOfAnt) {
        stateId = findScreenNodeByState(screenState);
        if (stateId.equals("")){
            this.stateId = "S" + String.valueOf(nodeCount);
            nodeCount+=1;
            this.newScreenNode = new ScreenNode(stateId, screenState);
            return true;
        }else {
            //replace previous remained currentScreenNode of last ant with
            //the current real state
            if (isFirstNodeOfAnt){
                this.currentScreenNode = stateGraph.getScreenNodes().get(stateId);
            }
            return false;
        }
    }


    public StateGraph getStateGraph() {
        return stateGraph;
    }

    public void setStateGraph(StateGraph stateGraph) {
        this.stateGraph = stateGraph;
    }

//    public void updateTransitionFitness(String transitionID, float fitness){
//        stateGraph.updateEdgeFitness(transitionID,fitness);
//    }
//
//    public float getTransitionFitness(String transitionID){
//        return stateGraph.getEdgeFitness(transitionID);
//    }
public EventEdge getCurrentEventEdge() {
    return currentEventEdge;
}

    public void setCurrentEventEdge(EventEdge currentEventEdge) {
        this.currentEventEdge = currentEventEdge;
    }

    public ScreenNode getNewScreenNode() {
        return newScreenNode;
    }

    public void setNewScreenNode(ScreenNode newScreenNode) {
        this.newScreenNode = newScreenNode;
    }
    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public Vector<IScreenState> getStates(){
        Vector<IScreenState> states = new Vector<IScreenState>();
        for (ScreenNode node: stateGraph.getScreenNodes().values())
            states.add(node.getScreenState());
        return states;
    }

}
