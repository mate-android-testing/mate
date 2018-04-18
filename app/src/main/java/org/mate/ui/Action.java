package org.mate.ui;

import java.util.Vector;

/**
 * Created by marceloe on 12/12/16.
 */

public class Action {

    private Widget widget;
    private int actionType;
    private String extraInfo;
    private boolean executed;

    private float fitness;
    private long timeToWait;
    private float pheromone;
    private float proportionalPheromone;


    private Vector<Action> adjActions;

    public Vector<Action> getAdjActions() {
        return adjActions;
    }

    public Action(int actionType){
        this.actionType = actionType;
        fitness=0;
        widget = new Widget("","","");
    }

    public Action(Widget widget, int actionType) {
        setWidget(widget);
        setActionType(actionType);
        setExtraInfo("");
        adjActions = new Vector<Action>();
        setExecuted(false);
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }
    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void addAdjAction(Action eventAction){
        adjActions.add(eventAction);
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
    }

    public long getTimeToWait() {
        return timeToWait;
    }

    public void setTimeToWait(long timeToWait) {
        this.timeToWait = timeToWait;
    }
    public float getPheromone() {
        return pheromone;
    }

    public void setPheromone(float pheromone) {
        this.pheromone = pheromone;
    }
    public float getProportionalPheromone() {
        return proportionalPheromone;
    }

    public void setProportionalPheromone(float proportionalPheromone) {
        this.proportionalPheromone = proportionalPheromone;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Action action = (Action) o;

        if (actionType != action.actionType) return false;
        if (widget != null ? !widget.equals(action.widget) : action.widget != null) return false;
        return true;
    }
}
