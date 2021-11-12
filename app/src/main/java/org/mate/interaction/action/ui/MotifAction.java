package org.mate.interaction.action.ui;

import android.support.annotation.NonNull;

import org.mate.Registry;
import org.mate.utils.Randomness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a motif gene, i.e. a combination of multiple widget or primitive actions, e.g.
 * filling a form and pressing the submit button. Consider the
 * {@link org.mate.exploration.genetic.algorithm.Sapienz} implementation for more details.
 */
public class MotifAction extends UIAction {

    // TODO: Consider if we need to use the more generic ui actions to represent a motif gene.

    /**
     * The list of widget actions that represent the motif gene. Only available when the respective
     * {@link org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory} considers a
     * {@link org.mate.state.IScreenState} for retrieving the actions, like in the traditional
     * widget based exploration.
     */
    private final List<WidgetAction> widgetActions;

    /**
     * Constructs a new motif action as used in the
     * {@link org.mate.exploration.genetic.algorithm.Sapienz} implementation, i.e. the individual
     * alleles represent actions that are not directly backed by a widget.
     *
     * @param actionType   The type of action, e.g. FILL_FORM_AND_SUBMIT.
     * @param activityName The name of the activity on which the action should be applied.
     */
    public MotifAction(ActionType actionType, String activityName) {
       this(actionType, activityName, null);
    }

    /**
     * Constructs a new motif action that is backed by a list of widget actions.
     *
     * @param actionType   The type of action, e.g. FILL_FORM_AND_SUBMIT.
     * @param activityName The name of the activity on which the action should be applied.
     * @param widgetActions The list of widget actions that represent the motif gene.
     */
    public MotifAction(ActionType actionType, String activityName, List<WidgetAction> widgetActions) {
        super(actionType, activityName);
        this.widgetActions = widgetActions;
    }

    /**
     * Returns the widget actions that back the motif gene.
     *
     * @return Returns the widget actions that back the motif action.
     */
    public List<WidgetAction> getWidgetActions() {
        if (widgetActions == null) {
            throw new IllegalStateException("The motif action is not backed by any widget actions!");
        } else {
            return Collections.unmodifiableList(widgetActions);
        }
    }

    /**
     * Generates a random motif action. The motif gene is not backed by any widgets and should be
     * only used by the {@link org.mate.exploration.genetic.algorithm.Sapienz} implementation.
     *
     * @return Returns a randomly generated motif action.
     */
    public static MotifAction randomAction() {
        String activity = Registry.getCurrentActivity();
        return new MotifAction(Randomness.randomElement(Arrays.asList(ActionType.motifActionTypes)), activity);
    }

    /**
     * Compares two motif actions for equality.
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
            MotifAction other = (MotifAction) o;
            return actionType == other.actionType && Objects.equals(widgetActions, other.widgetActions);
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the motif action.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(actionType) + Objects.hashCode(widgetActions);
    }

    /**
     * The string representation used in combination with analysis framework.
     *
     * @return Returns the string representation of a primitive action.
     */
    @NonNull
    @Override
    public String toString() {
        return "motif action: " + actionType;
    }

    /**
     * A simplified textual representation used for the {@link org.mate.model.IGUIModel}.
     *
     * @return Returns a simplified string representation.
     */
    @NonNull
    @Override
    public String toShortString() {
        return String.valueOf(actionType);
    }
}
