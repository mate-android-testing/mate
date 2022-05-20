package org.mate.model.deprecated.graph;

import org.mate.commons.exceptions.InvalidScreenStateException;
import org.mate.commons.interaction.action.Action;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.WidgetAction;

import java.util.List;

/**
 * Created by marceloeler on 22/06/17.
 */
@Deprecated
public interface IGUIModel {

    void moveToState(IScreenState screenState) throws InvalidScreenStateException;
    boolean updateModel(WidgetAction event, IScreenState screenState);
    String getCurrentStateId();
    IScreenState getStateById(String id);
    List<List<Action>> pathFromTo(String source, String target);
    List<IScreenState> getStates();

}
