package org.mate.model;

import org.mate.interaction.action.Action;
import org.mate.state.IScreenState;

public interface IGUIModel {

    void update(IScreenState source, IScreenState target, Action action);
    // boolean containsState(IScreenState screenState);
    // boolean moveToState(IScreenState screenState);
    // boolean moveToActivity(String activity);
    // boolean moveToMainActivity();
    String toString();
    // void draw();
    // int getNumberOfStates();
}
