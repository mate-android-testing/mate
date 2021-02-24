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
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * @param widgetId The widget id.
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

        for (Widget widget: widgets) {

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
                    widget.setClickable(true);
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
                        widget.setLongClickable(true);
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
        for (String id: idAmount.keySet()) {
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
        return Collections.unmodifiableList(executables);
    }

    /**
     * Returns the list of actions are applicable independent on any widgets.
     *
     * @return Returns the list of actions applicable on any screen.
     */
    private List<UIAction> getUIActions() {

        List<UIAction> executables = new ArrayList<>();
        executables.add(new UIAction(ActionType.BACK, activityName));
        executables.add(new UIAction(ActionType.MENU, activityName));
        executables.add(new UIAction(ActionType.TOGGLE_ROTATION, activityName));
        // executables.add(new UIAction(ActionType.HOME, activityName));
        executables.add(new UIAction(ActionType.SEARCH, activityName));
        // executables.add(new UIAction(ActionType.QUICK_SETTINGS, activityName));
        // executables.add(new UIAction(ActionType.NOTIFICATIONS, activityName));
        executables.add(new UIAction(ActionType.SLEEP, activityName));
        executables.add(new UIAction(ActionType.WAKE_UP, activityName));
        executables.add(new UIAction(ActionType.DELETE, activityName));
        executables.add(new UIAction(ActionType.DPAD_CENTER, activityName));
        executables.add(new UIAction(ActionType.DPAD_DOWN, activityName));
        executables.add(new UIAction(ActionType.DPAP_UP, activityName));
        executables.add(new UIAction(ActionType.DPAD_LEFT, activityName));
        executables.add(new UIAction(ActionType.DPAD_RIGHT, activityName));

        // swipes are both applicable to widgets and non-widgets
        executables.add(new UIAction(ActionType.SWIPE_DOWN, activityName));
        executables.add(new UIAction(ActionType.SWIPE_UP, activityName));
        executables.add(new UIAction(ActionType.SWIPE_LEFT, activityName));
        executables.add(new UIAction(ActionType.SWIPE_RIGHT, activityName));
        return executables;
    }

    /**
     * Returns the hash code of this screen state.
     *
     * @return Returns the hash code.
     */
    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hashCode(actions);
    }

    /**
     * Compares two screen states for equality.
     *
     * @param o The other screen state to compare against.
     * @return Returns {@code true} if the two screen states are identical,
     *          otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            ActionsScreenState other = (ActionsScreenState) o;
            return super.equals(other) && Objects.equals(actions, other.actions);
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
