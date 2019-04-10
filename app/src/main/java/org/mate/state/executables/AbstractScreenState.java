package org.mate.state.executables;

import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by marceloeler on 21/06/17.
 */

public abstract class AbstractScreenState implements IScreenState {

    protected String activityName;
    protected String packageName;
    protected Vector<Widget> widgets;

    public AbstractScreenState(String packageName, String activityName){
        widgets = new Vector<Widget>();
        this.packageName = packageName;
        this.activityName = activityName;
    }

    public Vector<Widget> getWidgets(){
        return widgets;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void addWidget(Widget widget){
        widgets.add(widget);
    }

    public Widget getWidget(String id){
        for (Widget widget: widgets)
            if (widget.getId().equals(id))
                return widget;
        return null;
    }

    public Hashtable<String, Widget> getEditableWidgets(){
        Hashtable<String, Widget> editables = new Hashtable<String, Widget>();
        for (Widget widget: widgets){
            if (widget.isEditable())
                editables.put(widget.getId(),widget);
        }
        return editables;
    }

    public Hashtable<String, Widget> getCheckableWidgets(){
        Hashtable<String, Widget> checkables = new Hashtable<String, Widget>();
        for (Widget widget: widgets){
            if (widget.isCheckable()||widget.isChecked())
                checkables.put(widget.getId(),widget);
        }
        return checkables;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
