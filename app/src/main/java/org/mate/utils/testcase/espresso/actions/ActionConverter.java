package org.mate.utils.testcase.espresso.actions;

import org.mate.interaction.action.Action;

import static org.mate.utils.testcase.espresso.EspressoDependency.ON_VIEW;

public abstract class ActionConverter {

    /**
     * The action that should be converted.
     */
    protected final Action action;

    protected final StringBuilder builder = new StringBuilder();

    public ActionConverter(Action action) {
        this.action = action;
    }

    public String convert() {
        openViewMatcher();
        buildViewMatcher();
        closeViewMatcher();
        buildPerform();
        buildComment();
        return builder.toString();
    }

    protected void buildComment() {
        builder.append("// ");
    }

    protected abstract void buildPerform();

    protected void closeViewMatcher() {
        builder.append(")");
    }

    protected abstract void buildViewMatcher();

    protected void openViewMatcher() {
        builder.append(ON_VIEW).append("(");
    }

}
