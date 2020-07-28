package org.mate.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by marceloe on 12/12/16.
 */

public class WidgetAction extends Action {

    private Widget widget;
    private ActionType actionType;
    private String extraInfo;
    private boolean executed;
    private int executionCount;

    private float fitness;
    private long timeToWait;
    private float pheromone;
    private float proportionalPheromone;


    private List<WidgetAction> adjActions;

    public List<WidgetAction> getAdjActions() {
        return adjActions;
    }

    public WidgetAction(ActionType actionType){
        this.actionType = actionType;
        this.executed = false;
        this.setExecutionCount(0);
        fitness=0;
        pheromone=0;
        proportionalPheromone=0;
        widget = new Widget("","","");
    }

    public WidgetAction(Widget widget, ActionType actionType) {
        setWidget(widget);
        setActionType(actionType);
        setExtraInfo("");
        adjActions = new ArrayList<>();
        setExecuted(false);
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
        this.setExecutionCount(this.getExecutionCount() + 1);
    }



    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void addAdjAction(WidgetAction eventAction){
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
        WidgetAction action = (WidgetAction) o;
        return actionType == action.actionType &&
                Objects.equals(widget.getIdByActivity(), action.widget.getIdByActivity()) &&
                widget.getX1() == action.widget.getX1() &&
                widget.getX2() == action.widget.getX2() &&
                widget.getY1() == action.widget.getY1() &&
                widget.getX2() == action.widget.getY2();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                widget.getIdByActivity(),
                actionType,
                widget.getX1(),
                widget.getX2(),
                widget.getY1(),
                widget.getY2());
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    public boolean isExecuted(){
        return this.executed;
    }
}
