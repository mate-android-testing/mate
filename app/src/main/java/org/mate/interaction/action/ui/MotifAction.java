package org.mate.interaction.action.ui;

import android.support.annotation.NonNull;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.utils.Randomness;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a motif gene, i.e. a combination of multiple widget or primitive actions, e.g.
 * filling a form and pressing the submit button. Consider the
 * {@link org.mate.exploration.genetic.algorithm.Sapienz} implementation for more details.
 */
public class MotifAction extends UIAction {

    /**
     * Records which menu items have been selected so far per menu. The widget (key) refers
     * to the menu, not the list view containing the menu items.
     */
    private static final Map<Widget, Set<String>> selectedMenuItems = new HashMap<>();

    /**
     * The list of ui actions that represent the motif gene. Depending on whether we use widget-based
     * or primitive actions, the list contains those type of actions.
     */
    private List<UIAction> uiActions;

    /**
     * Constructs a new motif action as used in the
     * {@link org.mate.exploration.genetic.algorithm.Sapienz} implementation, i.e. the individual
     * alleles represent actions that are not directly backed by a widget.
     *
     * @param actionType   The type of action, e.g. FILL_FORM_AND_SUBMIT.
     * @param activityName The name of the activity on which the action should be applied.
     */
    public MotifAction(ActionType actionType, String activityName) {
       this(actionType, activityName, Collections.emptyList());
    }

    /**
     * Constructs a new motif action that is backed by a list of ui actions.
     *
     * @param actionType   The type of action, e.g. FILL_FORM_AND_SUBMIT.
     * @param activityName The name of the activity on which the action should be applied.
     * @param uiActions The list of ui actions that represent the motif gene.
     */
    public MotifAction(ActionType actionType, String activityName, List<UIAction> uiActions) {
        super(actionType, activityName);
        this.uiActions = uiActions;
    }

    /**
     * Returns for the given menu which menu items have been selected so far.
     *
     * @param widget The widget identifying the menu.
     * @return Returns the selected (already used) menu items for the given menu.
     */
    public Set<String> getSelectedMenuItems(final Widget widget) {
        return selectedMenuItems.getOrDefault(widget, new HashSet<>());
    }

    /**
     * Adds for the given menu a new menu item that has been selected.
     *
     * @param widget The widget identifying the menu.
     * @param menuItem The new menu item that has been selected.
     */
    public void addSelectedMenuItem(final Widget widget, final String menuItem) {
        Set<String> menuItems = getSelectedMenuItems(widget);
        menuItems.add(menuItem);
        selectedMenuItems.put(widget, menuItems);
    }

    /**
     * Saves the ui actions. Should be only used when {@link Properties#USE_PRIMITIVE_ACTIONS()} is
     * turned on, i.e. when we use primitive actions. It is necessary to save the actions in order
     * to replay them if desired.
     *
     * @param uiActions The list of ui actions that represent the motif gene.
     */
    public void setUiActions(List<UIAction> uiActions) {
        this.uiActions = uiActions;
    }

    /**
     * Returns the ui actions that back the motif gene.
     *
     * @return Returns the ui actions that back the motif action.
     */
    public List<UIAction> getUIActions() {
            return Collections.unmodifiableList(uiActions);
    }

    /**
     * Generates a random motif action. The motif gene is not backed by any widgets and should be
     * only used by the {@link org.mate.exploration.genetic.algorithm.Sapienz} implementation.
     *
     * @return Returns a randomly generated motif action.
     */
    public static MotifAction randomAction() {
        String activity = Registry.getUiAbstractionLayer().getCurrentActivity();
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
            return actionType == other.actionType && Objects.equals(uiActions, other.uiActions);
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the motif action.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(actionType) + Objects.hashCode(uiActions);
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
