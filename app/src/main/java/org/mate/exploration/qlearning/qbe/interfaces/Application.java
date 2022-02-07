package org.mate.exploration.qlearning.qbe.interfaces;

import org.mate.utils.Pair;

import java.util.Optional;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;

public interface Application<S extends State<A>, A extends Action> {

    S getCurrentState();

    Pair<Optional<S>, ActionResult> executeAction(A action);

    void reset();

    S copyWithDummyComponent(S conflictingState);
}
