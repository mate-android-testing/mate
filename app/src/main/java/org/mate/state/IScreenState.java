package org.mate.state;

import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.ui.Action;
import org.mate.ui.Widget;

import java.util.List;
import java.util.Map;

/**
 * Created by marceloeler on 21/06/17.
 */

public interface IScreenState {


    public String getId();
    public void setId(String stateId);
    public List<Widget> getWidgets();
    public List<Action> getActions();
    public String getActivityName();
    public String getPackageName();
    public String getType();
    public void updatePheromone(Action triggeredAction);
    public Map<Action,Float> getActionsWithPheromone();
    public AccessibilityNodeInfo getRootAccessibilityNodeInfo();
}
