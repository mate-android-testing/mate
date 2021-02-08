package org.mate.interaction.action.ui;

import android.support.annotation.NonNull;

import org.mate.interaction.action.Action;

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

    private long timeToWait;
    private float pheromone;
    private float proportionalPheromone;


    private List<WidgetAction> adjActions;

    public List<WidgetAction> getAdjActions() {
        return adjActions;
    }

    public WidgetAction(ActionType actionType){
        this.actionType = actionType;
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

    /**
     * Compares two widget actions for equality.
     *
     * @param o The object to which we compare.
     * @return Returns {@code true} if both actions are equal,
     *          otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            WidgetAction other = (WidgetAction) o;
            return actionType == other.actionType && widget.equals(other.widget);
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the widget action.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(actionType) + widget.hashCode();
    }

    /**
     * Returns the string representation of a widget action. Do not
     * alter this representation without changing the parsing routine
     * of the analysis framework!
     *
     * @return Returns the string representation of a widget action.
     */
    @NonNull
    @Override
    public String toString() {
        String representation = "widget-based action: " + actionType + " ";

        if (widget.getIdByActivity() != null && !widget.getIdByActivity().isEmpty()) {
            representation += "widget=" + widget.getIdByActivity() + " ";
        }

        if (widget.getResourceID() != null && !widget.getResourceID().isEmpty()) {
            representation += "resource=" + widget.getResourceID() + " ";
        }

        if (widget.getClazz() != null && !widget.getClazz().isEmpty()) {
            representation += "clazz=" + widget.getClazz();
        }

        return representation;
    }
}
