package org.mate.utils.testcase.espresso.actions;

import android.widget.TextView;

import org.mate.Registry;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;

import static org.mate.utils.testcase.espresso.EspressoDependency.ALL_OF;
import static org.mate.utils.testcase.espresso.EspressoDependency.WITH_TEXT;

public class WidgetActionConverter extends ActionConverter {

    private final Widget widget;
    private final ActionType actionType;
    private final int numberOfUsableViewMatchers;

    public WidgetActionConverter(WidgetAction action) {
        super(action);
        this.widget = action.getWidget();
        this.actionType = action.getActionType();
        this.numberOfUsableViewMatchers = getNumberOfUsableViewMatchers();
    }

    private String getShortResourceID() {

        // the resource id might look as follows: android:id/statusBarBackground
        if (!widget.getResourceID().startsWith(Registry.getPackageName())) {
            return null;
        }

        // or it might look as follows: com.zola.bmi:id/resultLabel
        String[] tokens = widget.getResourceID().split("/");
        if (tokens.length > 1) {
            return tokens[1];
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildComment() {
        super.buildComment();
        builder.append(actionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildPerform() {
    }

    @Override
    protected void buildViewMatcher() {

        final String resourceID = getShortResourceID();

        // the resource id is not necessarily present for all widgets
        if (resourceID != null) {
            buildWithIDString(resourceID);
        } else {

            // TODO: There is no view matcher for a ImageViewer right now.
            if (isNonEmptyTextView()) {
                // try first a view matcher solely based on the widget text
                buildOneOf();
            }

            // use an 'allOf()' view matcher if there are multiple matchers applicable
            if (numberOfUsableViewMatchers > 1) {
                buildAllOf();
            }

            if (isNonEmptyTextView()) {
                // close the oneOf() matcher
                builder.append(")");
            }
        }
    }

    /**
     * Determines how many view matchers can be applied.
     *
     * @return Returns the number of applicable view matchers.
     */
    private int getNumberOfUsableViewMatchers() {
        int numberOfUsableViewMatchers = 1; // there is at least one matcher applicable

        if (getShortClassName() != null) {
            numberOfUsableViewMatchers++;
        }

        if (isNonEmptyTextView()) {
            numberOfUsableViewMatchers++;
        }

        return numberOfUsableViewMatchers;
    }

    /**
     * Builds a view matcher for the given resource id using the 'withIDString()' matcher.
     *
     * @param resourceID The non empty resource id.
     */
    private void buildWithIDString(String resourceID) {
        builder.append("withIDString(\"").append("R.id.").append(resourceID).append("\")");
    }

    /**
     * Builds a view matcher for a text view using the custom 'oneOf()' matcher.
     */
    private void buildOneOf() {
            builder.append("oneOf(");
            buildTextMatcher(true);
    }

    /**
     * Builds a view matcher for a text view using the 'withText()' matcher.
     *
     * @param appendComma Whether to append a comma at the end.
     */
    private void buildTextMatcher(boolean appendComma) {
        builder.append(WITH_TEXT).append("(\"").append(widget.getText()).append("\")");
        if (appendComma) {
            builder.append(", ");
        }
    }

    /**
     * Builds a view matcher using the 'allOf()' matcher, see the hamcrest API for more details.
     */
    private void buildAllOf() {
            builder.append(ALL_OF).append("(");
            buildCoordinatesMatcher();
            buildClassMatcher();
            buildTextMatcher(false);
            builder.append(")");
    }

    /**
     * Builds a view matcher based on the coordinates using 'withCoordinates()'.
     */
    private void buildCoordinatesMatcher() {
        builder.append("withCoordinates(")
                .append(widget.getX1()).append(", ")
                .append(widget.getY1()).append(", ")
                .append(widget.getX2() - widget.getX1()).append(", ") // width
                .append(widget.getY2() - widget.getY1()) // height
                .append(")");
    }

    /**
     * Builds a view matcher based on the class name.
     */
    private void buildClassMatcher() {
        String className = getShortClassName();
        if (className != null && !className.equals("ImageView")) {
            builder.append(", ").append("withClassNameString(\"").append(className).append("\")");
        }
    }

    /**
     * Checks whether the widget represents a non-empty text view.
     *
     * @return Returns {@code true} if the widget is a non-empty text view.
     */
    private boolean isNonEmptyTextView() {
        // TODO: Is the class check necessary or can it be any kind of text view?
        return widget.getClazz().equals(TextView.class.getName())
                && widget.getText() != null
                && !widget.getText().isEmpty();
    }

    /**
     * Returns a short form of the original class name.
     *
     * @return Returns the short class name or {@code null} if the class name is not defined.
     */
    private String getShortClassName() {
        if (!widget.getClazz().isEmpty()) {
            // the class name looks as follows: android.widget.TextView
            String[] tokens = widget.getClazz().split("\\.");
            return tokens[tokens.length - 1];
        } else {
            return null;
        }
    }
}
