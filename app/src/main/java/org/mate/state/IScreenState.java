package org.mate.state;

import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.state.executables.RelatedState;
import org.mate.ui.Action;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

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
    public String getSessionId();
    public String getScreenTitle();
    public void updatePheromone(Action triggeredAction);
    public Map<Action,Float> getActionsWithPheromone();
    public AccessibilityNodeInfo getRootAccessibilityNodeInfo();
    public boolean differentColor(IScreenState visitedState);
    public void addRelatedState(IScreenState state, String difference);
    public List<RelatedState> getRelatedStates();
}
