package org.mate.model.fsm.qbe;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;

/**
 * Defines a transition in the {@link ELTS}.
 */
public final class QBETransition extends Transition {

    /**
     * The action result associated with the execution of the action.
     */
    private final ActionResult actionResult;

    /**
     * Creates a new transition from a given state to a target state with a given action. Each
     * transition is additionally associated with the action result.
     *
     * @param source The source state.
     * @param target The target state.
     * @param action The action which leads to the target state.
     * @param actionResult The result of the action.
     */
    public QBETransition(State source, State target, Action action, ActionResult actionResult) {
        super(source, target, action);
        this.actionResult = actionResult;
    }

    /**
     * Returns the action result associated with the action leading from the source to the target
     * state.
     *
     * @return Returns the action result.
     */
    ActionResult getActionResult() {
        return actionResult;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final QBETransition that = (QBETransition) o;
        return actionResult == that.actionResult;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + actionResult.hashCode();
        return result;
    }

    /**
     * Provides a custom string representation for the transition.
     *
     * @return Returns the string representation of the given transition.
     */
    @Override
    public String toString() {
        return "{\"from\":" + getSource() + ",\"trigger\":" + getAction() + ",\"to\":" + getTarget()
                + ",\"actionResult\":\"" + actionResult + "\"}";
    }
}
