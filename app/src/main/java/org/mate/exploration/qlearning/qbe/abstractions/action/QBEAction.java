package org.mate.exploration.qlearning.qbe.abstractions.action;

import org.mate.interaction.action.ui.UIAction;

import java.util.Objects;

/**
 * A wrapper around a {@link UIAction}.
 */
public final class QBEAction implements Action {

    /**
     * The ui action that is wrapped.
     */
    private final UIAction uiAction;

    /**
     * Initialises a new QBEAction by wrapping a {@link UIAction}.
     *
     * @param uiAction The ui action to be wrapped.
     */
    public QBEAction(final UIAction uiAction) {
        this.uiAction = Objects.requireNonNull(uiAction);
    }

    /**
     * Returns the wrapped ui action.
     *
     * @return Returns the ui action.
     */
    public UIAction getUiAction() {
        return uiAction;
    }

    /**
     * Defines a custom string representation for a {@link QBEAction}. Note that this format
     * is reflected in the serialized transition system, see
     * {@link org.mate.exploration.qlearning.qbe.transition_system.TransitionSystemSerializer}.
     *
     * @return Returns the string representation of the action.
     */
    @Override
    public String toString() {
        return "{\"uiAction\":{\"actionType\":\"" + uiAction.getActionType()
                + "\",\"activityName\":\"" + uiAction.getActivityName()
                + "\",\"hash\":" + uiAction.hashCode() + "}}";
    }

    /**
     * Compares two {@link QBEAction}s for equality.
     *
     * @param o The other {@link QBEAction}.
     * @return Returns {@code true} if both actions are equal, i.e. when the underlying ui actions
     *          are equal, otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final QBEAction action = (QBEAction) o;
            return uiAction.equals(action.uiAction);
        }
    }

    /**
     * Retrieves the hash code for the {@link QBEAction}.
     *
     * @return Returns the associated hash code.
     */
    @Override
    public int hashCode() {
        return uiAction.hashCode();
    }
}
