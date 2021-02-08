package org.mate.state;

import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;

import java.util.List;
import java.util.Map;

/**
 * Created by marceloeler on 21/06/17.
 */
public interface IScreenState {

    String getId();
    void setId(String stateId);
    List<Widget> getWidgets();
    List<WidgetAction> getActions();
    String getActivityName();
    String getPackageName();
    String getType();
    void updatePheromone(Action triggeredAction);
    Map<Action,Float> getActionsWithPheromone();
    AccessibilityNodeInfo getRootAccessibilityNodeInfo();
    boolean differentColor(IScreenState visitedState);
}
