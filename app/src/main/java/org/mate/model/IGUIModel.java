package org.mate.model;

import org.mate.exceptions.InvalidScreenStateException;
import org.mate.interaction.ui.Action;
import org.mate.state.IScreenState;
import org.mate.interaction.ui.WidgetAction;

import java.util.List;

/**
 * Created by marceloeler on 22/06/17.
 */

public interface IGUIModel {

    public void moveToState(IScreenState screenState) throws InvalidScreenStateException;
    public boolean updateModel(WidgetAction event, IScreenState screenState);
    public String getCurrentStateId();
    public IScreenState getStateById(String id);
    public List<List<Action>> pathFromTo(String source, String target);
    public List<IScreenState> getStates();

}
