package org.mate.state.executables;

import org.mate.MATE;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.state.ScreenStateType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Models a screen state and maintains the list of applicable widget actions.
 */
public class ActionsScreenState extends AbstractScreenState {

    /**
     * Defines the actions that are applicable on the associated screen.
     */
    private List<UIAction> actions;

    /**
     * TODO: What is the purpose of this?
     * Tracks per activity (app screen) each widget and the number of ids.
     */
    private static Map<String, Map<String, List<Integer>>> idSizes = new Hashtable<>();


    /**
     * Creates a new screen state based on the given {@link AppScreen}.
     *
     * @param appScreen The given app screen.
     */
    public ActionsScreenState(AppScreen appScreen) {
        super(appScreen.getPackageName(), appScreen.getActivityName(), appScreen.getWidgets());
        this.actions = null;
    }

    /**
     * Returns the maximal number of ...
     *
     * @param sameIDWidgets Stores a mapping of widget id to the amount the widget appeared.
     * @param widgetId      The widget id.
     * @return Returns the maximal number of ... or zero if no entry for the widget id exists.
     */
    private int getMaxAmountOfID(Map<String, List<Integer>> sameIDWidgets, String widgetId) {

        List<Integer> amounts = sameIDWidgets.get(widgetId);

        if (amounts == null) {
            amounts = new ArrayList<>();
            sameIDWidgets.put(widgetId, amounts);
            return 0;
        }

        if (amounts.size() == 0) {
            return 0;
        } else {
            return Collections.max(amounts);
        }
    }

    /**
     * Returns the list of widget actions.
     *
     * @return Returns the list of widget actions.
     */
    @Override
    public List<WidgetAction> getWidgetActions() {
        List<WidgetAction> widgetActions = new ArrayList<>();
        for (UIAction uiAction : actions) {
            if (uiAction instanceof WidgetAction) {
                widgetActions.add((WidgetAction) uiAction);
            }
        }
        return Collections.unmodifiableList(widgetActions);
    }

    /**
     * Extracts the list of applicable widget actions on the underlying screen.
     *
     * @return Returns the list of widget actions.
     */
    @Override
    public List<UIAction> getActions() {

        if (actions != null)
            return actions;

        // TODO: what is the purpose of this?
        Map<String, List<Integer>> sameIDWidgets = idSizes.get(activityName);

        if (sameIDWidgets == null) {
            sameIDWidgets = new Hashtable<>();
            idSizes.put(activityName, sameIDWidgets);
        }

        // collect the executable ui actions
        List<UIAction> executables = new ArrayList<>();

        // track whether some editable widget was found
        boolean foundEditable = false;

        // track whether we have already added the action 'ENTER'
        boolean enterAdded = false;

        // track whether the current widget is selected to have a widget action
        boolean selected;

        // a counter how often the same widget id (key) appears
        Map<String, Integer> idAmount = new Hashtable<>();

        for (Widget widget : widgets) {

            selected = false;

            if (widget.getClazz().contains("Button") || widget.isClickable()
                    || widget.isLongClickable()
                    || widget.isScrollable()
                    || widget.isEditable()) {
                selected = true;
            }

            if (widget.directSonOf("ListView") || widget.directSonOf("GridView")) {
                if (widget.getParent().isClickable())
                    selected = true;
            }

            if (widget.getClazz().equals("android.view.View")
                    && (!widget.getContentDesc().isEmpty() || !widget.getText().isEmpty())) {
                selected = true;
            }

            if (widget.getClazz().contains("Spinner")) {
                selected = true;
            }

            // for skype test
            if (widget.getClazz().contains("ViewGroup")) {
                if (!widget.getContentDesc().isEmpty())
                    selected = true;
            }

            /*
             * TODO: Review this. It seems like to ignore redundant actions for the same widget,
             *  or more specifically for widgets with the same id.
             */
            Integer amount = idAmount.get(widget.getId());

            if (amount == null) {
                idAmount.put(widget.getId(), 1);
                amount = idAmount.get(widget.getId());
            } else {
                idAmount.put(widget.getId(), ++amount);
                amount = idAmount.get(widget.getId());
            }

            if (amount > getMaxAmountOfID(sameIDWidgets, widget.getId())
                    && sameIDWidgets.get(widget.getId()).size() == 2) {
                selected = false;
            }

            if (selected) {
                if (widget.getClazz().equals("android.widget.GridView"))
                    selected = false;
            }

            if (selected) {
                if (widget.getClazz().equals("android.view.View")) {
                    if (!widget.isClickable() && !widget.isLongClickable() && !widget.isScrollable()
                            && widget.getText().isEmpty() && widget.getContentDesc().isEmpty()) {
                        selected = false;
                    }
                }
            }

            if (selected) {
                if (widget.getClazz().contains("ListView"))
                    selected = false;
            }

            if (selected) {
                if (widget.getClazz().contains("ScrollView"))
                    selected = false;
            }

            if (selected) {
                if (widget.isSonOf("android.webkit.WebView"))
                    selected = false;
            }

            if (!widget.isEnabled())
                selected = false;

            if (selected) {

                // derive the appropriate widget action
                WidgetAction event;

                if (!widget.isEditable()) {
                    event = new WidgetAction(widget, ActionType.CLICK);
                    executables.add(event);
                    // TODO: widget.setClickable(true);
                }

                if (widget.isEditable() || widget.getClazz().contains("Edit")) {
                    event = new WidgetAction(widget, ActionType.TYPE_TEXT);
                    executables.add(0, event);
                    foundEditable = true;
                }

                if (widget.isLongClickable() && !widget.isEditable()) {
                    event = new WidgetAction(widget, ActionType.LONG_CLICK);
                    executables.add(event);
                } else {
                    if ((widget.isSonOfLongClickable()) && (!widget.isEditable()
                            && !widget.getClazz().contains("TextView"))) {
                        event = new WidgetAction(widget, ActionType.LONG_CLICK);
                        executables.add(event);
                        // TODO: widget.setLongClickable(true);
                    }
                }

                if (widget.isScrollable()) {

                    if (!widget.getClazz().contains("Spinner") && !widget.isSonOf("Spinner")) {
                        event = new WidgetAction(widget, ActionType.SWIPE_LEFT);
                        executables.add(event);

                        event = new WidgetAction(widget, ActionType.SWIPE_RIGHT);
                        executables.add(event);

                        event = new WidgetAction(widget, ActionType.SWIPE_UP);
                        executables.add(event);

                        event = new WidgetAction(widget, ActionType.SWIPE_DOWN);
                        executables.add(event);
                    }
                }
            }

            // if we found some editable widget, we should add the action 'ENTER'
            if (foundEditable && !enterAdded) {
                executables.add(new UIAction(ActionType.ENTER, activityName));
                enterAdded = true;
            }
        }

        // TODO: review this
        // update number of ids
        for (String id : idAmount.keySet()) {
            List<Integer> amounts = sameIDWidgets.get(id);
            if (amounts.size() < 2) {
                boolean sameAmount = false;
                for (int i = 0; i < amounts.size(); i++)
                    if (amounts.get(i) == idAmount.get(id))
                        sameAmount = true;
                if (!sameAmount)
                    amounts.add(idAmount.get(id));
            }
        }

        if (activityName.contains("GoogleOAuthActivity")) {
            MATE.log_acc("Reached GoogleOAuthActivity!");
            // we can't authenticate, so only allow to press 'BACK'
            executables = new ArrayList<>();
            executables.add(new UIAction(ActionType.BACK, activityName));
        } else {
            // those actions should be always applicable independent of the widget
            executables.addAll(getUIActions());
        }

        actions = executables;
        MATE.log_debug("Number of ui actions: " + executables.size());
        MATE.log_debug("Number of widget actions: " + getWidgetActions().size());
        MATE.log_debug("Widget actions: " + getWidgetActions());

        retrieveUIActions();

        return Collections.unmodifiableList(executables);
    }

    @SuppressWarnings("debug")
    private List<UIAction> retrieveUIActions() {

        // check whether the actions have been requested once
        if (actions != null) {
            return actions;
        }

        if (activityName.contains("GoogleOAuthActivity")) {
            MATE.log_acc("Reached GoogleOAuthActivity!");
            // we can't authenticate here, so only allow to press 'BACK'
            return Collections.singletonList(new UIAction(ActionType.BACK, activityName));
        }

        MATE.log_debug("Retrieving widget actions for screen state...");
        MATE.log_debug("Number of all widgets: " + this.widgets.size());

        List<Widget> widgets = new ArrayList<>();

        for (Widget widget : this.widgets) {
            /*
             * We ignore here primarily all widgets that are not visible, not enabled and don't
             * represent leaf widgets in the ui hierarchy. There are two exceptions to this rule:
             * 1) A spinner widget is not a leaf widget but represents a candidate for a widget
             * action. The other possibility would be to apply the action to the text view
             * that is the child element of the spinner.
             * 2) There are widgets like android.support.v7.app.ActionBar$Tab that are
             * (long-)clickable but don't represent leaf widgets. We should apply the action to
             * the widget that is (long-)clickable and ignore the child widget instead, since
             * a static analysis of event handlers can't properly match the widget otherwise.
             * 3) Likewise, it may can happen that checkable widgets are no leaf widgets.
             */
            if ((!widget.hasChildren() || widget.isSpinnerType() || widget.isClickable()
                    || widget.isLongClickable() || widget.isCheckable())
                    && widget.isVisible() && widget.isEnabled()) {
                widgets.add(widget);
            }
        }

        MATE.log_debug("Number of relevant widgets: " + widgets.size());

        /*
         * We use here a LinkedHashSet to maintain the insertion order, since we later
         * convert the set to a list and equals() builds on the list and inherently
         * its order.
         */
        Set<WidgetAction> widgetActions = new LinkedHashSet<>();

        for (Widget widget : widgets) {

            MATE.log_debug("Widget: " + widget);

            /*
            * TODO: We should exclude widgets that are part of top status/symbol bar.
            *  Otherwise, we may click unintentionally on the wifi symbol and cut off
            *  the connection. For a device with a resolution of 1080x1920 this represents
            *  the area [0,0][1080,72].
             */

            /*
            * TODO: It can happen that multiple sibling widgets are overlapping each other.
            *  We should define only for a single widget an action.
             */

            // TODO: support subtypes of spinner
            if (widget.isSonOf("android.widget.Spinner")) {
                MATE.log_debug("Spinner widget defines action itself!");
                // we define the action directly on the parent representing the spinner widget
                continue;
            }

            if (widget.isContainer()) {
                MATE.log_debug("Container as a leaf widget!");
                /*
                * It can happen that leaf widgets actually represent containers like
                * a linear layout in order to fill or introduce a gap.
                 */
                continue;
            }

            // TODO: Check whether we should only consider the direct parent widget?
            if (widget.isSonOfLongClickable() || widget.isSonOfClickable()) {
                MATE.log_debug("Parent widget that is clickable defines the action!");
                // we define the action directly on the (long-)clickable parent widget
                continue;
            }

            // TODO: Check whether we should only consider the direct parent widget?
            if (widget.isSonOfCheckable()) {
                MATE.log_debug("Parent widget that is checkable defines the action!");
                // we define the action directly on the checkable parent widget
                continue;
            }

            if (widget.isEditTextType()) {
                MATE.log_debug("Widget is an edit text instance!");
                widgetActions.add(new WidgetAction(widget, ActionType.TYPE_TEXT));
                // TODO: Do we need a corresponding clear widget action here?
            }

            if (widget.isButtonType()) {
                MATE.log_debug("Widget is a button instance!");

                if (widget.isClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
                }

                if (widget.isLongClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.LONG_CLICK));
                }
            }

            if (widget.isSpinnerType()) {
                MATE.log_debug("Widget is a spinner instance!");

                // TODO: check whether there are spinners that are not clickable
                if (widget.isClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
                }

                // TODO: check whether there are spinners that are not long clickable
                if (widget.isLongClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.LONG_CLICK));
                }

                if (widget.isScrollable()) {
                    // TODO: add proper scroll action
                }
            }

            /*
             * The elements in a list view are typically of type android.widget.TextView
             * and not clickable according to the underlying AccessibilityNodeInfo object,
             * however those elements represent in most cases clickable widgets.
             */
            // TODO: support subtypes of list view
            if (widget.isSonOf("android.widget.ListView")) {
                widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
                // TODO: widget.setClickable(true);
            }

            if (widget.isCheckable()) {
                // we check a widget by clicking on it
                widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
            }

            /*
             * Right now, we can't tell whether any kind of view widget should be clickable
             * or not, thus we assign to each leaf widget the click action. In the future,
             * we should rely on an additional static analysis of the byte code to verify
             * which leaf widget, in particular which text view, defines an event handler
             * and thus should be clickable.
             */
            widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
            // TODO: widget.setClickable(true);

            if (widget.isClickable()) {
                MATE.log_debug("Widget is clickable!");
            }

            if (widget.isLongClickable()) {
                MATE.log_debug("Widget is long clickable!");
            }

            if (widget.isScrollable()) {
                MATE.log_debug("Widget is scrollable!");
            }

            if (widget.isEditable()) {
                MATE.log_debug("Widget is editable!");
            }

            if (widget.isCheckable()) {
                MATE.log_debug("Widget is checkable!");
            }
        }

        MATE.log_debug("Number of derived widget actions: " + widgetActions.size());
        MATE.log_debug("Derived the following widget actions: " + widgetActions);

        List<UIAction> uiActions = new ArrayList<UIAction>(widgetActions);
        uiActions.addAll(getUIActions());
        return uiActions;
    }

    /**
     * Returns the list of actions are applicable independent on any widgets.
     *
     * @return Returns the list of actions applicable on any screen.
     */
    private List<UIAction> getUIActions() {

        List<UIAction> uiActions = new ArrayList<>();
        uiActions.add(new UIAction(ActionType.BACK, activityName));
        uiActions.add(new UIAction(ActionType.MENU, activityName));
        uiActions.add(new UIAction(ActionType.TOGGLE_ROTATION, activityName));
        // uiActions.add(new UIAction(ActionType.HOME, activityName));
        uiActions.add(new UIAction(ActionType.SEARCH, activityName));
        // uiActions.add(new UIAction(ActionType.QUICK_SETTINGS, activityName));
        // uiActions.add(new UIAction(ActionType.NOTIFICATIONS, activityName));
        uiActions.add(new UIAction(ActionType.SLEEP, activityName));
        uiActions.add(new UIAction(ActionType.WAKE_UP, activityName));
        uiActions.add(new UIAction(ActionType.DELETE, activityName));
        uiActions.add(new UIAction(ActionType.DPAD_CENTER, activityName));
        uiActions.add(new UIAction(ActionType.DPAD_DOWN, activityName));
        uiActions.add(new UIAction(ActionType.DPAP_UP, activityName));
        uiActions.add(new UIAction(ActionType.DPAD_LEFT, activityName));
        uiActions.add(new UIAction(ActionType.DPAD_RIGHT, activityName));
        uiActions.add(new UIAction(ActionType.ENTER, activityName));

        // swipes are both applicable to widgets and non-widgets
        uiActions.add(new UIAction(ActionType.SWIPE_DOWN, activityName));
        uiActions.add(new UIAction(ActionType.SWIPE_UP, activityName));
        uiActions.add(new UIAction(ActionType.SWIPE_LEFT, activityName));
        uiActions.add(new UIAction(ActionType.SWIPE_RIGHT, activityName));
        return uiActions;
    }

    /**
     * Returns the hash code of this screen state.
     *
     * @return Returns the hash code.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Compares two screen states for equality.
     *
     * @param o The other screen state to compare against.
     * @return Returns {@code true} if the two screen states are identical,
     * otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            /*
             * Since we want to cache screen states and the actions are constructed lazily,
             * a comparison on those actions is not useful. A cached screen state has its
             * actions initialized while a new screen state has not.
             */
            return super.equals(o);
        }
    }

    /**
     * Returns the screen state type.
     *
     * @return Returns the screen state types.
     */
    @Override
    public ScreenStateType getType() {
        return ScreenStateType.ACTION_SCREEN_STATE;
    }
}
