package org.mate.representation.interaction;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.Widget;

import java.util.Objects;

public class PrimitiveActionExecutor extends ActionExecutor {

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    public boolean perform(Action action) throws AUTCrashException {
        // executeAction((PrimitiveAction) action);
        return false;
    }

    /**
     * Executes a primitive action, e.g. a click on a specific coordinate.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    /*private void executeAction(PrimitiveAction action) throws AUTCrashException {

        switch (action.getActionType()) {
            case CLICK:
                handleClick(action);
                break;
            case LONG_CLICK:
                device.swipe(action.getX(), action.getY(), action.getX(), action.getY(), 120);
                break;
            case SWIPE_DOWN:
                device.swipe(action.getX(), action.getY(), action.getX(), action.getY() - 300, 15);
                break;
            case SWIPE_UP:
                device.swipe(action.getX(), action.getY(), action.getX(), action.getY() + 300, 15);
                break;
            case SWIPE_LEFT:
                device.swipe(action.getX(), action.getY(), action.getX() + 300, action.getY(), 15);
                break;
            case SWIPE_RIGHT:
                device.swipe(action.getX(), action.getY(), action.getX() - 300, action.getY(), 15);
                break;
            case TYPE_TEXT:
                handleEdit(action);
                break;
            case BACK:
                device.pressBack();
                break;
            case MENU:
                device.pressMenu();
                break;
            default:
                throw new IllegalArgumentException("Action type " + action.getActionType()
                        + " not implemented for primitive actions.");
        }
        checkForCrash();
    }*/

    /**
     * Performs a click based on the coordinates of the given action.
     *
     * @param action The given primitive action.
     */
    // private void handleClick(PrimitiveAction action) {
    //     device.click(action.getX(), action.getY());
    // }

    /**
     * Inserts a text into a input field based on the given primitive action.
     *
     * @param action The given primitive action.
     */
    // private void handleEdit(PrimitiveAction action) {
    //
    //     // clicking on the screen should get a focus on the underlying 'widget'
    //     device.click(action.getX(), action.getY());
    //     UiObject2 uiElement = device.findObject(By.focused(true));
    //
    //     if (uiElement != null) {
    //
    //         Widget widget = null;
    //
    //         try {
    //             /*
    //              * We resort here to the underlying widget, otherwise we have no idea what type of
    //              * text should be generated as input.
    //              */
    //             widget = findWidget(uiElement);
    //         } catch (StaleObjectException e) {
    //
    //             MATELog.log_warn("Stale ui element!");
    //             e.printStackTrace();
    //
    //             /*
    //              * Unfortunately, it can happen that the requested ui element gets immediately stale.
    //              * The only way to recover from such a situation is to call findObject() another time.
    //              */
    //             uiElement = device.findObject(By.focused(true));
    //             if (uiElement != null) {
    //                 widget = findWidget(uiElement);
    //             }
    //         }
    //
    //         if (widget != null && widget.isEditTextType()) {
    //
    //             /*
    //              * If we run in replay mode, we should use the recorded text instead of a new text
    //              * that is randomly created. Otherwise, we may end up in a different state and
    //              * subsequent actions might not show the same behaviour as in the recorded run.
    //              */
    //             String textData = Registry.isReplayMode() ? action.getText() :
    //                     Objects.toString(generateTextData(widget, widget.getMaxTextLength()), "");
    //
    //             MATELog.log_debug("Inserting text: " + textData);
    //             uiElement.setText(textData);
    //
    //             // record for possible replaying + findObject() relies on it
    //             action.setText(textData);
    //             widget.setText(textData);
    //         }
    //
    //         // we need to close the soft keyboard, but only if it is present, see:
    //         // https://stackoverflow.com/questions/17223305/suppress-keyboard-after-setting-text-with-android-uiautomator
    //         device.pressBack();
    //     }
    // }

    /**
     * Finds the corresponding widget to the given ui element if possible. This considers the
     * class name, e.g. android.widget.EditText, the boundaries and the resource name for the
     * comparison.
     *
     * @param uiElement The given ui element.
     * @return Returns the corresponding widget or {@code null} if not possible.
     */
    // private Widget findWidget(UiObject2 uiElement) {
    //
    //     IScreenState screenState = Registry.getUiAbstractionLayer().getLastScreenState();
    //
    //     for (Widget widget : screenState.getWidgets()) {
    //
    //         String resourceID = widget.getResourceID().isEmpty() ? null : widget.getResourceID();
    //
    //         if (widget.getClazz().equals(uiElement.getClassName())
    //                 && widget.getBounds().equals(uiElement.getVisibleBounds())
    //                 && Objects.equals(resourceID, uiElement.getResourceName())) {
    //             return widget;
    //         }
    //     }
    //
    //     return null;
    // }
}
