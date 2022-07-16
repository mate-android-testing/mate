package org.mate.model.fsm.qbe;

import org.mate.model.fsm.State;
import org.mate.state.IScreenState;

public class QBEState extends State {

    QBEState(int id, IScreenState screenState) {
        super(id, screenState);
    }
}
