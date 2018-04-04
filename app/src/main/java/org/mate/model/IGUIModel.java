package org.mate.model;

import org.mate.exceptions.InvalidScreenStateException;
import org.mate.ui.Action;
import org.mate.state.IScreenState;

import java.util.Vector;

/**
 * Created by marceloeler on 22/06/17.
 */

public interface IGUIModel {

    public void moveToState(IScreenState screenState) throws InvalidScreenStateException;
    public boolean updateModel(Action event, IScreenState screenState);
    public String getCurrentStateId();
    public IScreenState getStateById(String id);
    public Vector<Vector<Action>> pathFromTo(String source, String target);
    public Vector<IScreenState> getStates();

}
