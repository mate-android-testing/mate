package org.mate.interaction;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiSelector;
import android.text.InputType;

import org.mate.MATE;
import org.mate.Properties;
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
import org.mate.utils.coverage.Coverage;
import org.mate.utils.input_generation.InputFieldType;
import org.mate.utils.input_generation.Mutation;
import org.mate.utils.input_generation.StaticStrings;
import org.mate.utils.input_generation.StaticStringsParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static org.mate.interaction.action.ui.ActionType.SWIPE_DOWN;
import static org.mate.interaction.action.ui.ActionType.SWIPE_UP;

/**
 * The device manager is responsible for the actual execution of the various actions.
 * Also provides functionality to check for crashes, restart or re-install the AUT, etc.
 */
public class DeviceMgr {

    public static final double PROB_HINT = 0.5;
    public static final double PROB_HINT_MUTATION = 0.5;
    public static final double PROB_STATIC_STRING = 0.5;
    public static final double PROB_STATIC_STRING_MUTATION = 0.25;
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

    /**
     * Contains the static strings extracted from the byte code.
     */
    private final StaticStrings staticStrings;

    public DeviceMgr(UiDevice device, String packageName) {
        this.device = device;
        this.packageName = packageName;
        this.isInPortraitMode = true;
        this.disabledAutoRotate = false;
        this.staticStrings = StaticStringsParser.parseStaticStrings();
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
     * @param action The system event.
     */
    private void executeAction(SystemAction action) throws AUTCrashException {

        // the inner class separator '$' needs to be escaped
        String receiver = action.getReceiver().replaceAll("\\$", Matcher.quoteReplacement("\\$"));

        String tag;
        String component;

        if (action.isDynamicReceiver()) {
            /*
             * In the case we deal with a dynamic receiver, we can't specify the full component name,
             * since dynamic receivers can't be triggered by explicit intents! Instead, we can only
             * specify the package name in order to limit the receivers of the broadcast.
             */
            tag = "-p";
            component = packageName;
        } else {
            tag = "-n";
            component = packageName + "/" + receiver;
        }

        try {
            device.executeShellCommand("su root am broadcast -a " + action.getAction()
                    + " " + tag + " " + component);
        } catch (IOException e) {
            MATE.log_warn("Executing system action failed!");
            MATE.log_warn(e.getMessage());

            // fall back mechanism
            Registry.getEnvironmentManager().executeSystemEvent(Registry.getPackageName(),
                    action.getReceiver(), action.getAction(), action.isDynamicReceiver());
        }
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
        } finally {
            /*
             * After the rotation it takes some time that the device gets back in a stable state.
             * If we proceed too fast, the UIAutomator loses its connection. Thus, we insert a
             * minimal waiting time to avoid this problem.
             */
            Utils.sleep(100);
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
        if (textData == null) {
            MATE.log_warn("No text data were produced! Should never happen!");
            textData = "";
        } else {
            MATE.log_debug("Input text: " + textData);
            MATE.log_debug("Previous text: " + widget.getText());
        }

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
     * Generates a text input for the given editable widget.
     *
     * @param action The widget action containing the editable widget.
     * @return Returns a text input for the editable widget.
     */
    private String generateTextData(WidgetAction action) {

        Widget widget = action.getWidget();

        // TODO: we always look at the activity name, but a fragment might be actually displayed on top
        //  -> in this case we should the collected string constants from the fragment probably
        String className = convertClassName(widget.getActivity());

        InputFieldType type = InputFieldType.getFieldTypeByNumber(widget.getInputType());
        Random r = Registry.getRandom();

        // A hint should be present for the given widget. If it is, we check if the hint is a valid
        // input param and use the probability PROB_HINT to select the hint as the input string.
        if (widget.getHint() != null && !widget.getHint().isEmpty()) {
            if (type.isValid(widget.getHint()) && r.nextDouble() < PROB_HINT) {

                // We first consider the nature of the input field at hand. If we cannot assign this
                // unambiguously, we enter the hint without changing it. If it is assignable, we
                // will mutate it with the probability PROB_HINT_MUTATION.
                if (type == InputFieldType.NOTHING || r.nextDouble() < PROB_HINT_MUTATION) {
                    return action.getWidget().getHint();
                } else {
                    return Mutation.mutateInput(type, widget.getHint());
                }
            }
        }

        // If the static strings were successfully parsed before, then we use the probability
        // PROB_STATIC_STRING to fetch a random static string that matches for the input field type
        // and the current class name.
        if (staticStrings.isPresent()) {
            if (r.nextDouble() < PROB_STATIC_STRING) {
                String randomStaticString;
                if (type != InputFieldType.NOTHING) {
                    randomStaticString = staticStrings.getRandomStringFor(type, className);

                    // If there is no matching string for these properties, we reduce the
                    // requirements and search all classes for a matching string for this input
                    // field type.
                    if (randomStaticString == null) {
                        randomStaticString = staticStrings.getRandomStringFor(type);
                    }

                    // If a string was found in the previous stations, we can still mutate it with
                    // the probability PROB_STATIC_STRING_MUTATION.
                    if (randomStaticString != null) {
                        if (r.nextDouble() < PROB_STATIC_STRING_MUTATION) {
                            randomStaticString = Mutation.mutateInput(type, randomStaticString);
                        }
                        return randomStaticString;
                    }
                }

                // If we have an input field type that is not further defined, or if the attempts
                // before were unsuccessful, we try a random string for the current class name. If
                // this does not work, we cannot include the static strings in the input generation.
                randomStaticString = staticStrings.getRandomStringFor(className);
                if (randomStaticString != null) {
                    return randomStaticString;
                }
            }
        }
        //TODO: Return strings of example file as fall back mechanism. Or another approach?!
        // Nevertheless a fall back mechanism is needed.
        return "DummyString";
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
     * Doesn't actually re-install the app, solely deletes the app's internal storage.
     */
    public void reinstallApp() {
        MATE.log("Reinstall app");
        clearApp();
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

    /**
     * Retrieves the name of the currently visible activity.
     *
     * @return Returns the name of the currently visible activity.
     */
    public String getCurrentActivity() {

        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) {
                return getCurrentActivityAPI25();
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                return getCurrentActivityAPI28();
            } else {
                // fall back mechanism (slow)
                return Registry.getEnvironmentManager().getCurrentActivityName();
            }
        } catch (Exception e) {
            MATE.log_warn("Couldn't retrieve current activity name via local shell!");
            MATE.log_warn(e.getMessage());

            // fall back mechanism (slow)
            return Registry.getEnvironmentManager().getCurrentActivityName();
        }
    }

    /**
     * Returns the name of the current activity on an emulator running API 25.
     *
     * @return Returns the current activity name.
     */
    private String getCurrentActivityAPI25() throws IOException {
        String output = device.executeShellCommand("dumpsys activity top");
        return output.split("\n")[1].split(" ")[3];
    }

    /**
     * Returns the name of the current activity on an emulator running API 28.
     *
     * @return Returns the current activity name.
     */
    private String getCurrentActivityAPI28() throws IOException {
        String output = device.executeShellCommand("dumpsys activity activities");
        return output.split("mResumedActivity")[1].split("\n")[0].split(" ")[3];
    }

    /**
     * Grants the AUT the read and write runtime permissions for the external storage.
     * <p>
     * Depending on the API level, we can either use the very fast method grantRuntimePermissions()
     * (API >= 28) or the slow routine executeShellCommand(). Right now, however, we let the
     * MATE-Server granting those permissions directly before executing a privileged command in
     * order to avoid unnecessary requests.
     * <p>
     * In order to verify that the runtime permissions got granted, check the output of the
     * following command:
     * device.executeShellCommand("dumpsys package " + packageName);
     *
     * @return Returns {@code true} when operation succeeded, otherwise {@code false} is returned.
     */
    @SuppressWarnings("unused")
    public boolean grantRuntimePermissions() {

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        final String readPermission = "android.permission.READ_EXTERNAL_STORAGE";
        final String writePermission = "android.permission.WRITE_EXTERNAL_STORAGE";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            instrumentation.getUiAutomation().grantRuntimePermission(packageName, readPermission);
            instrumentation.getUiAutomation().grantRuntimePermission(packageName, writePermission);
            return true;
        }

        try {
            /*
             * The operation executeShellCommand() is costly, but unfortunately it is not possible
             * to concatenate two commands yet.
             */
            final String grantedReadPermission
                    = device.executeShellCommand("pm grant " + packageName + " " + readPermission);
            final String grantedWritePermission
                    = device.executeShellCommand("pm grant " + packageName + " " + writePermission);

            // an empty response indicates success of the operation
            return grantedReadPermission.isEmpty() && grantedWritePermission.isEmpty();
        } catch (IOException e) {
            MATE.log_error("Couldn't grant runtime permissions!");
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the activity names of the AUT.
     *
     * @return Returns the activity names of the AUT.
     */
    public List<String> getActivityNames() {

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        try {
            // see: https://stackoverflow.com/questions/23671165/get-all-activities-by-using-package-name
            PackageInfo pi = instrumentation.getTargetContext().getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_ACTIVITIES);

            return Arrays.stream(pi.activities).map(activity -> activity.name)
                    .collect(Collectors.toList());
        } catch (PackageManager.NameNotFoundException e) {
            MATE.log_warn("Couldn't retrieve activity names!");
            MATE.log_warn(e.getMessage());

            // fallback mechanism
            return Registry.getEnvironmentManager().getActivityNames();
        }
    }

    /**
     * Clears the files contained in the app-internal storage, i.e. the app is reset to its
     * original state.
     */
    public void clearApp() {

        try {

            device.executeShellCommand("pm clear " + packageName);

            /*
             * We need to re-generate an empty 'coverage.exec' file for those apps that have been
             * manually instrumented with Jacoco, otherwise the apps keep crashing.
             * TODO: Is the final call to 'exit' really necessary?
             */
            if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
                device.executeShellCommand("run-as " + packageName + " mkdir -p files");
                device.executeShellCommand("run-as " + packageName + " touch files/coverage.exe");
                // device.executeShellCommand("run-as " + packageName + " exit");
            }

        } catch (IOException e) {
            MATE.log_warn("Couldn't clear app data!");
            MATE.log_warn(e.getMessage());

            // fallback mechanism
            Registry.getEnvironmentManager().clearAppData();
        }
    }

    /**
     * Retrieves the stack trace of the last discovered crash.
     *
     * @return Returns the stack trace of the last crash.
     */
    public String getLastCrashStackTrace() {

        try {
            String response = device.executeShellCommand("run-as " + packageName
                    + " logcat -b crash -t 2000 AndroidRuntime:E *:S");

            List<String> lines = Arrays.asList(response.split("\n"));

            // traverse the stack trace from bottom up until we reach the beginning
            for (int i = lines.size() - 1; i >= 0; i--) {
                if (lines.get(i).contains("E AndroidRuntime: FATAL EXCEPTION: ")) {
                    return lines.subList(i, lines.size()).stream()
                            .collect(Collectors.joining("\n"));
                }
            }

        } catch (IOException e) {
            MATE.log_warn("Couldn't retrieve stack trace of last crash!");
            MATE.log_warn(e.getMessage());
        }

        // fallback mechanism
        return Registry.getEnvironmentManager().getLastCrashStackTrace();
    }

    @Deprecated
    public boolean goToState(IGUIModel guiModel, String targetScreenStateId) {
        return new GUIWalker(guiModel, packageName, this).goToState(targetScreenStateId);
    }

}
