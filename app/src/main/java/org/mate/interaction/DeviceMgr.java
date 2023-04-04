package org.mate.interaction;

import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
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
import android.view.accessibility.AccessibilityWindowInfo;

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
import org.mate.state.ScreenStateFactory;
import org.mate.state.ScreenStateType;
import org.mate.utils.MateInterruptedException;
import org.mate.utils.Randomness;
import org.mate.utils.StackTrace;
import org.mate.utils.UIAutomatorException;
import org.mate.utils.Utils;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.input_generation.DataGenerator;
import org.mate.utils.input_generation.Mutation;
import org.mate.utils.input_generation.StaticStrings;
import org.mate.utils.input_generation.StaticStringsParser;
import org.mate.utils.input_generation.format_types.InputFieldType;
import org.mate.utils.manifest.element.ComponentDescription;
import org.mate.utils.manifest.element.ComponentType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
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
     * The probability for considering a whitespace as input.
     */
    private static final double PROB_WHITESPACE = 0.1;

    /**
     * The probability for mutating a given hint.
     */
    private static final double PROB_HINT_MUTATION = 0.5;

    /**
     * The probability for using a static string or the input generation.
     */
    private static final double PROB_STATIC_STRING = 0.5;

    /**
     * The probability for using a stack trace token as input.
     */
    private static final double PROB_STACK_TRACE_USER_INPUT = 0.8;

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
     * The error message when the ui automator is disconnected.
     */
    private static final String UiAutomatorDisconnectedMessage = "UiAutomation not connected!";

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

        try {
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
                        + action.getClass().getSimpleName() + " not yet supported!");
            }
        } catch (IllegalStateException e) {
            MATE.log_debug("Couldn't execute action: " + action);
            e.printStackTrace();
            if (Objects.equals(e.getMessage(), UiAutomatorDisconnectedMessage)) {
                throw new UIAutomatorException("UIAutomator disconnected, couldn't execute action!");
            } else {
                // unexpected behaviour
                throw e;
            }
        }

        checkForCrash();
    }

    /**
     * Simulates the occurrence of a system event.
     *
     * @param action The system event.
     */
    private void executeAction(SystemAction action) {

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
    }

    /**
     * Executes the given motif action.
     *
     * @param action The given motif action.
     */
    private void executeAction(MotifAction action) {

        ActionType typeOfAction = action.getActionType();

        switch (typeOfAction) {
            case FILL_FORM_AND_SUBMIT:
                handleFillFormAndSubmit(action);
                break;
            case FILL_FORM:
                handleFillForm(action);
                break;
            case FILL_FORM_AND_SCROLL:
                handleFillFormAndScroll(action);
                break;
            case SPINNER_SCROLLING:
                handleSpinnerScrolling(action);
                break;
            case MENU_CLICK_AND_ITEM_SELECTION:
                handleMenuClickAndItemSelection(action);
                break;
            case SORT_MENU_CLICK_AND_SORT_ORDER_SELECTION:
                handleSortMenuClickAndSortOrderSelection(action);
                break;
            case OPEN_NAVIGATION_AND_OPTION_SELECTION:
                handleOpenNavigationAndOptionSelection(action);
                break;
            case TYPE_TEXT_AND_PRESS_ENTER:
                handleTypeTextAndPressEnter(action);
                break;
            case CHANGE_RADIO_GROUP_SELECTIONS:
                handleChangeRadioGroupSelections(action);
                break;
            case CHANGE_LIST_VIEW_SELECTION:
                handleChangeListViewSelection(action);
                break;
            case CHANGE_SEEK_BARS:
                handleChangeSeekBars(action);
                break;
            case CHANGE_DATE:
                handleChangeDate(action);
                break;
            case CHANGE_CHECKABLES:
                handleChangeCheckables(action);
                break;
            case SWAP_LIST_ITEMS:
                handleSwapListItems(action);
                break;
            default:
                throw new UnsupportedOperationException("UI action "
                        + action.getActionType() + " not yet supported!");
        }
    }

    /**
     * Executes the 'swap list items' motif action, i.e. two arbitrary list items are swapped.
     *
     * @param action The given motif action.
     */
    private void handleSwapListItems(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            // TODO: Select two arbitrary list items that should be swapped.
            final Widget source = action.getWidgets().get(0);
            final Widget target = action.getWidgets().get(1);

            // TODO: Fix target y-coordinate if swapping from bottom to top.
            device.drag(source.getX(), source.getY(), target.getX(), target.getY2(), 100);

        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'fill form and scroll' motif action, i.e. a scrollable form is filled out.
     *
     * @param action The given motif action.
     */
    private void handleFillFormAndScroll(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            final WidgetAction scrollAction = (WidgetAction) action.getUIActions().get(0);

            IScreenState screenState
                    = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

            boolean change = true;

            // TODO: Remove this constraint if a change of the screen state can be reliably detected
            //  and there are no changes caused by filling out the edit text fields (endless loop).
            final int maxSwipes = 5;
            int swipes = 0;

            while (change && swipes < maxSwipes) {

                // fill out the currently visible edit text fields
                final List<Widget> editTextWidgets = screenState.getWidgets().stream()
                        .filter(Widget::isEditTextType)
                        .collect(Collectors.toList());

                editTextWidgets.stream().forEach(this::handleEdit);

                // TODO: Toggle checkboxes, radio buttons, switches, etc.

                // scroll down
                handleSwipe(scrollAction.getWidget(), scrollAction.getActionType());

                Utils.sleep(300);

                IScreenState newScreenState
                        = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

                // TODO: Ensure that the equals() check is actually comparing the widgets.
                change = !screenState.equals(newScreenState);
                swipes++;
                screenState = newScreenState;
            }

            // TODO: Click on a submit/save button if present.

        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'change checkables' motif action, i.e. multiple checkables are changed at once.
     *
     * @param action The given motif action.
     */
    private void handleChangeCheckables(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            action.getUIActions().stream().forEach(checkableAction -> {
                // Only check/uncheck with a probability of 1/2 to enable different combinations.
                if (Randomness.getRnd().nextDouble() < 0.5) {
                    handleClick(((WidgetAction) checkableAction).getWidget());
                }
            });
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'change date' motif action, i.e. a date is selected from a date picker.
     *
     * @param action The given motif action.
     */
    private void handleChangeDate(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            // TODO: Enable changing the year by clicking on the year text view at the top.
            // TODO: Enable the selection of an arbitrary date of the current year.

            // the click action on the previous week/month/year image button
            final WidgetAction prev = (WidgetAction) action.getUIActions().get(0);

            // the click action on the next week/month/year image button
            final WidgetAction next = (WidgetAction) action.getUIActions().get(1);

            final double rnd = Randomness.getRnd().nextDouble();

            if (rnd <= 0.33) {
                // pick date after clicking on previous week/month/year
                handleClick(prev.getWidget());
                Utils.sleep(300); // sleep a while to get a stable screen state returned
            } else if (rnd <= 0.66) {
                // pick date after clicking on next week/month/year
                handleClick(next.getWidget());
                Utils.sleep(300); // sleep a while to get a stable screen state returned
            } // else pick date on current week/month/year

            final IScreenState screenState
                    = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

            /*
            * NOTE: Unfortunately here the UIAutomator API has major problems to return only those
            * widgets that represent selectable (i.e. visible) dates. Without the bounds check on
            * the view pager, the list of dates also includes dates from the previous/next month
            * that are in no way selectable. Also checking for existence of the bare ui object
            * exhibits the same limitation, hence we opted for the bounds checking approach. The
            * dates of the previous month have negative x-coordinates while the dates of the next
            * month have x-coordinates that are outside of view pager's visible area.
             */
            final Widget viewPager = screenState.getWidgets().stream()
                    .filter(widget ->
                            widget.getClazz().equals("com.android.internal.widget.ViewPager"))
                    .findFirst().orElse(null);

            if (viewPager != null) {

                final List<Widget> dates = screenState.getWidgets().stream()
                        .filter(Widget::isLeafWidget)
                        .filter(widget -> widget.getClazz().equals("android.view.View"))
                        .filter(widget -> widget.isSonOf(parent -> parent.equals(viewPager)))
                        .filter(widget -> viewPager.getBounds().contains(widget.getBounds()))
                        .collect(Collectors.toList());

                // pick a random date
                final Widget date = Randomness.randomElement(dates);
                handleClick(date);

                // click on OK button
                screenState.getWidgets().stream()
                        .filter(Widget::isButtonType)
                        .filter(widget -> widget.getText().equalsIgnoreCase("OK")
                                || widget.getResourceID().equals("android:id/button1"))
                        .findFirst().ifPresent(this::handleClick);
            } else {
                MATE.log_warn("ViewPager couldn't be located on date picker!");
            }
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'change seek bars' motif action, i.e. multiple seek bars are changed at once.
     *
     * @param action The given motif action.
     */
    private void handleChangeSeekBars(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {
            action.getUIActions().stream().forEach(changeSeekBarAction ->
                    handleChangeSeekBar(((WidgetAction) changeSeekBarAction).getWidget()));
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'change list view selection' motif action, i.e. a click on a random list view
     * item is performed.
     *
     * @param action The given motif action.
     */
    private void handleChangeListViewSelection(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            // TODO: Pick a list view item that hasn't been selected so far or pick random otherwise.
            final WidgetAction clickAction
                    = (WidgetAction) Randomness.randomElement(action.getUIActions());
            handleClick(clickAction.getWidget());
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'type text and press enter' motif action, i.e. an edit text widget is filled
     * with some content and then enter is pressed.
     *
     * @param action The given motif action.
     */
    private void handleTypeTextAndPressEnter(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            // type some arbitrary text and press enter afterwards
            final WidgetAction typeTextAction = (WidgetAction) action.getUIActions().get(0);
            handleEdit(typeTextAction.getWidget());
            device.pressEnter();
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'open sort menu and sort order selection' motif action, i.e. first the sort menu
     * is opened by clicking on the sort symbol and then a possible different sort order is selected
     * by clicking on it.
     *
     * @param action The given motif action.
     */
    private void handleOpenNavigationAndOptionSelection(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            // click on the navigation menu to open the list of options
            final WidgetAction navigationMenuClickAction = (WidgetAction) action.getUIActions().get(0);
            handleClick(navigationMenuClickAction.getWidget());

            /*
             * TODO: We encountered a strange situation where the fetched screen state contained
             *  essentially the displayed widgets but with malformed coordinates, which led to
             *  clicking above the chosen menu item. We believe this is related to a sync issue of
             *  UIAutomator. A small waiting time seems to remedy the issue, but it is not clear
             *  whether such hand-crafted waiting time works across apps. We could resort to
             *  UIDevice#waitForIdle(); but the idle time varies largely between two consecutive calls.
             */
            Utils.sleep(200);

            // Fetch the new screen state containing the list view with the options.
            final IScreenState screenState
                    = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

            // Check whether there is a drawer layout used for the navigation.
            final Widget drawerLayout = screenState.getWidgets().stream()
                    .filter(Widget::isDrawerLayout)
                    .findAny()
                    .orElse(null);

            final Predicate<Widget> isNavigationWidget = widget -> {

                if (drawerLayout == null || drawerLayout.getChildren().size() <= 1) {
                    return true;
                }

                // Although there is a convention that the first child should refer to the main content
                // view and the second child to the navigation menu, this rule is not followed by all
                // apps. To circumvent this problem, we consider the size of the widgets and assume
                // that the smaller widget in width refers to the navigation menu.
                final Widget firstChild = drawerLayout.getChildren().get(0);
                final Widget secondChild = drawerLayout.getChildren().get(1);
                final Widget navigationElementContainer = firstChild.getWidth() < secondChild.getWidth()
                        ? firstChild : secondChild;
                return widget.isSonOf(parent -> parent.equals(navigationElementContainer));
            };

            // extract the shown menu items
            final List<Widget> menuItems = screenState.getWidgets().stream()
                    .filter(Widget::isLeafWidget)
                    .filter(isNavigationWidget)
                    .filter(widget -> widget.isSonOf(w -> w.isListViewType() || w.isRecyclerViewType()))
                    .filter(Widget::isTextViewType)
                    .filter(Widget::isEnabled)
                    .filter(Widget::hasText)
                    .filter(Widget::hasResourceID)
                    .collect(Collectors.toList());

            if (menuItems.isEmpty()) {
                throw new IllegalStateException("Couldn't discover any options!");
            }

            final Set<String> selectedMenuItems
                    = action.getSelectedMenuItems(navigationMenuClickAction.getWidget());

            // pick the first not yet selected menu item
            final Widget notSelectedMenuItem = menuItems.stream()
                    .filter(widget -> !selectedMenuItems.contains(widget.getText()))
                    .findFirst()
                    .orElse(null);

            // TODO: Record the not yet selected menu item for deterministic replaying.

            if (notSelectedMenuItem == null) {
                // All menu items have been selected at least once, pick random.
                final Widget menuItem = Randomness.randomElement(menuItems);
                handleClick(menuItem);
            } else {
                handleClick(notSelectedMenuItem);
                action.addSelectedMenuItem(navigationMenuClickAction.getWidget(),
                        notSelectedMenuItem.getText());
            }
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'change radio group selections' motif action, i.e. a random radio button is
     * clicked per radio group.
     *
     * @param action The given motif action.
     */
    private void handleChangeRadioGroupSelections(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            // TODO: Store click actions on radio buttons directly in motif action.

            final IScreenState screenState
                    = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

            // Extract the radio groups.
            final List<Widget> radioGroups = screenState.getWidgets().stream()
                    .filter(Widget::isRadioGroupType)
                    .filter(Widget::isEnabled)
                    .collect(Collectors.toList());

            if (!radioGroups.isEmpty()) {

                final Map<Widget, List<Widget>> radioButtonsPerGroup =
                        radioGroups.stream().collect(Collectors.toMap(Function.identity(),
                                (radioGroup) -> radioGroup.getChildren().stream()
                                        .filter(Widget::isLeafWidget)
                                        .filter(Widget::isRadioButtonType)
                                        .filter(Widget::isEnabled)
                                        .collect(Collectors.toList())
                        ));

                // TODO: Memorize which combination of radio buttons have been selected and choose
                //  a possible distinct sort order else pick random.

                for (Map.Entry<Widget, List<Widget>> radioGroup : radioButtonsPerGroup.entrySet()) {

                    final List<Widget> radioButtons = radioGroup.getValue();

                    if (!radioButtons.isEmpty()) {
                        // click on a random radio button
                        final Widget radioButton = Randomness.randomElement(radioButtons);
                        handleClick(radioButton);
                    }
                }
            }
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'open sort menu and sort order selection' motif action, i.e. first the sort menu
     * is opened by clicking on the sort symbol and then a possible different sort order is selected
     * by clicking on it.
     *
     * @param action The given motif action.
     */
    private void handleSortMenuClickAndSortOrderSelection(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            // click on the sort menu to open the list of menu items
            final WidgetAction menuClickAction = (WidgetAction) action.getUIActions().get(0);
            handleClick(menuClickAction.getWidget());

            /*
             * TODO: We encountered a strange situation where the fetched screen state contained
             *  essentially the displayed widgets but with malformed coordinates, which led to
             *  clicking above the chosen menu item. We believe this is related to a sync issue of
             *  UIAutomator. A small waiting time seems to remedy the issue, but it is not clear
             *  whether such hand-crafted waiting time works across apps. We could resort to
             *  UIDevice#waitForIdle(); but the idle time varies largely between two consecutive calls.
             */
            Utils.sleep(200);

            // Fetch the new screen state containing the sort order possibilities.
            final IScreenState screenState
                    = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

            /*
            * There are basically two different layouts used for the sort order possibilities. Either
            * we have radio groups where one can select a single entry, or we have a regular list
            * view containing the clickable possibilities.
             */
            final List<Widget> radioGroups = screenState.getWidgets().stream()
                    .filter(Widget::isRadioGroupType)
                    .filter(Widget::isEnabled)
                    .collect(Collectors.toList());

            if (!radioGroups.isEmpty()) {

                final Map<Widget, List<Widget>> radioButtonsPerGroup =
                        radioGroups.stream().collect(Collectors.toMap(Function.identity(),
                                (radioGroup) -> radioGroup.getChildren().stream()
                                        .filter(Widget::isLeafWidget)
                                        .filter(Widget::isRadioButtonType)
                                        .filter(Widget::isEnabled)
                                .collect(Collectors.toList())
                        ));

                // TODO: Memorize which combination of radio buttons have been selected and choose
                //  a possible distinct sort order else pick random.

                for (Map.Entry<Widget, List<Widget>> radioGroup : radioButtonsPerGroup.entrySet()) {

                    final List<Widget> radioButtons = radioGroup.getValue();

                    if (!radioButtons.isEmpty()) {
                        // click on a random radio button
                        final Widget radioButton = Randomness.randomElement(radioButtons);
                        handleClick(radioButton);
                    }
                }

                // click on 'OK' button
                screenState.getWidgets().stream()
                        .filter(Widget::isLeafWidget)
                        .filter(Widget::isButtonType)
                        .filter(Widget::isEnabled)
                        .filter(Widget::isVisible)
                        .filter(widget -> widget.getText().equalsIgnoreCase("OK")
                                || widget.getText().equalsIgnoreCase("APPLY"))
                        .findFirst().ifPresent(this::handleClick);
            } else {

                // TODO: The sorting order might be defined through a sortable list (drag & drop).

                // extract the shown menu items
                final List<Widget> menuItems = screenState.getWidgets().stream()
                        .filter(Widget::isLeafWidget)
                        .filter(widget -> widget.isSonOf(Widget::isListViewType)
                                        || widget.isSonOf(Widget::isRecyclerViewType))
                        .filter(Widget::isTextViewType)
                        .filter(Widget::isEnabled)
                        .filter(widget -> widget.hasText() || widget.hasContentDescription())
                        .collect(Collectors.toList());

                if (menuItems.isEmpty()) {
                    throw new IllegalStateException("Couldn't discover any options!");
                }

                final Set<String> selectedMenuItems
                        = action.getSelectedMenuItems(menuClickAction.getWidget());

                // pick the first not yet selected menu item
                final Widget notSelectedMenuItem = menuItems.stream()
                        .filter(widget -> !selectedMenuItems.contains(widget.getText()))
                        .findFirst()
                        .orElse(null);

                // TODO: Record the not yet selected menu item for deterministic replaying.

                if (notSelectedMenuItem == null) {
                    // All menu items have been selected at least once, pick random.
                    final Widget menuItem = Randomness.randomElement(menuItems);
                    handleClick(menuItem);
                } else {
                    handleClick(notSelectedMenuItem);
                    action.addSelectedMenuItem(menuClickAction.getWidget(), notSelectedMenuItem.getText());
                }

                // There might be an (optional) OK button.
                screenState.getWidgets().stream()
                        .filter(Widget::isLeafWidget)
                        .filter(Widget::isButtonType)
                        .filter(Widget::isEnabled)
                        .filter(Widget::isVisible)
                        .filter(widget -> widget.getText().equalsIgnoreCase("OK")
                                || widget.getText().equalsIgnoreCase("APPLY"))
                        .findFirst().ifPresent(this::handleClick);

                // Or there might be an (optional) save text view in the menu bar.
                screenState.getWidgets().stream()
                        .filter(Widget::isLeafWidget)
                        .filter(Widget::isEnabled)
                        .filter(Widget::isTextViewType)
                        .filter(Widget::isVisible)
                        .filter(widget -> widget.getText().toLowerCase().contains("save")
                                || widget.getContentDesc().toLowerCase().contains("save"))
                        .findFirst().ifPresent(this::handleClick);
            }
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the 'menu click and item selection' motif action, i.e. first the menu is opened by
     * clicking on the menu symbol and then a not yet selected menu item is selected by also clicking
     * on it.
     *
     * @param action The given motif action.
     */
    private void handleMenuClickAndItemSelection(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            // click on the menu to open the list of menu items
            final WidgetAction menuClickAction = (WidgetAction) action.getUIActions().get(0);
            handleClick(menuClickAction.getWidget());

            /*
            * TODO: We encountered a strange situation where the fetched screen state contained
            *  essentially the displayed widgets but with malformed coordinates, which led to
            *  clicking above the chosen menu item. We believe this is related to a sync issue of
            *  UIAutomator. A small waiting time seems to remedy the issue, but it is not clear
            *  whether such hand-crafted waiting time works across apps. We could resort to
            *  UIDevice#waitForIdle(); but the idle time varies largely between two consecutive calls.
             */
            Utils.sleep(200);

            // fetch the new screen state containing the list view
            final IScreenState screenState
                    = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

            // extract the shown menu items
            final List<Widget> menuItems = screenState.getWidgets().stream()
                    .filter(Widget::isLeafWidget)
                    .filter(widget -> widget.isSonOf(w -> w.isListViewType() || w.isRecyclerViewType()))
                    .filter(Widget::isTextViewType)
                    .filter(Widget::isEnabled)
                    .filter(Widget::hasText)
                    .collect(Collectors.toList());

            if (menuItems.isEmpty()) {
                throw new IllegalStateException("Couldn't discover any menu item!");
            }

            final Set<String> selectedMenuItems
                    = action.getSelectedMenuItems(menuClickAction.getWidget());

            // pick the first not yet selected menu item
            final Widget notSelectedMenuItem = menuItems.stream()
                    .filter(widget -> !selectedMenuItems.contains(widget.getText()))
                    .findFirst()
                    .orElse(null);

            // TODO: Record the not yet selected menu item for deterministic replaying.

            if (notSelectedMenuItem == null) {
                // All menu items have been selected at least once, pick random.
                final Widget menuItem = Randomness.randomElement(menuItems);
                handleClick(menuItem);
            } else {
                handleClick(notSelectedMenuItem);
                action.addSelectedMenuItem(menuClickAction.getWidget(), notSelectedMenuItem.getText());
            }
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the motif action 'fill forms', i.e. it fills out all visible and enabled text forms
     * on the current screen.
     *
     * @param action The given motif action.
     */
    private void handleFillForm(final MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

            action.getUIActions().stream().forEach(textInsertAction ->
                            handleEdit(((WidgetAction) textInsertAction).getWidget()));
        } else {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }

    /**
     * Executes the motif action 'fill form and click submit' as used in the Sapienz paper.
     *
     * @param action The given motif action.
     */
    private void handleFillFormAndSubmit(MotifAction action) {

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

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
                        handleEdit(primitiveAction, false);
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
                        handleEdit(typeText, false);
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

        // cache attributes to avoid stale object exception
        String className = uiElement.getClassName();
        Rect bounds = uiElement.getVisibleBounds();
        String resourceName = uiElement.getResourceName();

        IScreenState screenState = Registry.getUiAbstractionLayer().getLastScreenState();

        for (Widget widget : screenState.getWidgets()) {

            String resourceID = widget.getResourceID().isEmpty() ? null : widget.getResourceID();

            if (widget.getClazz().equals(className)
                    && widget.getBounds().equals(bounds)
                    && Objects.equals(resourceID, resourceName)) {
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

        /*
        * The subsequent call to clickAndWait() can swallow a TimeoutException, which is thrown also
        * by our TimeoutRun class to indicate the termination of the exploration. If swallowed, our
        * timeout run thread would never terminate. To minimise such a case, we check if an interrupt
        * was already triggered by our shutdown procedure and abort the execution in that case.
        * See https://gitlab.infosun.fim.uni-passau.de/se2/mate/mate/-/merge_requests/184#note_80071.
         */
        Utils.throwOnInterrupt();

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

        if (!Properties.USE_PRIMITIVE_ACTIONS()) {

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
     */
    private void executeAction(UIAction action) {

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
    }

    /**
     * Executes an Intent-based action. Depending on the target component, either
     * startActivity(), startService() or sendBroadcast() is invoked.
     *
     * @param action The action which contains the Intent to be sent.
     */
    private void executeAction(IntentBasedAction action) {

        Intent intent = action.getIntent();

        try {
            switch (action.getComponentType()) {
                case ACTIVITY:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        // https://stackoverflow.com/a/57490942/6110448
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
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
            final String msg = "Calling startActivity() from outside of an Activity context " +
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
    }

    /**
     * Executes a primitive action, e.g. a click on a specific coordinate.
     *
     * @param action The action to be executed.
     */
    private void executeAction(PrimitiveAction action) {

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
                handleEdit(action, false);
                break;
            case CLEAR_TEXT:
                handleEdit(action, true);
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
     * @param clear Whether the text field should be cleared (filled with an empty string).
     */
    private void handleEdit(PrimitiveAction action, boolean clear) {

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

                MATE.log_warn("Stale UiObject2!");
                e.printStackTrace();

                /*
                 * Unfortunately, it can happen that the requested ui element gets immediately stale.
                 * The only way to recover from such a situation is to call findObject() another time.
                 */
                uiElement = device.findObject(By.focused(true));
                if (uiElement != null) {

                    try {
                        widget = findWidget(uiElement);
                    } catch (StaleObjectException ex) {
                        MATE.log_warn("Stale UiObject2!");
                        ex.printStackTrace();
                    }
                }
            }

            if (widget != null && widget.isEditTextType()) {

                // use empty string for clearing
                String textData = "";

                if (!clear) {
                    /*
                     * If we run in replay mode, we should use the recorded text instead of a new text
                     * that is randomly created. Otherwise, we may end up in a different state and
                     * subsequent actions might not show the same behaviour as in the recorded run.
                     */
                    textData = Registry.isReplayMode() ? action.getText() :
                            Objects.toString(generateTextData(widget, widget.getMaxTextLength()), "");
                }

                MATE.log_debug("Inserting text: " + textData);

                try {
                    uiElement.setText(textData);

                    // record for possible replaying + findObject() relies on it
                    action.setText(textData);
                    widget.setText(textData);
                } catch (StaleObjectException e) {
                    MATE.log_warn("Stale UiObject2!");
                    e.printStackTrace();
                } finally {
                    // we need to close the soft keyboard, but only if it is present
                    if (isKeyboardOpened()) {
                        device.pressBack();
                    }
                }
            }
        }
    }

    /**
     * Checks whether the soft keyboard is opened or not.
     *
     * @return Returns {@code true} if the soft keyboard is opened, otherwise {@code false} is
     *         returned.
     */
    public boolean isKeyboardOpened() {

        // https://stackoverflow.com/questions/17223305/suppress-keyboard-after-setting-text-with-android-uiautomator
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

        for (AccessibilityWindowInfo window : uiAutomation.getWindows()) {
            if (window.getType() == AccessibilityWindowInfo.TYPE_INPUT_METHOD) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executes a widget action, e.g. a click on a certain widget.
     *
     * @param action The action to be executed.
     */
    private void executeAction(WidgetAction action) {

        Widget selectedWidget = action.getWidget();
        ActionType typeOfAction = action.getActionType();

        switch (typeOfAction) {
            case CLICK:
            case CHANGE_CHECKABLE:
                handleClick(selectedWidget);
                break;
            case LONG_CLICK:
                handleLongClick(selectedWidget);
                break;
            case TYPE_TEXT:
            case TYPE_SPECIFIC_TEXT:
                handleEdit(selectedWidget);
                break;
            case CLEAR_TEXT:
                handleClear(selectedWidget);
                break;
            case SWIPE_DOWN:
            case SWIPE_UP:
            case SWIPE_LEFT:
            case SWIPE_RIGHT:
                handleSwipe(selectedWidget, typeOfAction);
                break;
            case CHANGE_SEEK_BAR:
                handleChangeSeekBar(selectedWidget);
                break;
            default:
                throw new IllegalArgumentException("Action type " + action.getActionType()
                        + " not implemented for widget actions.");
        }
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
     * Checks whether the given screen contains a progress bar.
     *
     * @param screenState The given screen state.
     * @return Returns {@code true} if the screen contains a progress bar, otherwise {@code false}
     *         is returned.
     */
    public boolean checkForProgressBar(IScreenState screenState) {

        for (Widget widget : screenState.getWidgets()) {
            if (widget.isProgressBarType() && widget.isEnabled() && widget.isVisible()) {
                return true;
            }
        }

        return false;
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
     * Tries to move the seek bar (special kind of progress bar) in a random direction.
     *
     * @param widget The seek bar widget.
     */
    private void handleChangeSeekBar(final Widget widget) {

        // TODO: Handle vertically laid-out seek bars.

        final UiObject2 uiObject = findObject(widget);

        if (uiObject != null) {

            /*
            * NOTE: We tried out different low-level actions like swipe, drag, scroll and fling but
            * all of them had their limitations or didn't function as expected. In the end, clicking
            * directly on a random X coordinate appeared to be the most reliable option. One
            * alternative to below approach is to use DPAD_RIGHT and DPAD_LEFT (this works reliably
            * and has the advantage that it moves the seek bar exactly one 'step' in either direction.
            * However, the problem here was that to use those two actions, one has to first move the
            * DPAD 'cursor' to the correct location and this didn't seem feasible.
             */

            try {
                // Click on a random X coordinate.
                final int X1 = uiObject.getVisibleBounds().left;
                final int X2 = uiObject.getVisibleBounds().right;
                final int Y = uiObject.getVisibleBounds().centerY();
                int randomX = Randomness.getRandom(X1, X2);
                device.click(randomX, Y);
            } catch (StaleObjectException e) {
                MATE.log_warn("Stale UiObject2!");
                e.printStackTrace();

                // fall back mechanism
                final int X1 = widget.getX1();
                final int X2 = widget.getX2();
                final int Y = widget.getY();
                int randomX = Randomness.getRandom(X1, X2);
                device.click(randomX, Y);
            }
        }
    }

    /**
     * Clears the widget's text.
     *
     * @param widget The widget whose input should be cleared.
     */
    private void handleClear(Widget widget) {

        UiObject2 uiObject = findObject(widget);

        if (uiObject != null) {

            try {
                uiObject.setText("");

                // reflect change since we cache screen states and findObject() relies on it
                widget.setText("");

                // we need to close the soft keyboard, but only if it is present
                if (isKeyboardOpened()) {
                    device.pressBack();
                }
            } catch (StaleObjectException e) {
                MATE.log_warn("Stale UiObject2!");
                e.printStackTrace();
                handleEditFallback(widget, "");
            }
        } else {
            handleEditFallback(widget, "");
        }
    }

    /**
     * Executes a swipe (upon a widget) in a given direction.
     *
     * @param widget The widget at which position the swipe should be performed.
     * @param direction The direction of the swipe, e.g. swipe to the left.
     */
    private void handleSwipe(Widget widget, ActionType direction) {

        // TODO: Elaborate how good swiping is working on different scrollable views. This can largely
        //  impact how good the exploration works.

        int pixelsMove = 300;
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
                    MATE.log_warn("Stale UiObject2!");
                    e.printStackTrace();
                    X = widget.getX();
                    Y = widget.getY();
                }

                /*
                * The default pixel move size is rather low, thus the change upon a swipe is tiny.
                * The preferred option is to make the pixel move size dependent on the size of the
                * scrollable UI element.
                 */
                try {
                    if (direction == SWIPE_DOWN || direction == SWIPE_UP) {
                        pixelsMove = (obj.getVisibleBounds().bottom - obj.getVisibleBounds().top) / 2;
                    } else {
                        pixelsMove = (obj.getVisibleBounds().right - obj.getVisibleBounds().left) / 2;
                    }
                } catch (StaleObjectException e) {
                    MATE.log_warn("Stale UiObject2!");
                    e.printStackTrace();
                }

            } else { // rely on the widget coordinates

                X = widget.getX();
                Y = widget.getY();

                // take half of the widget size in the given direction
                if (direction == SWIPE_DOWN || direction == SWIPE_UP) {
                    pixelsMove = Y; // is essentially (Y2-Y1)/2
                } else {
                    pixelsMove = X; // is essentially (X2-X1)/2
                }
            }
        } else {

            X = device.getDisplayWidth() / 2;
            Y = device.getDisplayHeight() / 2;

            if (direction == SWIPE_DOWN || direction == SWIPE_UP)
                pixelsMove = Y;
            else
                pixelsMove = X;
        }

        switch (direction) {
            case SWIPE_UP:
                device.swipe(X, Y, X, Y - pixelsMove, steps);
                break;
            case SWIPE_DOWN:
                device.swipe(X, Y, X, Y + pixelsMove, steps);
                break;
            case SWIPE_RIGHT:
                device.swipe(X, Y, X + pixelsMove, Y, steps);
                break;
            case SWIPE_LEFT:
                device.swipe(X, Y, X - pixelsMove, Y, steps);
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

        int X = 0, Y = 0;

        try {
            if (obj != null) {
                X = obj.getVisibleBounds().centerX();
                Y = obj.getVisibleBounds().centerY();
            }
        } catch (StaleObjectException e) {
            MATE.log_warn("Stale UiObject2!");
            e.printStackTrace();
            X = widget.getX();
            Y = widget.getY();
        }

        device.swipe(X, Y, X, Y, 120);
    }

    /**
     * Tries to return a ui object matching the given widget. This is a
     * best effort approach.
     *
     * @param widget The widget whose ui object should be looked up.
     * @return Returns the corresponding ui object or {@code null} if no sui ui object could be
     *         found or the ui object got stale in the meantime.
     */
    private UiObject2 findObject(Widget widget) {

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
            MATE.log_warn("Stale UiObject2!");
            e.printStackTrace();
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

            try {
                uiObject.setText(textData);

                // reflect change since we cache screen states and findObject() relies on it
                widget.setText(textData);

                // we need to close the soft keyboard, but only if it is present
                if (isKeyboardOpened()) {
                    device.pressBack();
                }
            } catch (StaleObjectException e) {
                MATE.log_warn("Stale UiObject2!");
                e.printStackTrace();
                handleEditFallback(widget, textData);
            }
        } else {
            handleEditFallback(widget, textData);
        }
    }

    /**
     * Provides a fallback mechanism for editing a text field.
     *
     * @param widget The text field widget.
     * @param textData The text to be inserted.
     */
    private void handleEditFallback(Widget widget, String textData) {

        // try to click on the widget, which in turn should get focused
        device.click(widget.getX(), widget.getY());
        UiObject2 obj = device.findObject(By.focused(true));
        if (obj != null) {
            try {
                obj.setText(textData);

                // reflect change since we cache screen states and findObject() relies on it
                widget.setText(textData);
            } catch (StaleObjectException e) {
                MATE.log_warn("Stale UiObject2!");
                e.printStackTrace();
                MATE.log_warn("Couldn't edit widget: " + widget);
            } finally {
                // we need to close the soft keyboard, but only if it is present
                if (isKeyboardOpened()) {
                    device.pressBack();
                }
            }
        } else {
            MATE.log_warn("Couldn't edit widget: " + widget);
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

        if (Properties.STACK_TRACE_USER_INPUT_SEEDING()) {
            if (random.nextDouble() < PROB_STACK_TRACE_USER_INPUT) {

                // use as input a random stack trace token
                final Set<String> tokens = Registry.getEnvironmentManager().getStackTraceUserInput();

                if (!tokens.isEmpty()) {
                    return Randomness.randomElement(tokens);
                }
            }
        }

        // Select an empty input with a low probability.
        if (random.nextDouble() < PROB_WHITESPACE) {
            return " ";
        }

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
        Context context = getTargetContext();
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
            e.printStackTrace();
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

        // TODO: Check if the command output is consistent among different APIs.

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
                .filter(line -> !line.replaceAll("\\s+", "").isEmpty())
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
        return Registry.getManifest().getActivities().stream()
                .map(ComponentDescription::getFullyQualifiedName)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    private List<String> getActivitiesFromPackageManager() {

        // NOTE: This will only retrieve the exported/enabled activities, not necessarily all listed
        // in the AndroidManifest.xml file!

        Instrumentation instrumentation = getInstrumentation();

        try {
            // see: https://stackoverflow.com/questions/23671165/get-all-activities-by-using-package-name
            PackageInfo pi = instrumentation.getTargetContext().getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_ACTIVITIES);

            // TODO: Ensure that the short form '/.subpackage.activityName' is not used!!!
            return Arrays.stream(pi.activities).map(activity -> activity.name)
                    .collect(Collectors.toList());
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException("Couldn't retrieve activity names!", e);
        }
    }

    /**
     * Clears the files contained in the app-internal storage, i.e. the app is reset to its
     * original state.
     */
    public void clearApp() {

        Utils.throwOnInterrupt();

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
        } catch (IOException e) {
            MATE.log_warn("Couldn't clear app data!");
            MATE.log_warn(e.getMessage());

            // fallback mechanism
            Registry.getEnvironmentManager().clearAppData();
        } finally {
            /*
             * The execution of the 'pm clear' command also drops the runtime permissions of the AUT,
             * thus we have to re-grant them in order to allow the tracer to write its traces to the
             * external storage. Otherwise, one may encounter the following situation: A reset is
             * performed, dropping the runtime permissions. The execution of the next actions triggers
             * dumping the traces because the cache limit of the tracer is reached. This operation would
             * fail consequently. We need to call this operation in any case even when MATE received
             * the timeout interrupt (only possible in the fallback mechanism!).
             */
            MATE.log("Granting runtime permissions: " + grantRuntimePermissions());
        }
    }

    /**
     * Retrieves the stack trace of the last discovered crash.
     *
     * @return Returns the stack trace of the last crash.
     */
    public StackTrace getLastCrashStackTrace() {

        final StackTrace stackTrace = getLastCrashStackTraceInternal();

        if (Properties.WRITE_STACK_TRACE_TO_FILE()) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");
            final String fileName = "crash_" + LocalDateTime.now().format(formatter) + ".txt";
            Registry.getEnvironmentManager().writeFile("stack_traces/" + fileName,
                    stackTrace.getRawStackTraceLines().stream().collect(Collectors.joining("\n")));
        }

        return stackTrace;
    }

    /**
     * Extracts the last stack trace from the logcat logs.
     *
     * @return Returns the extracted stack trace.
     */
    private StackTrace getLastCrashStackTraceInternal() {

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

        } catch (IOException e) {
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
        MATE.log_debug("Sending broadcast...");
        Intent intent = new Intent("STORE_TRACES");
        intent.setComponent(new ComponentName(Registry.getPackageName(),
                "de.uni_passau.fim.auermich.tracer.Tracer"));
        InstrumentationRegistry.getTargetContext().sendBroadcast(intent);
    }

    /**
     * Retrieves a file handle on some file located on the external storage.
     *
     * @param filename The name of the file.
     * @return Returns a file handle for the specified file on the external storage.
     */
    private File getFileFromExternalStorage(final String filename) {
        final File sdCard = Environment.getExternalStorageDirectory();
        return new File(sdCard, filename);
    }

    /**
     * Retrieves the info.txt file from the external storage.
     *
     * @return Returns a file handle on the info.txt file.
     */
    private File getInfoFile() {
        return getFileFromExternalStorage("info.txt");
    }

    /**
     * Retrieves the traces.txt file from the external storage.
     *
     * @return Returns a file handle on the traces.txt file.
     */
    private File getTracesFile() {
        return getFileFromExternalStorage("traces.txt");
    }

    /**
     * Checks whether the info.txt file exists.
     *
     * @return Returns {@code true} if the info.txt file exists, otherwise {@code false} is
     *         returned.
     */
    private boolean infoFileExists() {
        return getInfoFile().exists();
    }

    /**
     * Checks whether the traces.txt file exists.
     *
     * @return Returns {@code true} if the traces.txt file exists, otherwise {@code false} is
     *         returned.
     */
    private boolean tracesFileExists() {
        return getTracesFile().exists();
    }

    /**
     * Deletes both the traces.txt and info.txt file from the external storage.
     */
    private void deleteTraceFiles() {

        // delete both files in order that the next action is assigned the correct traces
        boolean removedTracesFile = getTracesFile().delete();
        boolean removedInfoFile = getInfoFile().delete();

        if (!removedInfoFile) {
            MATE.log_warn("Couldn't remove the info.txt file!");
        }

        if (!removedTracesFile) {
            MATE.log_warn("Couldn't remove the traces.txt file!");
        }
    }

    /**
     * Requests the dumping of the traces by sending a broadcast to the tracer.
     */
    private void dumpTraces() {

        // triggers the dumping of traces to a file called traces.txt
        sendBroadcastToTracer();

        /*
         * We need to wait until the info.txt file is generated, once it is there, we know that all
         * traces have been dumped.
         */
        MateInterruptedException interrupted = null;
        while (!infoFileExists()) {
            MATE.log_debug("Waiting for info.txt...");
            try {
                Utils.sleep(200);
            } catch (final MateInterruptedException e) {
                /*
                 * We might get a timeout (signaled through an interrupt) while waiting for the
                 * traces to be written. In that case we still want to wait for the tracer to write
                 * its traces.
                 */
                interrupted = e;
            }
        }

        /*
         * Re-throw the interrupt exception caught while waiting for the tracer to finish dumping
         * its traces. This is done before reading and deleting the traces.txt file, so that the
         * traces won't be lost.
         */
        if (interrupted != null) {
            MATE.log_debug("Interrupt detected during dumping traces!");
            throw interrupted;
        }
    }

    /**
     * Reads the traces from the traces.txt file.
     *
     * @return Returns the traces from the traces.txt file.
     */
    private Set<String> readTracesFile() {

        /*
         * The method exists() may return 'false' if we try to access it while it is written by
         * another process, i.e. the tracer class. By sending the broadcast (asynchronous operation!)
         * only if the info.txt doesn't exist yet, this should never happen.
         */
        if (!tracesFileExists()) {
            getInfoFile().delete();
            throw new IllegalStateException("The file traces.txt doesn't exist!");
        }

        final Set<String> traces = new HashSet<>();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(getTracesFile())))) {
            String line;
            while ((line = reader.readLine()) != null) {
                traces.add(line);
            }
        } catch (final IOException e) {
            getInfoFile().delete();
            throw new IllegalStateException("Couldn't read traces!", e);
        }

        return traces;
    }

    /**
     * Waits for the tracer until it dumped both the traces.txt and info.txt file.
     *
     * @return Returns {@code true} if the waiting was successful, otherwise {@code false} is
     *         returned.
     */
    private boolean waitForTracer() {

        MATE.log_debug("Waiting for info.txt/traces.txt...");

        boolean tracesFileExists = tracesFileExists();
        boolean infoFileExists = infoFileExists();

        if (infoFileExists && !tracesFileExists) {
            MATE.log_error("info.txt exists, but not traces.txt, this should not happen.");
            return false;
        }

        if (tracesFileExists && !infoFileExists) {
            /*
             * There are two possible states here:
             *
             *     1) The tracer is currently dumping its traces, and we just need to wait for it
             *        to finish.
             *     2) The tracer dumped the traces because its cache got full. In that case we need
             *        to call the tracer to get the remaining traces.
             *
             * We have no clear method of determining in which state we are in, so we have to wait
             * for a while to have the tracer potentially finish dumping its traces and re-check for
             * the info.txt.
             */
            MateInterruptedException interrupted = null;
            final int maxWaitTimeInSeconds = 30;

            for (int i = 1; i < maxWaitTimeInSeconds; ++i) {

                try {
                    Utils.sleep(1);
                } catch (final MateInterruptedException e) {
                    // keep track of any interrupt
                    interrupted = e;
                }

                tracesFileExists = tracesFileExists();
                infoFileExists = infoFileExists();

                if (tracesFileExists && infoFileExists) {
                    // We were in case 1), now the tracer has finished dumping the traces.
                    break;
                }
            }

            if (interrupted != null) {
                /*
                 * We suspend the interrupt until the tracer hopefully completed dumping its traces.
                 * By re-throwing, we terminate the current thread.
                 */
                MATE.log_debug("Interrupt detected during waiting for tracer!");
                throw interrupted;
            }

            if (infoFileExists && !tracesFileExists) {
                MATE.log_error("info.txt exists, but not traces.txt, this should not happen.");
                return false;
            }
        }

        MATE.log_debug("Waiting for info.txt/traces.txt completed!");
        return true;
    }

    /**
     * Reads the traces from the external memory and deletes afterwards the info and traces file.
     *
     * @return Returns the set of traces.
     */
    public Set<String> getTraces() {

        /*
         * If an interrupt happened, i.e. the TimeoutRun signaled the end of the execution, we abort
         * the execution here.
         */
        Utils.throwOnInterrupt();

        if (!waitForTracer()) {
            MATE.log_warn("Couldn't wait for tracer.");
            return new HashSet<>(0);
        }

        /*
         * If the AUT has been crashed, the uncaught exception handler takes over and produces both
         * an info.txt and traces.txt file, thus sending the broadcast would be redundant. Under
         * every other condition, there should be no info.txt present and the broadcast is necessary.
         */
        if (!infoFileExists()) {
            dumpTraces();
        }

        final Set<String> traces = readTracesFile();
        deleteTraceFiles();
        return traces;
    }

    /**
     * Stores the traces to a file called traces.txt on the external memory. Also generates a file
     * called info.txt that contains the number of written traces and indicates that the writing
     * of the traces has been completed.
     *
     * @param traces The traces to be stored.
     */
    public void storeTraces(Set<String> traces) {

        try (final Writer fileWriter = new FileWriter(getTracesFile())) {
            for (final String trace : traces) {
                fileWriter.write(trace);
                fileWriter.write(System.lineSeparator());
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Couldn't write to traces.txt!", e);
        }

        /*
         * The info.txt indicates that the dumping of traces has been completed and it contains
         * the number of written traces.
         */
        try (final Writer fileWriter = new FileWriter(getInfoFile())) {
            fileWriter.write(traces.size());
        } catch (final IOException e) {
            throw new IllegalStateException("Couldn't write to info.txt!", e);
        }
    }
}
