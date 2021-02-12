package org.mate.model.fsm;

import org.mate.interaction.action.Action;
import org.mate.model.IGUIModel;
import org.mate.state.IScreenState;

public class FSMModel implements IGUIModel {

    private final FSM fsm;

    public FSMModel(IScreenState rootState) {
        fsm = new FSM(new State(0, rootState));
    }

    @Override
    public void update(IScreenState source, IScreenState target, Action action) {
        State sourceState = fsm.getState(source);
        State targetState = fsm.getState(target);
        fsm.addTransition(sourceState, targetState, action);
    }

    @Override
    public String toString() {
        return fsm.toString();
    }

}
