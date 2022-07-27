package org.mate.utils.testcase.espresso.actions;

import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;

import java.util.List;

import static org.mate.utils.testcase.espresso.EspressoDependency.CONTAINS_STRING;
import static org.mate.utils.testcase.espresso.EspressoDependency.ON_DATA;

/**
 * Converts a {@link MotifAction} into an espresso action.
 */
public class MotifActionConverter extends ActionConverter {

    /**
     * The action type of the {@link MotifAction}.
     */
    private final ActionType actionType;

    /**
     * The list of individual {@link UIAction}s that compose the {@link MotifAction}.
     */
    private final List<UIAction> actions;

    /**
     * Constructs a converter for the given {@link MotifAction}.
     *
     * @param action The motif action that should be converted.
     */
    public MotifActionConverter(MotifAction action) {
        super(action);
        this.actionType = action.getActionType();
        this.actions = action.getUIActions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convert() {

        builder.append("// BEGIN MOTIF ACTION").append(System.lineSeparator());

        for (UIAction action : actions) {
            // every motif action consists of a list of widget actions
            WidgetAction widgetAction = (WidgetAction) action;
            WidgetActionConverter converter = new WidgetActionConverter(widgetAction);
            builder.append(converter.convert());
            builder.append(System.lineSeparator());
        }

        if (actionType == ActionType.SPINNER_SCROLLING) {
            // the spinner scrolling motif action contains just a single widget action
            WidgetAction widgetAction = (WidgetAction) actions.get(0);
            builder.append(new SpinnerScrollingConverter(widgetAction).convert());
        }

        builder.append("// END MOTIF ACTION");
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildPerform() {
        throw new UnsupportedOperationException("This method should never be called!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildViewMatcher() {
        throw new UnsupportedOperationException("This method should never be called!");
    }

    /**
     * An converter for the {@link ActionType#SPINNER_SCROLLING} action.
     */
    private static class SpinnerScrollingConverter extends WidgetActionConverter {

        /**
         * The children of the spinner widget.
         */
        private final List<Widget> children;

        /**
         * Constructs a converter for the given action.
         *
         * @param action The action that should be converted.
         */
        public SpinnerScrollingConverter(WidgetAction action) {
            super(action);
            this.children = action.getWidget().getChildren();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String convert() {

            // TODO: The spinner motif action only clicks on the first children (the next element).
            for (Widget child : children) {
                openViewMatcher();
                buildTextMatcher(child.getText());
                closeViewMatcher();
                buildPerform();
                buildComment();
                builder.append(System.lineSeparator());
            }

            return builder.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void openViewMatcher() {
            builder.append(ON_DATA).append("(");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void buildViewMatcher() {
            throw new UnsupportedOperationException("This method should never be called!");
        }

        /**
         * Builds a view matcher using 'containsString()' for the given string.
         *
         * @param text The text attribute of the view matcher.
         */
        private void buildTextMatcher(String text) {
            builder.append(CONTAINS_STRING).append("(\"").append(text).append("\")");
        }
    }
}
