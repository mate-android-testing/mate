package org.mate.representation.interaction;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.StaleObjectException;
import androidx.test.uiautomator.UiObject2;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.input_generation.Mutation;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.ActionType;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.interaction.action.ui.WidgetAction;
import org.mate.commons.utils.MATELog;
import org.mate.representation.DeviceInfo;
import org.mate.representation.ExplorationInfo;
import org.mate.representation.input_generation.TextDataGenerator;

import java.util.List;
import java.util.Objects;

import static org.mate.commons.interaction.action.ui.ActionType.SWIPE_DOWN;
import static org.mate.commons.interaction.action.ui.ActionType.SWIPE_UP;

/**
 * ActionExecutor class for Widget actions.
 */
public class WidgetActionExecutor extends ActionExecutor {

    public WidgetActionExecutor() {
        super();

        Mutation.setRandom(ExplorationInfo.getInstance().getRandom());
    }

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    @Override
    public boolean perform(Action action) throws AUTCrashException {
        return executeAction((WidgetAction) action);
    }

    /**
     * Executes a widget action, e.g. a click on a certain widget.
     *
     * @param action The action to be executed.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
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
            case TYPE_SPECIFIC_TEXT:
                success = handleEdit(selectedWidget);
                break;
            case CLEAR_TEXT:
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

        return success;
    }

    /**
     * Executes a click on the given widget.
     *
     * @param widget The widget on which a click should be performed.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     */
    public boolean handleClick(Widget widget) {
        return device.click(widget.getX(), widget.getY());
    }

    /**
     * Clears the widget's text.
     *
     * @param widget The widget whose input should be cleared.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     */
    private boolean handleClear(Widget widget) {

        UiObject2 uiObject = findObject(widget);

        if (uiObject != null) {

            try {
                uiObject.setText("");

                // reflect change since we cache screen states and findObject() relies on it
                widget.setText("");

                // we need to close the soft keyboard, but only if it is present
                if (DeviceInfo.getInstance().isKeyboardOpened()) {
                    device.pressBack();
                }

                return true;
            } catch (StaleObjectException e) {
                MATELog.log_warn("Stale UiObject2!");
                e.printStackTrace();
                return handleEditFallback(widget, "");
            }
        } else {
            return handleEditFallback(widget, "");
        }
    }

    /**
     * Executes a swipe (upon a widget) in a given direction.
     *
     * @param widget The widget at which position the swipe should be performed.
     * @param direction The direction of the swipe, e.g. swipe to the left.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     */
    private boolean handleSwipe(Widget widget, ActionType direction) {

        int pixelsmove = 300;
        int X = 0;
        int Y = 0;
        int steps = 15;

        if (widget != null && !widget.getClazz().isEmpty()) {
            UiObject2 obj = findObject(widget);
            if (obj != null) {
                try {
                    X = obj.getVisibleBounds().centerX();
                    Y = obj.getVisibleBounds().centerY();
                } catch (StaleObjectException e) {
                    MATELog.log_warn("Stale UiObject2!");
                    e.printStackTrace();
                    X = widget.getX();
                    Y = widget.getY();
                }

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
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     */
    private boolean handleLongClick(Widget widget) {

        // TODO: consider https://stackoverflow.com/questions/21432561/how-to-achieve-long-click-in-uiautomator
        UiObject2 obj = findObject(widget);

        int X = 0, Y = 0;

        try {
            if (obj != null) {
                X = obj.getVisibleBounds().centerX();
                Y = obj.getVisibleBounds().centerY();
            }
        } catch (StaleObjectException e) {
            MATELog.log_warn("Stale UiObject2!");
            e.printStackTrace();
            X = widget.getX();
            Y = widget.getY();
        }

        return device.swipe(X, Y, X, Y, 120);
    }

    /**
     * Handles the insertion of a text in the given editable widget.
     *
     * @param widget The editable widget.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     */
    public boolean handleEdit(Widget widget) {

        /*
         * If we run in replay mode, we should insert the text that we recorded, otherwise we may
         * break execution, since a different (valid) input may lead to a different state, e.g. we
         * end on a different activity and all subsequent widget actions are not applicable anymore.
         */
        String textData = ExplorationInfo.getInstance().isReplayMode() ? widget.getText() :
                Objects.toString(TextDataGenerator.getInstance().generateTextData(
                        widget,
                        widget.getMaxTextLength()), "");

        MATELog.log_debug("Input text: " + textData);
        MATELog.log_debug("Previous text: " + widget.getText());

        UiObject2 uiObject = findObject(widget);

        if (uiObject != null) {

            try {
                uiObject.setText(textData);

                // reflect change since we cache screen states and findObject() relies on it
                widget.setText(textData);

                // we need to close the soft keyboard, but only if it is present
                if (DeviceInfo.getInstance().isKeyboardOpened()) {
                    device.pressBack();
                }

                return true;
            } catch (StaleObjectException e) {
                MATELog.log_warn("Stale UiObject2!");
                e.printStackTrace();
                return handleEditFallback(widget, textData);
            }
        } else {
            return handleEditFallback(widget, textData);
        }
    }

    /**
     * Provides a fallback mechanism for editing a text field.
     *
     * @param widget The text field widget.
     * @param textData The text to be inserted.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     */
    private boolean handleEditFallback(Widget widget, String textData) {

        // try to click on the widget, which in turn should get focused
        device.click(widget.getX(), widget.getY());
        UiObject2 obj = device.findObject(By.focused(true));
        if (obj != null) {
            try {
                obj.setText(textData);

                // reflect change since we cache screen states and findObject() relies on it
                widget.setText(textData);
                return true;
            } catch (StaleObjectException e) {
                MATELog.log_warn("Stale UiObject2!");
                e.printStackTrace();
                MATELog.log_warn("Couldn't edit widget: " + widget);
            } finally {
                // we need to close the soft keyboard, but only if it is present
                if (DeviceInfo.getInstance().isKeyboardOpened()) {
                    device.pressBack();
                }
            }
        } else {
            MATELog.log_warn("Couldn't edit widget: " + widget);
        }
        return false;
    }

    /**
     * Tries to return a ui object matching the given widget. This is a best effort approach.
     *
     * @param widget The widget whose ui object should be looked up.
     * @return Returns the corresponding ui object or {@code null} if no such ui object could be
     *         found or the ui object got be stale in the meantime.
     */
    public UiObject2 findObject(Widget widget) {

        try {

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

        } catch (StaleObjectException e) {
            MATELog.log_warn("Stale UiObject2!");
            e.printStackTrace();
        }

        return null;
    }
}
