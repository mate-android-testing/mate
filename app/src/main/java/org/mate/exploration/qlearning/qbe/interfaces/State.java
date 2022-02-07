package org.mate.exploration.qlearning.qbe.interfaces;

import java.util.Set;

public interface State<A extends Action> {

    Set<A> getActions();
}
