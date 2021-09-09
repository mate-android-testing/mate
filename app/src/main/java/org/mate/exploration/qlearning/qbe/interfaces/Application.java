package org.mate.exploration.qlearning.qbe.interfaces;

import java.util.Optional;

public interface Application<S extends State<A>, A extends Action> {

  S getInitialState();

  Optional<S> executeAction(A action);

  void reset();

  S copyWithDummyComponent(S conflictingState);
}
