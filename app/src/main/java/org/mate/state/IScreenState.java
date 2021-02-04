package org.mate.state;

import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.interaction.Action;
import org.mate.interaction.ui.Widget;
import org.mate.interaction.ui.WidgetAction;

import java.util.List;
import java.util.Map;

/**
 * Created by marceloeler on 21/06/17.
 */

public interface IScreenState {


    public String getId();
    public void setId(String stateId);
    public List<Widget> getWidgets();
    public List<WidgetAction> getActions();
    public String getActivityName();
    public String getPackageName();
    public String getType();
    public void updatePheromone(Action triggeredAction);
    public Map<Action,Float> getActionsWithPheromone();
    public AccessibilityNodeInfo getRootAccessibilityNodeInfo();
    public boolean differentColor(IScreenState visitedState);
}
