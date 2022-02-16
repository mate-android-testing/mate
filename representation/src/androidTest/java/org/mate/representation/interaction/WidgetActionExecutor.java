package org.mate.representation.interaction;

import static org.mate.commons.interaction.action.ui.ActionType.SWIPE_DOWN;
import static org.mate.commons.interaction.action.ui.ActionType.SWIPE_UP;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.input_generation.DataGenerator;
import org.mate.commons.input_generation.Mutation;
import org.mate.commons.input_generation.StaticStrings;
import org.mate.commons.input_generation.StaticStringsParser;
import org.mate.commons.input_generation.format_types.InputFieldType;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.ActionType;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.interaction.action.ui.WidgetAction;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Utils;
import org.mate.representation.DeviceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class WidgetActionExecutor extends ActionExecutor {

    /**
     * The probability for considering the hint for the input generation.
     */
    private static final double PROB_HINT = 0.5;

    /**
     * The probability for mutating a given hint.
     */
    private static final double PROB_HINT_MUTATION = 0.5;

    /**
     * The probability for using a static string or the input generation.
     */
    private static final double PROB_STATIC_STRING = 0.5;

    /**
     * The probability for mutating a static string.
     */
    private static final double PROB_STATIC_STRING_MUTATION = 0.25;

    /**
     * Contains the static strings extracted from the byte code.
     */
    private final StaticStrings staticStrings;

    public WidgetActionExecutor() {
        super();

        this.staticStrings = StaticStringsParser.parseStaticStrings();
    }

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    public boolean perform(Action action) throws AUTCrashException {
        return executeAction((WidgetAction) action);
    }

    /**
     * Executes a widget action, e.g. a click on a certain widget.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    private boolean executeAction(WidgetAction action) throws AUTCrashException {
        Widget selectedWidget = action.getWidget();
        ActionType typeOfAction = action.getActionType();

        boolean success = false;

        switch (typeOfAction) {
            case CLICK:
                success = handleClick(selectedWidget);
                break;
            case LONG_CLICK:
                success = handleLongClick(selectedWidget);
                break;
            case TYPE_TEXT:
                success = handleEdit(selectedWidget);
                break;
            case TYPE_SPECIFIC_TEXT:
                success = handleEdit(selectedWidget);
                break;
            case CLEAR_WIDGET:
                // TODO: Do we actually need this action?
                //  A 'type text' action overwrites the previous text anyways.
                success = handleClear(selectedWidget);
                break;
            case SWIPE_DOWN:
            case SWIPE_UP:
            case SWIPE_LEFT:
            case SWIPE_RIGHT:
                success = handleSwipe(selectedWidget, typeOfAction);
                break;
            default:
                throw new IllegalArgumentException("Action type " + action.getActionType()
                        + " not implemented for widget actions.");
        }

        // if there is a progress bar associated to that action
        Utils.sleep(action.getTimeToWait());

        return success;
    }
    /**
     * Executes a click on the given widget.
     *
     * @param widget The widget on which a click should be performed.
     */
    private boolean handleClick(Widget widget) {
        return device.click(widget.getX(), widget.getY());
    }

    /**
     * Clears the widget's text.
     *
     * @param widget The widget whose input should be cleared.
     */
    private boolean handleClear(Widget widget) {
        UiObject2 obj = findObject(widget);
        if (obj != null) {
            obj.setText("");
            // reflect change since we cache screen states and findObject() relies on it
            widget.setText("");
            return true;
        }

        return false;
    }

    /**
     * Executes a swipe (upon a widget) in a given direction.
     *
     * @param widget The widget at which position the swipe should be performed.
     * @param direction The direction of the swipe, e.g. swipe to the left.
     */
    private boolean handleSwipe(Widget widget, ActionType direction) {

        int pixelsmove = 300;
        int X = 0;
        int Y = 0;
        int steps = 15;

        if (widget != null && !widget.getClazz().isEmpty()) {
            UiObject2 obj = findObject(widget);
            if (obj != null) {
                X = obj.getVisibleBounds().centerX();
                Y = obj.getVisibleBounds().centerY();
            } else {
                X = widget.getX();
                Y = widget.getY();
            }
        } else {
            X = device.getDisplayWidth() / 2;
            Y = device.getDisplayHeight() / 2;
            if (direction == SWIPE_DOWN || direction == SWIPE_UP)
                pixelsmove = Y;
            else
                pixelsmove = X;
        }

        // 50 pixels has been arbitrarily selected - create a properties file in the future
        switch (direction) {
            case SWIPE_DOWN:
                return device.swipe(X, Y, X, Y - pixelsmove, steps);
            case SWIPE_UP:
                return device.swipe(X, Y, X, Y + pixelsmove, steps);
            case SWIPE_LEFT:
                return device.swipe(X, Y, X + pixelsmove, Y, steps);
            case SWIPE_RIGHT:
                return device.swipe(X, Y, X - pixelsmove, Y, steps);
        }

        return false;
    }

    /**
     * Performs a long click on the given widget.
     *
     * @param widget The widget on which a long click should be applied.
     */
    private boolean handleLongClick(Widget widget) {
        // TODO: consider https://stackoverflow.com/questions/21432561/how-to-achieve-long-click-in-uiautomator
        UiObject2 obj = findObject(widget);
        int X = widget.getX();
        int Y = widget.getY();
        if (obj != null) {
            X = obj.getVisibleBounds().centerX();
            Y = obj.getVisibleBounds().centerY();
        }
        return device.swipe(X, Y, X, Y, 120);
    }
    /**
     * Handles the insertion of a text in the given editable widget.
     *
     * @param widget The editable widget.
     */
    private boolean handleEdit(Widget widget) {

        /*
         * If we run in replay mode, we should insert the text that we recorded, otherwise we may
         * break execution, since a different (valid) input may lead to a different state, e.g. we
         * end on a different activity and all subsequent widget actions are not applicable anymore.
         */
        String textData = Registry.isReplayMode() ? widget.getText() :
                Objects.toString(generateTextData(widget, widget.getMaxTextLength()), "");

        MATELog.log_debug("Input text: " + textData);
        MATELog.log_debug("Previous text: " + widget.getText());

        UiObject2 uiObject = findObject(widget);

        if (uiObject != null) {

            uiObject.setText(textData);

            // reflect change since we cache screen states and findObject() relies on it
            widget.setText(textData);
        } else {
            // try to click on the widget, which in turn should get focused
            device.click(widget.getX(), widget.getY());
            UiObject2 obj = device.findObject(By.focused(true));
            if (obj != null) {
                obj.setText(textData);

                // reflect change since we cache screen states and findObject() relies on it
                widget.setText(textData);

                // we need to close the soft keyboard, but only if it is present, see:
                // https://stackoverflow.com/questions/17223305/suppress-keyboard-after-setting-text-with-android-uiautomator
                device.pressBack();
            } else {
                MATELog.log("  ********* obj " + widget.getId() + "  not found");
            }
        }
    }

    /**
     * Tries to return a ui object matching the given widget. This is a
     * best effort approach.
     *
     * @param widget The widget whose ui object should be looked up.
     * @return Returns the corresponding ui object or {@code null} if no
     *         such ui object could be found.
     */
    private UiObject2 findObject(Widget widget) {

        // retrieve all ui objects that match the given widget resource id
        List<UiObject2> objs = device.findObjects(By.res(widget.getResourceID()));

        if (objs != null) {
            if (objs.size() == 1) {
                return objs.get(0);
            } else {
                /*
                 * It can happen that multiple widgets share the same resource id,
                 * thus we need to compare on the text attribute.
                 */
                for (UiObject2 uiObject2 : objs) {
                    if (uiObject2.getText() != null && uiObject2.getText().equals(widget.getText()))
                        return uiObject2;
                }
            }
        }

        // if no match for id, try to find the object by text match
        objs = device.findObjects(By.text(widget.getText()));

        if (objs != null) {
            if (objs.size() == 1) {
                return objs.get(0);
            } else {
                // try to match by content description or widget boundary
                for (UiObject2 uiObject2 : objs) {
                    if (uiObject2.getContentDescription() != null
                            && uiObject2.getContentDescription().equals(widget.getContentDesc()) ||
                            (uiObject2.getVisibleBounds() != null
                                    && uiObject2.getVisibleBounds().centerX() == widget.getX()
                                    && uiObject2.getVisibleBounds().centerY() == widget.getY()))
                        return uiObject2;
                }
            }
        }
        return null;
    }

    /**
     * Generates a text input for the given editable widget.
     *
     * @param widget The editable widget.
     * @param maxLength The maximal input length.
     * @return Returns a text input for the editable widget.
     */
    private String generateTextData(final Widget widget, final int maxLength) {

        final String activityName = convertClassName(widget.getActivity());

        final InputFieldType inputFieldType = InputFieldType.getFieldTypeByNumber(widget.getInputType());
        final Random random = DeviceInfo.getInstance().getRandom();

        /*
         * If a hint is present and with probability PROB_HINT we select the hint as input. Moreover,
         * with probability PROB_HINT_MUTATION we mutate the given hint.
         */
        if (widget.isHintPresent()) {
            if (inputFieldType.isValid(widget.getHint()) && random.nextDouble() < PROB_HINT) {
                if (inputFieldType != InputFieldType.NOTHING && random.nextDouble() < PROB_HINT_MUTATION) {
                    return Mutation.mutateInput(inputFieldType, widget.getHint());
                } else {
                    return widget.getHint();
                }
            }
        }
        if (staticStrings.isInitialised()) {
            /*
             * If the static strings from the bytecode were supplied and with probability
             * PROB_STATIC_STRING we try to find a static string matching the input field type.
             */
            if (random.nextDouble() < PROB_STATIC_STRING) {

                // consider both the string constants from the current activity and visible fragments
                List<String> uiComponents = new ArrayList<>();
                uiComponents.add(activityName);
                uiComponents.addAll(DeviceInfo.getInstance().getCurrentFragments());

                String randomStaticString;

                if (inputFieldType != InputFieldType.NOTHING) {

                    // get a random string matching the input field type from one of the ui classes
                    randomStaticString = staticStrings.getRandomStringFor(inputFieldType, uiComponents);

                    /*
                     * If there was no match, we consider a random string from any class matching
                     * the given input field type.
                     */
                    if (randomStaticString == null) {
                        randomStaticString = staticStrings.getRandomStringFor(inputFieldType);
                    }

                    // mutate the string with probability PROB_STATIC_STRING_MUTATION
                    if (randomStaticString != null) {
                        if (random.nextDouble() < PROB_STATIC_STRING_MUTATION) {
                            randomStaticString = Mutation.mutateInput(inputFieldType, randomStaticString);
                        }
                        return randomStaticString;
                    }
                }

                /*
                 * If the input field type couldn't be determined or no static string could be
                 * derived so far, we try to use a random string from either the current activity
                 * or any of the visible fragments.
                 */
                randomStaticString = staticStrings.getRandomStringFor(uiComponents);
                if (randomStaticString != null) {
                    return randomStaticString;
                }
            }
        }

        // fallback mechanism
        return generateRandomInput(inputFieldType, maxLength);
    }

    /**
     * Converts a fully-qualified class name to solely it's class name, i.e. the possibly redundant
     * package name is stripped off.
     *
     * @param className The fully-qualified class name consisting of <package-name>/<class-name>.
     * @return Returns the simple class name.
     */
    private String convertClassName(String className) {

        String[] tokens = className.split("/");
        String packageName = tokens[0];
        String componentName = tokens[1];

        // if the component resides in the application package, a dot is used instead of the package name
        if (componentName.startsWith(".")) {
            componentName = packageName + componentName;
        }

        return componentName;
    }

    /**
     * Generates a random input as a fallback mechanism. A random string is generated and shortened
     * to the maximum length if it is too long.
     *
     * @param inputFieldType The field for which the string is to be generated.
     * @param maxLength The maximum length of the result string.
     * @return A random string matching the given {@link InputFieldType} with at most maxLength
     *         length.
     */
    private String generateRandomInput(InputFieldType inputFieldType, int maxLength) {
        String randomData = DataGenerator.generateRandomData(inputFieldType);
        if (maxLength > 0 && randomData.length() > maxLength) {
            randomData = randomData.substring(0, maxLength);
        }
        return randomData;
    }
}
