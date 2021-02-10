package org.mate.state;

import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;

import java.util.List;

/**
 * Defines the interface for a screen state,
 * see {@link org.mate.state.executables.AbstractScreenState} and
 * {@link org.mate.state.executables.ActionsScreenState}, respectively.
 */
public interface IScreenState {

    String getId();
    void setId(String stateId);
    List<Widget> getWidgets();
    List<WidgetAction> getActions();
    String getActivityName();
    String getPackageName();
    ScreenStateType getType();
    Widget getWidget(String id);
    boolean differentColor(IScreenState visitedState);
}
