package org.mate.interaction.action.ui;

import android.support.annotation.NonNull;

import org.mate.interaction.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates an action together with a certain widget.
 */
public class WidgetAction extends Action {

    /**
     * The widget on which the action should be applied.
     */
    private Widget widget;

    /**
     * The type of action that should be applied on the given widget, e.g. 'CLICK'.
     */
    private ActionType actionType;

    /**
     * The list of adjacent widget actions.
     */
    @Deprecated
    private List<WidgetAction> adjActions;

    /**
     * The text that should be inserted in the underlying widget.
     * Only applicable for editable widgets.
     */
    private String extraInfo;

    /**
     * The time to wait after the action has been executed.
     */
    private long timeToWait;

    @Deprecated
    private float pheromone;

    @Deprecated
    private float proportionalPheromone;

    /**
     * Constructs a new widget action for a dummy widget, i.e. the
     * given action is not related to any widget. For instance, pressing
     * 'BACK' is not linked to a particular widget.
     *
     * @param actionType The kind of action, e.g. 'CLICK'.
     */
    public WidgetAction(ActionType actionType) {
        this.actionType = actionType;
        widget = new Widget("","","");
    }

    /**
     * Links a widget to a certain action, e.g. a click on a button.
     *
     * @param widget The widget on which the action should be applied.
     * @param actionType The kind of action, e.g. 'CLICK'.
     */
    public WidgetAction(Widget widget, ActionType actionType) {
        setWidget(widget);
        setActionType(actionType);
        setExtraInfo("");
        adjActions = new ArrayList<>();
    }

    /**
     * Gets the widget related to the widget action.
     *
     * @return Returns the widget.
     */
    public Widget getWidget() {
        return widget;
    }

    /**
     * Sets the widget.
     *
     * @param widget The new widget.
     */
    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    /**
     * Returns the action type, e.g. 'CLICK'.
     *
     * @return Returns the action type.
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Sets the action type of the widget action.
     *
     * @param actionType The new action type.
     */
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    /**
     * Returns the text input for the widget.
     *
     * @return Returns the text input of the widget.
     */
    public String getExtraInfo() {
        return extraInfo;
    }

    /**
     * Sets the text input for the widget. Typically,
     * only applicable for editable widgets.
     *
     * @param extraInfo A text input for the widget.
     */
    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    /**
     * Adds a new adjacent widget action.
     *
     * @param eventAction A new adjacent widget action.
     */
    @Deprecated
    public void addAdjAction(WidgetAction eventAction) {
        adjActions.add(eventAction);
    }

    /**
     * Returns the list of adjacent widget actions.
     *
     * @return Returns the list of adjacent widget actions.
     */
    @Deprecated
    public List<WidgetAction> getAdjActions() {
        return adjActions;
    }

    /**
     * Returns the wait time, i.e. the time to wait after
     * the execution of the action.
     *
     * @return Returns the wait time.
     */
    public long getTimeToWait() {
        return timeToWait;
    }

    /**
     * Defines a wait time, i.e. the time how long we should wait
     * after executing the action.
     *
     * @param timeToWait The new wait time.
     */
    public void setTimeToWait(long timeToWait) {
        this.timeToWait = timeToWait;
    }

    @Deprecated
    public float getPheromone() {
        return pheromone;
    }

    @Deprecated
    public void setPheromone(float pheromone) {
        this.pheromone = pheromone;
    }

    @Deprecated
    public float getProportionalPheromone() {
        return proportionalPheromone;
    }

    @Deprecated
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

    @NonNull
    @Override
    public String toShortString() {
        if (!widget.getId().isEmpty()) {
            return actionType + "(" + widget.getId() + ")";
        } else {
            return String.valueOf(actionType);
        }
    }
}
