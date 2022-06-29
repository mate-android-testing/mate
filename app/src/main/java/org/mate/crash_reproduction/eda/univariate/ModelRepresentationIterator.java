package org.mate.crash_reproduction.eda.univariate;

import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;

import java.util.Map;

public interface ModelRepresentationIterator {
    Map<Action, Double> getActionProbabilities();

    IScreenState getState();

    void updatePosition(TestCase testCase, Action action, IScreenState currentScreenState);

    void updatePositionImmutable(IScreenState currentScreenState);
}
