package org.mate.model.fsm.qbe;

import org.mate.interaction.action.Action;
import org.mate.model.fsm.State;
import org.mate.state.IScreenState;
import org.mate.utils.ListUtils;

import java.util.Set;

public class QBEState extends State {

    QBEState(int id, IScreenState screenState) {
        super(id, screenState);
    }

    Set<? extends Action> getActions() {
        // TODO: Can QBE handle all types of actions or only widget-based/ui actions?
        return ListUtils.toSet(screenState.getActions());
    }
}
