package org.mate.model.fsm.qbe;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.ActionResult;
import org.mate.model.fsm.State;
import org.mate.model.fsm.Transition;

public class QBETransition extends Transition {

    private final ActionResult actionResult;

    public QBETransition(State source, State target, Action action, ActionResult actionResult) {
        super(source, target, action);
        this.actionResult = actionResult;
    }
}
