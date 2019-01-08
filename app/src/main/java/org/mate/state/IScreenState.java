package org.mate.state;

import android.view.accessibility.AccessibilityNodeInfo;

import org.mate.ui.Action;
import org.mate.ui.Widget;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by marceloeler on 21/06/17.
 */

public interface IScreenState {


    public String getId();
    public void setId(String stateId);
    //Todo: Ugly: don't define equals like that in the interface. Fix.
    public boolean equals(IScreenState state);
    public Vector<Widget> getWidgets();
    public Vector<Action> getActions();
    public String getActivityName();
    public String getPackageName();
    public String getType();
    public void updatePheromone(Action triggeredAction);
    public HashMap<Action,Float> getActionsWithPheromone();
    public AccessibilityNodeInfo getRootAccessibilityNodeInfo();
}
