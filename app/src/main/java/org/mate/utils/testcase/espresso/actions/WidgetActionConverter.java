package org.mate.utils.testcase.espresso.actions;

import android.widget.TextView;

import org.mate.Registry;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.utils.testcase.espresso.EspressoDependency;

import java.util.EnumSet;

import static org.mate.utils.testcase.espresso.EspressoDependency.ALL_OF;
import static org.mate.utils.testcase.espresso.EspressoDependency.CLEAR_TEXT;
import static org.mate.utils.testcase.espresso.EspressoDependency.CLOSE_SOFT_KEYBOARD;
import static org.mate.utils.testcase.espresso.EspressoDependency.SCROLL_TO;
import static org.mate.utils.testcase.espresso.EspressoDependency.WITH_TEXT;

/**
 * Converts a {@link WidgetAction} to an espresso action.
 */
public class WidgetActionConverter extends ActionConverter {

    /**
     * The widget on which an action should be applied.
     */
    private final Widget widget;

    /**
     * The type of action that should be applied.
     */
    private final ActionType actionType;

    /**
     * Stores the number of applicable view matchers.
     */
    private final int numberOfUsableViewMatchers;

    /**
     * Constructs a converter for the given widget action.
     *
     * @param action The widget action that should be converted to an espresso action.
     */
    public WidgetActionConverter(WidgetAction action) {
        super(action);
        this.widget = action.getWidget();
        this.actionType = action.getActionType();
        this.numberOfUsableViewMatchers = getNumberOfUsableViewMatchers();
    }

    /**
     * Returns a short form of the resource id.
     *
     * @return Returns a short form of the resource id or {@code null} if the resource id doesn't
     *         start with the package name or isn't valid.
     */
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

        builder.append(".perform(");

        EnumSet<ActionType> textActions = EnumSet.of(ActionType.CLEAR_TEXT,
                ActionType.TYPE_SPECIFIC_TEXT, ActionType.TYPE_TEXT);

        if (textActions.contains(actionType)) {
            // we need to close the soft keyboard first
            builder.append(CLOSE_SOFT_KEYBOARD).append("()").append(", ");
        }

        if (widget.isSonOfScrollView()) {
            builder.append(SCROLL_TO).append("()").append(", ");
        }

        // remove any text before we insert some new text
        if (actionType == ActionType.TYPE_TEXT || actionType == ActionType.TYPE_SPECIFIC_TEXT) {
            builder.append(CLEAR_TEXT).append("()").append(", ");
        }

        // insert the actual action
        buildAction();

        if (textActions.contains(actionType)) {
            // close the soft keyboard in case we inserted some new text
            builder.append(", ").append(CLOSE_SOFT_KEYBOARD).append("()");
        }

        // close 'perform()' clause
        builder.append(");");
    }

    /**
     * Builds the actual action together.
     */
    private void buildAction() {

        EspressoDependency dependency;

        switch (actionType) {
            case CLICK:
                dependency = EspressoDependency.CLICK;
                break;
            case LONG_CLICK:
                dependency = EspressoDependency.LONG_CLICK;
                break;
            case TYPE_TEXT:
            case TYPE_SPECIFIC_TEXT:
                dependency = EspressoDependency.TYPE_TEXT;
                break;
            case CLEAR_TEXT:
                dependency = CLEAR_TEXT;
                break;
            case SWIPE_LEFT:
                dependency = EspressoDependency.SWIPE_LEFT;
                break;
            case SWIPE_RIGHT:
                dependency = EspressoDependency.SWIPE_RIGHT;
                break;
            case SWIPE_DOWN:
                dependency = EspressoDependency.SWIPE_DOWN;
                break;
            case SWIPE_UP:
                dependency = EspressoDependency.SWIPE_UP;
                break;
            default:
                throw new UnsupportedOperationException("Action type " + actionType
                        + " not yet supported");
        }

        builder.append(dependency).append("(");

        // the text actions requires the text to insert as additional argument
        if (dependency == EspressoDependency.TYPE_TEXT) {
            builder.append("\"");
            builder.append(widget.getText());
            builder.append("\"");
        }

        builder.append(")");
    }

    /**
     * {@inheritDoc}
     */
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
        // TODO: Why can't we build a class matcher for an image view?
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
                // TODO: Should we consider the content description attribute as well?
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
