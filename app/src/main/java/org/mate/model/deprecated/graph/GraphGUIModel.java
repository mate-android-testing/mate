package org.mate.model.deprecated.graph;

import org.mate.MATE;
import org.mate.exceptions.InvalidScreenStateException;
import org.mate.exploration.deprecated.aco.Ant;
import org.mate.state.IScreenState;
import org.mate.interaction.ui.Action;
import org.mate.interaction.ui.Widget;
import org.mate.interaction.ui.WidgetAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marceloeler on 22/06/17.
 */
@Deprecated
public class GraphGUIModel implements IGUIModel {

    private StateGraph stateGraph;

    private ScreenNode currentScreenNode;

    public String getStateId() {
        return stateId;
    }

    String stateId = null;
    ScreenNode newScreenNode = null;
    private EventEdge currentEventEdge;
    public static int stateCount;
    private int nodeCount;

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
        if (screenState==null){
            MATE.log_acc("screenstate is null");
            return ""; }

        for (ScreenNode scNode: stateGraph.getScreenNodes().values()){
            //compares widgest/actions of the screens on the graph
            if (scNode.getScreenState().equals(screenState)){
                return scNode.getId();
            }
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

    public boolean updateModel(WidgetAction event, IScreenState screenState){
        boolean updated = false;

        if (event == null) {
            addRootNode(screenState);
            updated=true;

            MATE.log_vin("New state name: "+stateId);
            MATE.log_vin("Activity name: "+screenState.getActivityName());
            MATE.log_vin("Widgets: " );
            for (Widget w: screenState.getWidgets()) {
                MATE.log_vin(w.getId() + " " + w.getClazz());
            }
        }
        else {
            if (screenState != null) {
                String stateId = getNewNodeName(screenState);
                EventEdge eventEdge = null;
                ScreenNode newScreenNode;
                if (stateId.equals("")) {
                    //new state
                    stateId = "S" + String.valueOf(nodeCount++);

                    MATE.log_vin("New state name: "+stateId);
                    MATE.log_vin("Activity name: "+screenState.getActivityName());
                    MATE.log_vin("Widgets: " );
                    for (Widget w: screenState.getWidgets()){
                        MATE.log_vin(w.getId()+ " " + w.getClazz());
                    }

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


    //VIN
    public boolean updateModelEVO(WidgetAction event, IScreenState screenState){
        boolean updated = false;

        if (event == null) {
            if(this.getStates().size()==0) {
                addRootNode(screenState);
                updated = true;
            }
            //TODO: consider screenstate null
            else {
                String stateId = getNewNodeName(screenState);
                ScreenNode newScreenNode;
                screenState.setId(stateId);
                newScreenNode = stateGraph.getScreenNodes().get(stateId);
                currentScreenNode = newScreenNode;
                updated = false;
            }
        }
        else {
            if (screenState != null) {
                String stateId = getNewNodeName(screenState);
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


    //yan
    public IScreenState updateModelForACO(WidgetAction event, IScreenState screenState, Ant ant){
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

    public List<List<Action>> pathFromTo(String source, String target){
        return stateGraph.pathFromTo(source,target);
    }

    public String getNewNodeName(IScreenState screenState) {
        //return "";
        return findScreenNodeByState(screenState);
    }

    //yan
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

    public List<IScreenState> getStates(){
        List<IScreenState> states = new ArrayList<>();
        for (ScreenNode node: stateGraph.getScreenNodes().values())
            states.add(node.getScreenState());
        return states;
    }

}
