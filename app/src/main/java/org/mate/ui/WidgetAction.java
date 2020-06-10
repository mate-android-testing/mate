package org.mate.ui;

import org.mate.state.IScreenState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by marceloe on 12/12/16.
 * Updated on May 2020
 */

public class WidgetAction extends Action {

    private Widget widget;
    private ActionType actionType;
    private String extraInfo;
    private boolean executed;
    private Integer weight;
    private float fitness;
    private long timeToWait;
    private float pheromone;
    private float proportionalPheromone;
    private int qtdeOfExec;
    private boolean newStateGenerated;
    private IScreenState adjScreen;
    //private List<IScreenState> listAdjScreen;
    private List<WidgetAction> adjActions;


    public List<WidgetAction> getAdjActions() {
        return adjActions;
    }

    public WidgetAction(ActionType actionType){
        this.actionType = actionType;
        fitness=0;
        widget = new Widget("","","");
        this.weight = 0;
    }

    public WidgetAction(Widget widget, ActionType actionType) {
        setWidget(widget);
        setActionType(actionType);
        setExtraInfo("");
        adjActions = new ArrayList<>();
        setExecuted(false);
        this.weight = 0;
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

    public boolean isExecuted() {
        return executed;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public float getFitness() {
        return fitness;
    }

    public int getQtdeOfExec() {
        return qtdeOfExec;
    }

    public void setQtdeOfExec(int qtdeOfExec) {
        this.qtdeOfExec = qtdeOfExec;
    }

    public void plusQtdeOfExec() {
        this.qtdeOfExec++;
    }

    public boolean isNewStateGenerated() {
        return newStateGenerated;
    }

    public void setNewStateGenerated(boolean newStateGenerated) {
        this.newStateGenerated = newStateGenerated;
    }

    public void increaseWeight(int weight) {
        this.weight = this.weight + weight;
    }

    public void decreaseWeight(int weight) {
        this.weight = this.weight - weight;
    }

    public void increaseFitness(float fitness) {
        this.fitness = this.fitness + fitness;
    }

    public void decreaseFitness(float fitness) {
        this.fitness = this.fitness - fitness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WidgetAction action = (WidgetAction) o;
        return actionType == action.actionType &&
                Objects.equals(widget.getIdByActivity(), action.widget.getIdByActivity());
    }

    public IScreenState getAdjScreen() {
        return adjScreen;
    }

    public void setAdjScreen(IScreenState adjScreen) {
        this.adjScreen = adjScreen;
    }

    @Override
    public int hashCode() {

        return Objects.hash(widget.getIdByActivity(), actionType);
    }
}
