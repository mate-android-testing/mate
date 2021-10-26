package org.mate.interaction;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiSelector;
import android.text.InputType;
import android.util.Log;

import org.mate.MATE;
import org.mate.Mutation;
import org.mate.Registry;
import org.mate.datagen.DataGenerator;
import org.mate.exceptions.AUTCrashException;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.intent.ComponentType;
import org.mate.interaction.action.intent.IntentBasedAction;
import org.mate.interaction.action.intent.SystemAction;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.PrimitiveAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.deprecated.graph.IGUIModel;
import org.mate.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static org.mate.interaction.action.ui.ActionType.SWIPE_DOWN;
import static org.mate.interaction.action.ui.ActionType.SWIPE_UP;

/**
 * The device manager is responsible for the actual execution of the various actions.
 * Also provides functionality to check for crashes, restart or re-install the AUT, etc.
 */
public class DeviceMgr {

    /**
     * The ADB command to to disable auto rotation.
     */
    private final String DISABLE_AUTO_ROTATION_CMD = "content insert " +
            "--uri content://settings/system --bind name:s:accelerometer_rotation --bind value:i:0";

    /**
     * The ADB command to rotate the emulator into portrait mode.
     */
    private final String PORTRAIT_MODE_CMD = "content insert --uri content://settings/system " +
            "--bind name:s:user_rotation --bind value:i:0";

    /**
     * The ADB command to rotate the emulator into landscape mode.
     */
    private final String LANDSCAPE_MODE_CMD = "content insert --uri content://settings/system " +
            "--bind name:s:user_rotation --bind value:i:1";

    /**
     * The device instance provided by the instrumentation class to perform various actions.
     */
    private final UiDevice device;

    /**
     * The package name of the AUT.
     */
    private final String packageName;

    /**
     * Keeps track whether the emulator is in portrait or landscape mode.
     */
    private boolean isInPortraitMode;

    /**
     * Keeps track whether auto rotation has been disabled.
     */
    private boolean disabledAutoRotate;

    public DeviceMgr(UiDevice device, String packageName) {
        this.device = device;
        this.packageName = packageName;
        this.isInPortraitMode = true;
        this.disabledAutoRotate = false;
    }

    /**
     * Returns the ui device instance.
     *
     * @return Returns the ui device instance.
     */
    public UiDevice getDevice() {
        return device;
    }

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    public void executeAction(Action action) throws AUTCrashException {

        if (action instanceof WidgetAction) {
            executeAction((WidgetAction) action);
        } else if (action instanceof PrimitiveAction) {
            executeAction((PrimitiveAction) action);
        } else if (action instanceof IntentBasedAction) {
            executeAction((IntentBasedAction) action);
        } else if (action instanceof SystemAction) {
            executeAction((SystemAction) action);
        } else if (action instanceof UIAction) {
            executeAction((UIAction) action);
        } else {
            throw new UnsupportedOperationException("Actions class "
                    + action.getClass().getSimpleName() + " not yet supported");
        }
    }

    /**
     * Simulates the occurrence of a system event.
     *
     * @param event The system event.
     */
    private void executeAction(SystemAction event) throws AUTCrashException {
        Registry.getEnvironmentManager().executeSystemEvent(Registry.getPackageName(), event.getReceiver(),
                event.getAction(), event.isDynamicReceiver());
        checkForCrash();
    }

    /**
     * Executes the given ui action.
     *
     * @param action The given ui action.
     * @throws AUTCrashException If the app crashes.
     */
    private void executeAction(UIAction action) throws AUTCrashException {

        ActionType typeOfAction = action.getActionType();

        switch (typeOfAction) {
            case BACK:
                device.pressBack();
                break;
            case MENU:
                device.pressMenu();
                break;
            case ENTER:
                device.pressEnter();
                break;
            case HOME:
                device.pressHome();
                break;
            case QUICK_SETTINGS:
                device.openQuickSettings();
                break;
            case SEARCH:
                device.pressSearch();
                break;
            case SLEEP:
                // Only reasonable when a wake up is performed soon, otherwise
                // succeeding actions have no effect.
                try {
                    device.sleep();
                } catch (RemoteException e) {
                    MATE.log("Sleep couldn't be performed");
                    e.printStackTrace();
                }
                break;
            case WAKE_UP:
                try {
                    device.wakeUp();
                } catch (RemoteException e) {
                    MATE.log("Wake up couldn't be performed");
                    e.printStackTrace();
                }
                break;
            case DELETE:
                device.pressDelete();
                break;
            case DPAD_UP:
                device.pressDPadUp();
                break;
            case DPAD_DOWN:
                device.pressDPadDown();
                break;
            case DPAD_LEFT:
                device.pressDPadLeft();
                break;
            case DPAD_RIGHT:
                device.pressDPadRight();
                break;
            case DPAD_CENTER:
                device.pressDPadCenter();
                break;
            case NOTIFICATIONS:
                device.openNotification();
                break;
            case TOGGLE_ROTATION:
                toggleRotation();
                break;
            case MANUAL_ACTION:
                // simulates a manual user interaction
                break;
            default:
                throw new UnsupportedOperationException("UI action "
                        + action.getActionType() + " not yet supported!");
        }
        checkForCrash();
    }

    /**
     * Executes an Intent-based action. Depending on the target component, either
     * startActivity(), startService() or sendBroadcast() is invoked.
     *
     * @param action The action which contains the Intent to be sent.
     */
    private void executeAction(IntentBasedAction action) throws AUTCrashException {

        Intent intent = action.getIntent();

        try {
            switch (action.getComponentType()) {
                case ACTIVITY:
                    InstrumentationRegistry.getTargetContext().startActivity(intent);
                    break;
                case SERVICE:
                    InstrumentationRegistry.getTargetContext().startService(intent);
                    break;
                case BROADCAST_RECEIVER:
                    InstrumentationRegistry.getTargetContext().sendBroadcast(intent);
                    break;
                default:
                    throw new UnsupportedOperationException("Component type not supported yet!");
            }
        } catch (Exception e) {
            final String msg = "Calling startActivity() from outside of an Activity  context " +
                    "requires the FLAG_ACTIVITY_NEW_TASK flag.";
            if (e.getMessage().contains(msg) && action.getComponentType() == ComponentType.ACTIVITY) {
                MATE.log("Retrying sending intent with ACTIVITY_NEW_TASK flag!");
                try {
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    InstrumentationRegistry.getTargetContext().startActivity(intent);
                } catch (Exception ex) {
                    MATE.log("Executing Intent-based action failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                MATE.log("Executing Intent-based action failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
        checkForCrash();
    }

    /**
     * Executes a primitive action, e.g. a click on a specific coordinate.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    private void executeAction(PrimitiveAction action) throws AUTCrashException {

        switch (action.getActionType()) {
            case CLICK:
                device.click(action.getX(), action.getY());
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
    }

    /**
     * Executes a widget action, e.g. a click on a certain widget.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    private void executeAction(WidgetAction action) throws AUTCrashException {

        Widget selectedWidget = action.getWidget();
        ActionType typeOfAction = action.getActionType();

        switch (typeOfAction) {
            case CLICK:
                handleClick(selectedWidget);
                break;
            case LONG_CLICK:
                handleLongClick(selectedWidget);
                break;
            case TYPE_TEXT:
                handleEdit(action);
                break;
            case TYPE_SPECIFIC_TEXT:
                handleEdit(action);
            case CLEAR_WIDGET:
                // TODO: Do we actually need this action?
                //  A 'type text' action overwrites the previous text anyways.
                handleClear(selectedWidget);
                break;
            case SWIPE_DOWN:
            case SWIPE_UP:
            case SWIPE_LEFT:
            case SWIPE_RIGHT:
                handleSwipe(selectedWidget, typeOfAction);
                break;
            default:
                throw new IllegalArgumentException("Action type " + action.getActionType()
                        + " not implemented for widget actions.");
        }

        // if there is a progress bar associated to that action
        Utils.sleep(action.getTimeToWait());
        checkForCrash();
    }

    /**
     * Checks whether a crash dialog appeared on the screen.
     *
     * @throws AUTCrashException Thrown when the last action caused a crash of the application.
     */
    private void checkForCrash() throws AUTCrashException {

        if (checkForCrashDialog()) {
            MATE.log("CRASH");
            throw new AUTCrashException("App crashed");
        }
    }

    /**
     * Checks whether a crash dialog is visible on the current screen.
     *
     * @return Returns {@code true} if a crash dialog is visible, otherwise {@code false}
     * is returned.
     */
    public boolean checkForCrashDialog() {

        UiObject crashDialog1 = device.findObject(
                new UiSelector().packageName("android").textContains("keeps stopping"));
        UiObject crashDialog2 = device.findObject(
                new UiSelector().packageName("android").textContains("has stopped"));

        return crashDialog1.exists() || crashDialog2.exists();
    }

    /**
     * Checks whether the given widget represents a progress bar.
     *
     * @param widget The given widget.
     * @return Returns {@code true} if the widget refers to a progress bar,
     * otherwise {@code false} is returned.
     */
    public boolean checkForProgressBar(Widget widget) {
        return widget.getClazz().contains("ProgressBar")
                && widget.isEnabled()
                && widget.getContentDesc().contains("Loading");
    }

    /**
     * Toggles the rotation between portrait and landscape mode. Based on the following reference:
     * https://stackoverflow.com/questions/25864385/changing-android-device-orientation-with-adb
     */
    private void toggleRotation() {

        if (!disabledAutoRotate) {
            disableAutoRotation();
        }

        try {
            String output = device.executeShellCommand(isInPortraitMode ? LANDSCAPE_MODE_CMD : PORTRAIT_MODE_CMD);
            if (!output.isEmpty()) {
                MATE.log_warn("Couldn't toggle rotation: " + output);
            }
            isInPortraitMode = !isInPortraitMode;
        } catch (IOException e) {
            MATE.log_error("Couldn't change rotation!");
            throw new IllegalStateException(e);
        }
    }

    /**
     * Disables the auto rotation. Rotations don't have any effect if auto rotation is not disabled.
     */
    private void disableAutoRotation() {
        try {
            String output = device.executeShellCommand(DISABLE_AUTO_ROTATION_CMD);
            if (!output.isEmpty()) {
                MATE.log_warn("Couldn't disable auto rotation: " + output);
            }
            disabledAutoRotate = true;
        } catch (IOException e) {
            MATE.log_error("Couldn't disable auto rotation!");
            throw new IllegalStateException(e);
        }
    }

    /**
     * Brings the emulator back into portrait mode.
     */
    public void setPortraitMode() {

        if (!disabledAutoRotate) {
            disableAutoRotation();
        }

        try {
            String output = device.executeShellCommand(PORTRAIT_MODE_CMD);
            if (!output.isEmpty()) {
                MATE.log_warn("Couldn't change to portrait mode: " + output);
            }
            isInPortraitMode = true;
        } catch (IOException e) {
            MATE.log_error("Couldn't change to portrait mode!");
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns whether the emulator is in portrait mode or not.
     *
     * @return Returns {@code true} if the emulator is in portrait mode, otherwise {@code false}
     * is returned.
     */
    public boolean isInPortraitMode() {
        return isInPortraitMode;
    }

    /**
     * Executes a click on the given widget.
     *
     * @param widget The widget on which a click should be performed.
     */
    private void handleClick(Widget widget) {
        device.click(widget.getX(), widget.getY());
    }

    /**
     * Clears the widget's text.
     *
     * @param widget The widget whose input should be cleared.
     */
    private void handleClear(Widget widget) {
        UiObject2 obj = findObject(widget);
        if (obj != null) {
            obj.setText("");
            // reflect change since we cache screen states and findObject() relies on it
            widget.setText("");
        }
    }

    /**
     * Executes a swipe (upon a widget) in a given direction.
     *
     * @param widget    The widget at which position the swipe should be performed.
     * @param direction The direction of the swipe, e.g. swipe to the left.
     */
    private void handleSwipe(Widget widget, ActionType direction) {

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
                device.swipe(X, Y, X, Y - pixelsmove, steps);
                break;
            case SWIPE_UP:
                device.swipe(X, Y, X, Y + pixelsmove, steps);
                break;
            case SWIPE_LEFT:
                device.swipe(X, Y, X + pixelsmove, Y, steps);
                break;
            case SWIPE_RIGHT:
                device.swipe(X, Y, X - pixelsmove, Y, steps);
                break;
        }
    }

    /**
     * Performs a long click on the given widget.
     *
     * @param widget The widget on which a long click should be applied.
     */
    private void handleLongClick(Widget widget) {
        // TODO: consider https://stackoverflow.com/questions/21432561/how-to-achieve-long-click-in-uiautomator
        UiObject2 obj = findObject(widget);
        int X = widget.getX();
        int Y = widget.getY();
        if (obj != null) {
            X = obj.getVisibleBounds().centerX();
            Y = obj.getVisibleBounds().centerY();
        }
        device.swipe(X, Y, X, Y, 120);
    }

    /**
     * Tries to return a ui object matching the given widget. This is a
     * best effort approach.
     *
     * @param widget The widget whose ui object should be looked up.
     * @return Returns the corresponding ui object or {@code null} if no
     * such ui object could be found.
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
     * Handles the insertion of a text in some editable widget.
     *
     * @param action The widget action to be performed.
     */
    private void handleEdit(WidgetAction action) {

        Widget widget = action.getWidget();
        String textData = generateTextData(action);
        MATE.log_debug("Input text: " + textData);
        MATE.log_debug("Previous text: " + widget.getText());

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
            } else {
                MATE.log("  ********* obj " + widget.getId() + "  not found");
            }
        }
    }

    /**
     * Generates a text input for the given editable widget.
     *
     * @param action The widget action containing the editable widget.
     * @return Returns a text input for the editable widget.
     */
    private String generateTextData(WidgetAction action) {
        Widget widget = action.getWidget();
        Random r = new Random();
        double randomNumber = r.nextDouble();
        //TOO
        if (randomNumber < 0) {
            return action.getWidget().getHint();
        }
        int inputType = widget.getInputType();

        //TODO: make maxNumberMutation editable --> maybe a range from 0 - 5?
        String hint = widget.getHint();
       // Log.d("inputType", hint + ":" + inputType);

        String mutation = Mutation.mutateInput(inputType, hint);
        Log.d("inputType",hint +"-->"+mutation);
        return mutation;


        /*
        Widget widget = action.getWidget();

        String widgetText = widget.getText();
        if (widgetText.isEmpty())
            widgetText = widget.getHint();

        // -1 if no limit has been set
        int maxLengthInt = widget.getMaxTextLength();

        // allow a max length of 15 characters
        if (maxLengthInt < 0)
            maxLengthInt = 15;
        if (maxLengthInt > 15)
            maxLengthInt = 15;

        // consider https://developer.android.com/reference/android/text/InputType
        int inputType = widget.getInputType();

        // check whether the input consists solely of digits (ignoring dots and comas)
        widgetText = widgetText.replace(".", "");
        widgetText = widgetText.replace(",", "");

        if (inputType == InputType.TYPE_NULL && !widgetText.isEmpty()
                && android.text.TextUtils.isDigitsOnly(widgetText)) {
            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER;
        }

        // check whether we can derive the input via the content description
        if (inputType == InputType.TYPE_NULL) {
            String desc = widget.getContentDesc();
            if (desc != null) {
                if (desc.contains("email") || desc.contains("e-mail")
                        || desc.contains("E-mail") || desc.contains("Email")) {
                    inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                }
            }
        }

        // assume text input if input type not derivable
        if (inputType == InputType.TYPE_NULL) {
            inputType = InputType.TYPE_CLASS_TEXT;
        }

        return getRandomData(inputType, maxLengthInt);
        */

    }




    /**
     * Generates a random input for the given input type on a best effort basis.
     *
     * @param inputType    The input type, e.g. phone number.
     * @param maxLengthInt The maximal length for the input.
     * @return Returns a random input matching the input type.
     */
    private String getRandomData(int inputType, int maxLengthInt) {

        // TODO: consider the generation of invalid strings, numbers, emails, uris, ...
        DataGenerator dataGen = new DataGenerator();

        switch (inputType) {
            case InputType.TYPE_NUMBER_FLAG_DECIMAL:
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_PHONE:
            case InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL:
                return dataGen.getRandomValidNumber(maxLengthInt);
            case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                return dataGen.getRandomValidEmail(maxLengthInt);
            case InputType.TYPE_TEXT_VARIATION_URI:
                return dataGen.getRandomUri(maxLengthInt);
            case InputType.TYPE_CLASS_TEXT:
                return dataGen.getRandomValidString(maxLengthInt);
            default:
                MATE.log_debug("Input type: " + inputType + " not explicitly supported yet!");
                return dataGen.getRandomValidString(maxLengthInt);
        }
    }

    /**
     * Returns the screen width.
     *
     * @return Returns the screen width in pixels.
     */
    public int getScreenWidth() {
        return device.getDisplayWidth();
    }

    /**
     * Returns the screen height.
     *
     * @return Returns the screen height in pixels.
     */
    public int getScreenHeight() {
        return device.getDisplayHeight();
    }

    /**
     * Doesn't actually re-install the app, solely deletes the app cache.
     */
    public void reinstallApp() {
        MATE.log("Reinstall app");
        Registry.getEnvironmentManager().clearAppData();
    }

    /**
     * Restarts the AUT.
     */
    public void restartApp() {
        MATE.log("Restarting app");
        // Launch the app
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
        // Clear out any previous instances
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } catch (Exception e) {
            e.printStackTrace();
            MATE.log("EXCEPTION CLEARING ACTIVITY FLAG");
        }
        context.startActivity(intent);
    }

    /**
     * Emulates pressing the 'HOME' button.
     */
    public void pressHome() {
        device.pressHome();
    }

    /**
     * Emulates pressing the 'BACK' button.
     */
    public void pressBack() {
        device.pressBack();
    }

    @Deprecated
    public boolean goToState(IGUIModel guiModel, String targetScreenStateId) {
        return new GUIWalker(guiModel, packageName, this).goToState(targetScreenStateId);
    }

}
