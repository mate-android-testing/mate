package org.mate.exploration.qlearning.qbe.exploration;

import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.State;

import java.util.Optional;

@FunctionalInterface
public interface ExplorationStrategy<S extends State<A>, A extends Action> {

  Optional<A> chooseAction(S currentState);
}
