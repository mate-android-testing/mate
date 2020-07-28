package org.mate.ui;

import android.app.ActivityManager;

public class ActionMeasure {

    private int newStateCount;
    private int newWidgetCount;

    public ActionMeasure(){
        this.newStateCount = 0;
        this.newWidgetCount = 0;

    }

    public int getNewStateCount() {
        return newStateCount;
    }

    public void setNewStateCount(int newStateCount) {
        this.newStateCount = newStateCount;
    }

    public int getNewWidgetCount() {
        return newWidgetCount;
    }

    public void setNewWidgetCount(int newWidgetCount) {
        this.newWidgetCount = newWidgetCount;
    }
}
