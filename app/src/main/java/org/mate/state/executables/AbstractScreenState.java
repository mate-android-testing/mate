package org.mate.state.executables;

import org.mate.Properties;
import org.mate.interaction.action.ui.Widget;
import org.mate.state.IScreenState;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
     * Compares two abstract screen states for equality.
     *
     * @param o The other screen state to compare against.
     * @return Returns {@code true} if both screen states are equal,
     *          otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            AbstractScreenState other = (AbstractScreenState) o;

            switch (Properties.STATE_EQUIVALENCE_LEVEL()) {
                case PACKAGE_NAME:
                    return Objects.equals(this.packageName, other.packageName);
                case ACTIVITY_NAME:
                    return Objects.equals(this.packageName, other.packageName)
                            && Objects.equals(this.activityName, other.activityName);
                case WIDGET:
                case WIDGET_WITH_ATTRIBUTES:
                    return Objects.equals(activityName, other.activityName) &&
                            Objects.equals(packageName, other.packageName) &&
                            Objects.equals(widgets, other.widgets);
                default:
                    throw new UnsupportedOperationException("State equivalence level "
                        + Properties.STATE_EQUIVALENCE_LEVEL() + " not yet supported!");
            }
        }
    }

    private static double cosineSimilarity(AbstractScreenState first, AbstractScreenState second) {
        return 0.0d;
    }

    /**
     * Computes a hash code for the abstract screen state.
     *
     * @return Returns the hash code associated with this screen state.
     */
    @Override
    public int hashCode() {
        switch (Properties.STATE_EQUIVALENCE_LEVEL()) {
            case PACKAGE_NAME:
                return Objects.hash(packageName);
            case ACTIVITY_NAME:
                return Objects.hash(packageName, activityName);
            case WIDGET:
            case WIDGET_WITH_ATTRIBUTES:
                return Objects.hash(activityName, packageName, widgets);
            default:
                throw new UnsupportedOperationException("State equivalence level "
                        + Properties.STATE_EQUIVALENCE_LEVEL() + " not yet supported!");
        }
    }

    /**
     * Provides a simple textual representation of a screen state of the form: id [activity].
     *
     * @return Returns the string representation of the screen state.
     */
    @Override
    public String toString() {
        return id + " [" + activityName + "]";
    }
}
