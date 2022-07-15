package org.mate.state;

import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.commons.interaction.action.ui.MotifAction;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.interaction.action.ui.WidgetAction;

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
    List<UIAction> getActions();
    List<WidgetAction> getWidgetActions();
    List<MotifAction> getMotifActions();
    List<EspressoAction> getEspressoActions();
    String getActivityName();
    String getPackageName();
    ScreenStateType getType();
}
