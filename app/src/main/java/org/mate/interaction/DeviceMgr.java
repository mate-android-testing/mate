package org.mate.interaction;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exceptions.AUTCrashException;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.intent.IntentBasedAction;
import org.mate.interaction.action.intent.SystemAction;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.PrimitiveAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.state.IScreenState;
import org.mate.utils.Randomness;
import org.mate.utils.StackTrace;
import org.mate.utils.Utils;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.input_generation.DataGenerator;
import org.mate.utils.input_generation.Mutation;
import org.mate.utils.input_generation.StaticStrings;
import org.mate.utils.input_generation.StaticStringsParser;
import org.mate.utils.input_generation.format_types.InputFieldType;
import org.mate.utils.manifest.element.ComponentType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.support.test.InstrumentationRegistry.getContext;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.mate.interaction.action.ui.ActionType.SWIPE_DOWN;
import static org.mate.interaction.action.ui.ActionType.SWIPE_UP;

/**
 * The device manager is responsible for the actual execution of the various actions.
 * Also provides functionality to check for crashes, restart or re-install the AUT, etc.
 */
public class DeviceMgr {

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

    /**
     * Initialises the device manager.
     *
     * @param device The underlying ui device provided by the uiautomator framework.
     * @param packageName The package name of the AUT.
     */
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
        } else if (action instanceof MotifAction) {
            executeAction((MotifAction) action);
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
     * Executes the given motif action.
     *
     * @param action The given motif action.
     * @throws AUTCrashException If the app crashes.
     */
    private void executeAction(MotifAction action) throws AUTCrashException {

        ActionType typeOfAction = action.getActionType();

        switch (typeOfAction) {
            case FILL_FORM_AND_SUBMIT:
                handleFillFormAndSubmit(action);
                break;
            case SPINNER_SCROLLING:
                handleSpinnerScrolling(action);
                break;
            default:
                throw new UnsupportedOperationException("UI action "
                        + action.getActionType() + " not yet supported!");
        }
        checkForCrash();
    }

    /**
     * Executes the motif action 'fill form and click submit' as used in the Sapienz paper.
     *
     * @param action The given motif action.
     */
    private void handleFillFormAndSubmit(MotifAction action) {

        if (Properties.WIDGET_BASED_ACTIONS()) {

            List<UIAction> widgetActions = action.getUIActions();

            for (int i = 0; i < widgetActions.size(); i++) {
                WidgetAction widgetAction = (WidgetAction) widgetActions.get(i);
                if (i < widgetActions.size() - 1) {
                    handleEdit(widgetAction.getWidget());
                } else {
                    // the last widget action represents the click on the submit button
                    handleClick(widgetAction.getWidget());
                }
            }
        } else {

            if (Registry.isReplayMode()) {
                // we simply replay the recorded primitive actions of the motif gene

                List<UIAction> primitiveActions = action.getUIActions();

                for (int i = 0; i < primitiveActions.size(); i++) {
                    PrimitiveAction primitiveAction = (PrimitiveAction) primitiveActions.get(i);
                    if (i < primitiveActions.size() - 1) {
                        handleEdit(primitiveAction);
                    } else {
                        // the last primitive action represents the click on the submit button
                        handleClick(primitiveAction);
                    }
                }
            } else {

                /*
                 * In the case we use primitive actions we stick to a more 'dynamic' approach. Instead of
                 * iterating over fixed widgets, we explore the current screen for all available input
                 * fields and buttons. Then, we fill all input fields and choose a random button for
                 * clicking. Finally, we save the executed actions for a deterministic replaying.
                 */
                IScreenState screenState = Registry.getUiAbstractionLayer().getLastScreenState();
                String currentActivity = screenState.getActivityName();

                List<Widget> inputFields = screenState.getWidgets().stream()
                        .filter(Widget::isEditTextType)
                        .collect(Collectors.toList());

                List<Widget> buttons = screenState.getWidgets().stream()
                        .filter(Widget::isButtonType)
                        .collect(Collectors.toList());

                if (!inputFields.isEmpty() && !buttons.isEmpty()) {

                    // we choose a button randomly on which we finally click
                    Widget button = Randomness.randomElement(buttons);

                    List<UIAction> uiActions = new ArrayList<>();

                    // execute 'type text' actions and save
                    inputFields.stream().forEach(widget -> {
                        PrimitiveAction typeText = new PrimitiveAction(widget.getX(), widget.getY(),
                                ActionType.TYPE_TEXT, currentActivity);
                        handleEdit(typeText);
                        uiActions.add(typeText);
                    });

                    // execute click and save
                    PrimitiveAction click = new PrimitiveAction(button.getX(), button.getY(),
                            ActionType.CLICK, currentActivity);
                    handleClick(click);
                    uiActions.add(click);

                    // record the actions for a possible deterministic replaying
                    action.setUiActions(uiActions);
                }
            }
        }
    }

    /**
     * Finds the corresponding widget to the given ui element if possible. This considers the
     * class name, e.g. android.widget.EditText, the boundaries and the resource name for the
     * comparison.
     *
     * @param uiElement The given ui element.
     * @return Returns the corresponding widget or {@code null} if not possible.
     */
    private Widget findWidget(UiObject2 uiElement) {

        IScreenState screenState = Registry.getUiAbstractionLayer().getLastScreenState();

        for (Widget widget : screenState.getWidgets()) {

            String resourceID = widget.getResourceID().isEmpty() ? null : widget.getResourceID();

            if (widget.getClazz().equals(uiElement.getClassName())
                    && widget.getBounds().equals(uiElement.getVisibleBounds())
                    && Objects.equals(resourceID, uiElement.getResourceName())) {
                    return widget;
            }
        }

        return null;
    }

    /**
     * Performs the spinner scrolling motif action, i.e. one combines the clicking and selecting
     * of another entry in the drop-down menu.
     *
     * @param spinnerWidget The selected spinner.
     * @param selectedWidget The currently selected entry of nested the drop-down menu.
     */
    private void handleSpinnerScrolling(Widget spinnerWidget, Widget selectedWidget) {

        // click on the spinner first to open the drop-down menu
        UiObject2 spinner = findObject(spinnerWidget);

        if (spinner == null) {
            // we fall back to single click mechanism
            MATE.log_warn("Spinner element couldn't be found!");
            handleClick(spinnerWidget);
            return;
        }

        Boolean success = spinner.clickAndWait(Until.newWindow(), 500);

        if (success != null && success) {

            UiObject2 selectedEntry = findObject(selectedWidget);

            if (selectedEntry == null) {
                // we fall back to single click mechanism
                MATE.log_warn("Selected entry of spinner couldn't be found!");
                handleClick(spinnerWidget);
                return;
            }

            // NOTE: We can't re-use the spinner object; it is not valid anymore!
            UiObject2 dropDownMenu = selectedEntry.getParent();

            if (dropDownMenu.getChildren().isEmpty()) {
                // we fall back to single click mechanism
                MATE.log_warn("Spinner without drop-down menu!");
                handleClick(spinnerWidget);
                return;
            }

            /*
             * We need to make a deterministic selection, otherwise when we replay such an action,
             * we may end up in a different state, which may break replay execution. Thus, we
             * simply pick the next entry of the drop-down menu.
             */
            int index = dropDownMenu.getChildren().indexOf(selectedEntry);
            int nextIndex = (index + 1) % dropDownMenu.getChildren().size();
            UiObject2 newSelection = dropDownMenu.getChildren().get(nextIndex);

            // click on new entry in order to select it
            newSelection.click();
        }
    }

    /**
     * Performs a scrolling action on a spinner, i.e. one combines the clicking on the spinner to
     * open the drop-down menu (list view) and the selection of a (different) entry from the
     * drop-down menu.
     *
     * @param action The given motif action.
     */
    private void handleSpinnerScrolling(MotifAction action) {

        if (Properties.WIDGET_BASED_ACTIONS()) {

            WidgetAction widgetAction = (WidgetAction) action.getUIActions().get(0);

            // retrieve the spinner widget and the selected entry of the dropdown-menu
            Widget spinnerWidget = widgetAction.getWidget();
            Widget selectedWidget = spinnerWidget.getChildren().get(0);

            handleSpinnerScrolling(spinnerWidget, selectedWidget);
        } else {

            IScreenState screenState = Registry.getUiAbstractionLayer().getLastScreenState();

            if (Registry.isReplayMode()) {

                /*
                * It is possible that the spinner action wasn't actually executed at record time,
                * because there was no spinner available. In this case, we can't do anything else
                * than simply ignoring the action.
                 */
                if (!action.getUIActions().isEmpty()) {

                    // retrieve the recorded spinner
                    PrimitiveAction spinnerClickAction = (PrimitiveAction) action.getUIActions().get(0);
                    Optional<Widget> spinner = screenState.getWidgets().stream()
                            .filter(Widget::isClickable)
                            .filter(Widget::isSpinnerType)
                            .filter(widget -> widget.getX() == spinnerClickAction.getX())
                            .filter(widget -> widget.getY() == spinnerClickAction.getY())
                            .findAny();

                    if (spinner.isPresent()) {
                        Widget spinnerWidget = spinner.get();
                        Widget selectedWidget = spinnerWidget.getChildren().get(0);
                        handleSpinnerScrolling(spinnerWidget, selectedWidget);
                    } else {
                        MATE.log_warn("Couldn't locate spinner at location ("
                                + spinnerClickAction.getX() + "," + spinnerClickAction.getY() + ")!");
                    }
                }
            } else {

                /*
                 * If we deal with primitive actions, then we elect a random spinner widget of
                 * the current screen. In addition, we need to record the executed actions in order to
                 * make replaying deterministic.
                 */
                List<Widget> spinners = screenState.getWidgets().stream()
                        .filter(Widget::isClickable)
                        .filter(Widget::isSpinnerType)
                        .collect(Collectors.toList());

                /*
                * If no spinner is available on the current screen, we simply do nothing alike
                * a primitive action may have no effect, e.g. a click on a random coordinate which
                * area is not covered by any clickable widget.
                 */
                if (!spinners.isEmpty()) {

                    // pick a random spinner and retrieve the selected drop-down menu entry
                    Widget spinnerWidget = Randomness.randomElement(spinners);
                    Widget selectedWidget = spinnerWidget.getChildren().get(0);

                    handleSpinnerScrolling(spinnerWidget, selectedWidget);

                    PrimitiveAction spinnerClick = new PrimitiveAction(spinnerWidget.getX(),
                            spinnerWidget.getY(), ActionType.CLICK, screenState.getActivityName());
                    action.setUiActions(Collections.singletonList(spinnerClick));
                }
            }
        }
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
    }

    /**
     * Performs a click based on the coordinates of the given action.
     *
     * @param action The given primitive action.
     */
    private void handleClick(PrimitiveAction action) {
        device.click(action.getX(), action.getY());
    }

    /**
     * Inserts a text into a input field based on the given primitive action.
     *
     * @param action The given primitive action.
     */
    private void handleEdit(PrimitiveAction action) {

        // clicking on the screen should get a focus on the underlying 'widget'
        device.click(action.getX(), action.getY());
        UiObject2 uiElement = device.findObject(By.focused(true));

        if (uiElement != null) {

            Widget widget = null;

            try {
                /*
                 * We resort here to the underlying widget, otherwise we have no idea what type of
                 * text should be generated as input.
                 */
                widget = findWidget(uiElement);
            } catch (StaleObjectException e) {

                MATE.log_warn("Stale ui element!");
                e.printStackTrace();

                /*
                * Unfortunately, it can happen that the requested ui element gets immediately stale.
                * The only way to recover from such a situation is to call findObject() another time.
                 */
                uiElement = device.findObject(By.focused(true));
                if (uiElement != null) {
                    widget = findWidget(uiElement);
                }
            }

            if (widget != null && widget.isEditTextType()) {

                /*
                * If we run in replay mode, we should use the recorded text instead of a new text
                * that is randomly created. Otherwise, we may end up in a different state and
                * subsequent actions might not show the same behaviour as in the recorded run.
                 */
                String textData = Registry.isReplayMode() ? action.getText() :
                        Objects.toString(generateTextData(widget, widget.getMaxTextLength()), "");

                MATE.log_debug("Inserting text: " + textData);
                uiElement.setText(textData);

                // record for possible replaying + findObject() relies on it
                action.setText(textData);
                widget.setText(textData);
            }

            // we need to close the soft keyboard, but only if it is present, see:
            // https://stackoverflow.com/questions/17223305/suppress-keyboard-after-setting-text-with-android-uiautomator
            device.pressBack();
        }
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
                handleEdit(selectedWidget);
                break;
            case TYPE_SPECIFIC_TEXT:
                handleEdit(selectedWidget);
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
     *         is returned.
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
     *         otherwise {@code false} is returned.
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
     *         is returned.
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
     * @param widget The widget at which position the swipe should be performed.
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
     * Handles the insertion of a text in the given editable widget.
     *
     * @param widget The editable widget.
     */
    private void handleEdit(Widget widget) {

        /*
         * If we run in replay mode, we should insert the text that we recorded, otherwise we may
         * break execution, since a different (valid) input may lead to a different state, e.g. we
         * end on a different activity and all subsequent widget actions are not applicable anymore.
         */
        String textData = Registry.isReplayMode() ? widget.getText() :
                Objects.toString(generateTextData(widget, widget.getMaxTextLength()), "");

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

                // we need to close the soft keyboard, but only if it is present, see:
                // https://stackoverflow.com/questions/17223305/suppress-keyboard-after-setting-text-with-android-uiautomator
                device.pressBack();
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

        if (!className.contains("/")) {
            // the class name is already in its desired form
            return className;
        }

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
     * @param widget The editable widget.
     * @param maxLength The maximal input length.
     * @return Returns a text input for the editable widget.
     */
    private String generateTextData(final Widget widget, final int maxLength) {

        final String activityName = widget.getActivity();

        final InputFieldType inputFieldType = InputFieldType.getFieldTypeByNumber(widget.getInputType());
        final Random random = Registry.getRandom();

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
                uiComponents.addAll(getCurrentFragments());

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
        Context context = getContext();
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
                return convertClassName(getCurrentActivityAPI25());
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                return convertClassName(getCurrentActivityAPI28());
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                return convertClassName(getCurrentActivityAPI29());
            } else {
                // fall back mechanism (slow)
                return convertClassName(Registry.getEnvironmentManager().getCurrentActivityName());
            }
        } catch (Exception e) {
            MATE.log_warn("Couldn't retrieve current activity name via local shell!");
            MATE.log_warn(e.getMessage());

            // fall back mechanism (slow)
            return convertClassName(Registry.getEnvironmentManager().getCurrentActivityName());
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
     * Returns the name of the current activity on an emulator running API 29.
     *
     * @return Returns the current activity name.
     */
    private String getCurrentActivityAPI29() throws IOException {
        String output = device.executeShellCommand("dumpsys activity activities");
        return output.split("mResumedActivity")[1].split("\n")[0].split(" ")[3];
    }

    /**
     * Returns the currently visible fragments.
     *
     * @return Returns the currently visible fragments.
     */
    private List<String> getCurrentFragments() {

        // https://stackoverflow.com/questions/24429049/get-info-of-current-visible-fragments-in-android-dumpsys
        try {
            String output = device.executeShellCommand("dumpsys activity " + getCurrentActivity());
            List<String> fragments = extractFragments(output);
            MATE.log_debug("Currently active fragments: " + fragments);
            return fragments;
        } catch (Exception e) {
            MATE.log_warn("Couldn't retrieve currently active fragments: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Extracts the visible fragments from the given output.
     *
     * @param output The output of the command 'dumpsys activity <activity-name>'.
     * @return Returns the visible fragments.
     */
    private List<String> extractFragments(String output) {

        /*
        * A typical output of the command 'dumpsys activity <activity-name>' looks as follows:
        *
        *  Local FragmentActivity 30388ff State:
        *     Added Fragments:
        *       #0: MyFragment{b4f3bc} (642de726-ae2d-439c-a047-4a4a35a6f435 id=0x7f080071)
        *       #1: MySecondFragment{a918ac1} (12f5630f-b93c-40c8-a9fa-49b74745678a id=0x7f080071)
        *     Back Stack Index: 0 (this line seems to be optional!)
        *     FragmentManager misc state:
        */

        final String fragmentActivityState = output.split("Local FragmentActivity")[1];
        
        // If no fragment is visible, the 'Added Fragments:' line is missing!
        if (!fragmentActivityState.contains("Added Fragments:")) {
            return Collections.emptyList();
        }

        final String[] fragmentLines = fragmentActivityState
                .split("Added Fragments:")[1]
                .split("FragmentManager")[0]
                .split("Back Stack Index:")[0] // this line is not always present
                .split(System.lineSeparator());

        return Arrays.stream(fragmentLines)
                .filter(line -> !line.replaceAll("\\s+","").isEmpty())
                .map(line -> line.split(":")[1])
                .map(line -> line.split("\\{")[0])
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Grants the AUT the read and write runtime permissions for the external storage.
     * <p>
     * Depending on the API level, we can either use the very fast method grantRuntimePermissions()
     * (API >= 28) or the slow routine executeShellCommand().
     * <p>
     * In order to verify that the runtime permissions got granted, check the output of the
     * following command:
     * device.executeShellCommand("dumpsys package " + packageName);
     *
     * @return Returns {@code true} when operation succeeded, otherwise {@code false} is returned.
     */
    public boolean grantRuntimePermissions() {

        Instrumentation instrumentation = getInstrumentation();

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
            throw new IllegalStateException("Couldn't grant runtime permissions!", e);
        }
    }

    /**
     * Returns the activities of the AUT.
     *
     * @return Returns the activities of the AUT.
     */
    public List<String> getActivities() {

        Instrumentation instrumentation = getInstrumentation();

        try {
            // see: https://stackoverflow.com/questions/23671165/get-all-activities-by-using-package-name
            PackageInfo pi = instrumentation.getTargetContext().getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_ACTIVITIES);

            // TODO: Ensure that the short form '/.subpackage.activityName' is not used!!!
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
                device.executeShellCommand("run-as " + packageName + " touch files/coverage.exec");
                // device.executeShellCommand("run-as " + packageName + " exit");
            }

            if (Properties.SURROGATE_MODEL()) {
                /*
                * The execution of the 'pm clear' command also drops the runtime permissions of the
                * AUT, thus we have to re-grant it in order to write the traces properly.
                 */
                MATE.log("Granting runtime permissions: " + grantRuntimePermissions());
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
    public StackTrace getLastCrashStackTrace() {

        try {
            String response = device.executeShellCommand("run-as " + packageName
                    + " logcat -b crash -t 2000 AndroidRuntime:E *:S");

            List<String> lines = Arrays.asList(response.split("\n"));

            // traverse the stack trace from bottom up until we reach the beginning
            for (int i = lines.size() - 1; i >= 0; i--) {
                if (lines.get(i).contains("E AndroidRuntime: FATAL EXCEPTION: ")) {
                    return new StackTrace(lines.subList(i, lines.size()).stream()
                            .collect(Collectors.joining("\n")));
                }
            }

        } catch(IOException e) {
            MATE.log_warn("Couldn't retrieve stack trace of last crash!");
            MATE.log_warn(e.getMessage());
        }

        // fallback mechanism
        return new StackTrace(Registry.getEnvironmentManager().getLastCrashStackTrace());
    }

    /**
     * Sends a broadcast to the tracer, which in turn dumps the collected traces to a file on
     * the external storage.
     */
    private void sendBroadcastToTracer() {
        Intent intent = new Intent("STORE_TRACES");
        intent.setComponent(new ComponentName(Registry.getPackageName(),
                "de.uni_passau.fim.auermich.tracer.Tracer"));
        InstrumentationRegistry.getTargetContext().sendBroadcast(intent);
    }

    /**
     * Reads the traces from the external memory and deletes afterwards the traces file.
     *
     * @return Returns the set of traces.
     *
     */
    public Set<String> getTraces() {

        // triggers the dumping of traces to a file called traces.txt
        sendBroadcastToTracer();

        File sdCard = Environment.getExternalStorageDirectory();
        File infoFile = new File(sdCard, "info.txt");

        /*
        * We need to wait until the info.txt file is generated, once it is there, we know that all
        * traces have been dumped.
         */
        while(!infoFile.exists()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Waiting for info.txt failed!", e);
            }
        }

        File traceFile = new File(sdCard, "traces.txt");

        if (!traceFile.exists()) {
            throw new IllegalStateException("The file traces.txt doesn't exist!");
        }

        Set<String> traces = new HashSet<>();

        try (BufferedReader reader
                     = new BufferedReader(new InputStreamReader(new FileInputStream(traceFile)))) {

            String line = reader.readLine();

            while (line != null) {
                traces.add(line);
                line = reader.readLine();
            }

        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read traces!", e);
        }

        // remove both files
        traceFile.delete();
        infoFile.delete();

        return traces;
    }

    /**
     * Stores the traces to a file called traces.txt on the external memory.
     *
     * @param traces The traces to be stored.
     */
    public void storeTraces(Set<String> traces) {

        File sdCard = Environment.getExternalStorageDirectory();
        File traceFile = new File(sdCard, "traces.txt");

        try (Writer fileWriter = new FileWriter(traceFile)) {

            for(String trace : traces) {
                fileWriter.write(trace);
                fileWriter.write(System.lineSeparator());
            }

            fileWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write to traces.txt!", e);
        }

        /*
        * The info.txt indicates that the dumping of traces has been completed and it contains
        * the number of written traces.
         */
        File infoFile = new File(sdCard, "info.txt");

        try (Writer fileWriter = new FileWriter(infoFile)) {
            fileWriter.write(traces.size());
            fileWriter.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't write to info.txt!", e);
        }
    }
}
