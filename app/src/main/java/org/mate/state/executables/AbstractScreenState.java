package org.mate.state.executables;

import org.mate.state.IScreenState;
import org.mate.interaction.action.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Models an abstract screen state. That is nothing more than a screen with its widgets,
 * the activity and package name it is associated with.
 */
public abstract class AbstractScreenState implements IScreenState {

    /**
     * The activity name to which the screen state refers.
     */
    protected final String activityName;

    /**
     * The package name to which the screen state refers.
     */
    protected final String packageName;

    /**
     * The list of widgets that are associated with the screen state.
     */
    protected final List<Widget> widgets;

    /**
     * The state id.
     */
    protected String id;

    /**
     * Creates a new screen state representing the given activity and package name.
     *
     * @param packageName The package name that corresponds to the screen state.
     * @param activityName The activity name that corresponds to the screen state.
     * @param widgets The list of widgets part of the screen state.
     */
    public AbstractScreenState(String packageName, String activityName, List<Widget> widgets){
        this.widgets = widgets;
        this.packageName = packageName;
        this.activityName = activityName;
    }

    /**
     * Returns the state id.
     *
     * @return Returns the state id.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the state id of the given state.
     *
     * @param id The new state id.
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the list of widgets that are linked to the screen state.
     *
     * @return Returns the associated widgets of the screen state.
     */
    @Override
    public List<Widget> getWidgets(){
        return Collections.unmodifiableList(widgets);
    }

    /**
     * Returns the activity name that is linked to the screen state.
     *
     * @return Returns the activity name.
     */
    @Override
    public String getActivityName() {
        return activityName;
    }

    /**
     * Returns the package name that is linked to the screen state.
     *
     * @return Returns the package name.
     */
    @Override
    public String getPackageName() {
        return packageName;
    }

    /**
     * Returns the widget with the given id.
     *
     * @param id The id of the widget.
     * @return Returns the widget with the given id,
     *          or {@code null} is returned if no such widget exists.
     */
    @Override
    @SuppressWarnings("unused")
    public Widget getWidget(String id){
        for (Widget widget: widgets)
            if (widget.getId().equals(id))
                return widget;
        return null;
    }
}
