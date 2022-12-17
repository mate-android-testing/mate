package org.mate.utils.testcase.espresso.actions;

import org.mate.interaction.action.Action;

import static org.mate.utils.testcase.espresso.EspressoDependency.ON_VIEW;

/**
 * Provides an abstract converter for an {@link Action}.
 */
public abstract class ActionConverter {

    /**
     * The action that should be converted.
     */
    protected final Action action;

    /**
     * The internal builder containing the final espresso action sequence.
     */
    protected final StringBuilder builder = new StringBuilder();

    /**
     * Constructs a converter for the given action.
     *
     * @param action The action that should be converted.
     */
    public ActionConverter(Action action) {
        this.action = action;
    }

    /**
     * Performs the conversion process.
     *
     * @return Returns the obtained espresso action.
     */
    public String convert() {
        openViewMatcher();
        buildViewMatcher();
        closeViewMatcher();
        buildPerform();
        buildComment();
        return builder.toString();
    }

    /**
     * Builds a comment.
     */
    protected void buildComment() {
        builder.append(" // ");
    }

    /**
     * Builds the view action.
     */
    protected abstract void buildPerform();

    /**
     * Closes the view matcher.
     */
    protected void closeViewMatcher() {
        builder.append(")");
    }

    /**
     * Builds the view matcher.
     */
    protected abstract void buildViewMatcher();

    /**
     * Opens the view matcher.
     */
    protected void openViewMatcher() {
        builder.append(ON_VIEW).append("(");
    }

}
