package org.mate.model.deprecated.graph;

import org.mate.Properties;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.WidgetAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marceloe on 08/12/16.
 */
@Deprecated
public class ScreenNode {

    private String id;
    private IScreenState screenState;
    private List<EventEdge> eventEdges;

    public void initPheromone(){
        //TODO: how about selectState.getactions
        List<WidgetAction> executableActions = screenState.getWidgetActions();
        for (WidgetAction action:executableActions){
            action.setPheromone(Properties.INITIALIZATION_PHEROMONE());
        }
    }

    public ScreenNode(String id, IScreenState screenState){
        setId(id);
        setScreenState(screenState);
        eventEdges = new ArrayList<>();
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


    public String getPackageName() {
        return screenState.getPackageName();
    }


    public String getActivityName() {
        return screenState.getActivityName();
    }

}
