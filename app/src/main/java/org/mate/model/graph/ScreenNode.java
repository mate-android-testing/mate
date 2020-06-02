package org.mate.model.graph;

import org.mate.Properties;
import org.mate.state.IScreenState;
import org.mate.ui.WidgetAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marceloe on 08/12/16.
 */
public class ScreenNode {

    private String id;
    private IScreenState screenState;
    private List<EventEdge> eventEdges;

    public void initPheromone(){
        //TODO: how about selectState.getactions
        List<WidgetAction> executableActions = screenState.getActions();
        for (WidgetAction action:executableActions){
            action.setPheromone(Properties.INITIALIZATION_PHEROMONE());
        }
    }

    public ScreenNode(IScreenState screenState) {
        setId(screenState.getId());
        setScreenState(screenState);
        this.addAllEdge(screenState.getActions());
    }

    public ScreenNode(String id, IScreenState screenState){
        setId(id);
        setScreenState(screenState);
        this.addAllEdge(screenState.getActions());
        //init pheromone when creating a new node
        initPheromone();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IScreenState getScreenState() {
        return screenState;
    }

    public void setScreenState(IScreenState screenState) {
        this.screenState = screenState;
    }

    public List<EventEdge> getEventEdges() {
        return eventEdges;
    }


    public void addEdge(EventEdge eventEdge){
        eventEdges.add(eventEdge);
    }

    public List<ScreenNode> getNeighbors(){
        List<ScreenNode> neighbors = new ArrayList<>();
        for (EventEdge edge: eventEdges){
            neighbors.add(edge.getTarget());
        }
        return neighbors;
    }

    public void addAllEdge(List<WidgetAction> executableActions){
        if(eventEdges == null){
            eventEdges = new ArrayList<>();
        }
        for(WidgetAction action : executableActions){
            this.addEdge(new EventEdge(this, action));
        }
    }


    public String getPackageName() {
        return screenState.getPackageName();
    }


    public String getActivityName() {
        return screenState.getActivityName();
    }

}
