package org.mate.state.executables;

import org.mate.MATE;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.state.IScreenState;
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
     * Defines the actions that are applicable on the screen's widgets.
     */
    private List<WidgetAction> actions;

    /**
     * TODO: What is the purpose of this?
     * Tracks per activity (app screen) each widget and the number of ids.
     */
    private static Map<String, Map<String, List<Integer>>> idSizes = new Hashtable<>();

    /**
     * The app screen on which the screen state is based.
     */
    private AppScreen appScreen;

    /**
     * Creates a new screen state based on the given {@link AppScreen}.
     *
     * @param appScreen The given app screen.
     */
    public ActionsScreenState(AppScreen appScreen){
        super(appScreen.getPackageName(), appScreen.getActivityName(), appScreen.getWidgets());
        MATE.log_debug("Creating new action screen state!");
        this.actions = null;
        this.appScreen = appScreen;
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
     * Extracts the list of applicable widget actions on the underlying screen.
     *
     * @return Returns the list of widget actions.
     */
    @Override
    public List<WidgetAction> getActions() {

        if (actions != null)
            return actions;

        // TODO: what is the purpose of this?
        Map<String, List<Integer>> sameIDWidgets = idSizes.get(activityName);

        if (sameIDWidgets == null) {
            sameIDWidgets = new Hashtable<>();
            idSizes.put(activityName, sameIDWidgets);
        }

        // collect the executable widget actions
        List<WidgetAction> executables = new ArrayList<>();

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
                    widget.setLongClickable(true);
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

                        widget.setScrollable(true);
                    }
                }
            }

            // if we found some editable widget, we should add the action 'ENTER'
            if (foundEditable && !enterAdded) {
                executables.add(new WidgetAction(ActionType.ENTER));
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

        if (appScreen.isHasToScrollDown() || appScreen.isHasToScrollUp()) {
            executables.add(new WidgetAction(ActionType.SWIPE_DOWN));
            executables.add(new WidgetAction(ActionType.SWIPE_UP));
        }

        if (appScreen.isHasToScrollLeft() || appScreen.isHasToScrollRight()) {
            executables.add(new WidgetAction(ActionType.SWIPE_LEFT));
            executables.add(new WidgetAction(ActionType.SWIPE_RIGHT));
        }

        // TODO: maybe we should only allow to press 'BACK', since we can't authenticate
        if (activityName.contains("GoogleOAuthActivity"))
            executables = new ArrayList<>();

        // those actions should be always applicable independent of the widget
        executables.addAll(getWidgetIndependentActions());

        actions = executables;
        return executables;
    }

    /**
     * Returns the list of actions are applicable independent on any widgets.
     *
     * @return Returns the list of actions applicable on any screen.
     */
    private List<WidgetAction> getWidgetIndependentActions() {

        List<WidgetAction> executables = new ArrayList<>();
        executables.add(new WidgetAction(ActionType.BACK));
        executables.add(new WidgetAction(ActionType.MENU));
        executables.add(new WidgetAction(ActionType.TOGGLE_ROTATION));
        // executables.add(new WidgetAction(ActionType.HOME));
        executables.add(new WidgetAction(ActionType.SEARCH));
        // executables.add(new WidgetAction(ActionType.QUICK_SETTINGS));
        // executables.add(new WidgetAction(ActionType.NOTIFICATIONS));
        executables.add(new WidgetAction(ActionType.SLEEP));
        executables.add(new WidgetAction(ActionType.WAKE_UP));
        executables.add(new WidgetAction(ActionType.DELETE));
        executables.add(new WidgetAction(ActionType.DPAD_CENTER));
        executables.add(new WidgetAction(ActionType.DPAD_DOWN));
        executables.add(new WidgetAction(ActionType.DPAP_UP));
        executables.add(new WidgetAction(ActionType.DPAD_LEFT));
        executables.add(new WidgetAction(ActionType.DPAD_RIGHT));
        return executables;
    }

    /**
     * Returns the hash code of this screen state.
     *
     * @return Returns the hash code.
     */
    @Override
    public int hashCode() {
        // TODO: adjust hash code according to equals()
        return Objects.hash(actions);
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

            /*
             * TODO: We should review the comparison of two screen states. The check highly
             *  depends on the underlying model we intend to use. If we want a very fine granulated
             *  comparison, we could simply use equals() of the AppScreen and WidgetAction class.
             *  Otherwise, we need to build the comparison on our own.
             */
            ActionsScreenState that = (ActionsScreenState) o;

            if (!this.activityName.equals(that.getActivityName())) {
                //MATE.log_acc("Strobe 1: State "+this.id+"different from State: "+object.getId());
                return false;
            }

            if (!this.packageName.equals(that.getPackageName())) {
                //MATE.log_acc("Strobe 2: State "+this.id+"different from State: "+object.getId());
                return false;
            }

            ActionsScreenState screenState = (ActionsScreenState) that;

            List<WidgetAction> actionsThis = this.getActions();
            List<WidgetAction> actionsOther = screenState.getActions();

            List<String> setActThis = new ArrayList<>();
            List<String> setActOther = new ArrayList<>();

            for (WidgetAction act : actionsThis) {
                if (act.getWidget() != null) {
                    if (act.getWidget().getClazz().contains("Button"))
                        setActThis.add(act.getWidget().getId() + "-" + act.getActionType() + "-" + act.getWidget().getText());
                    else
                        setActThis.add(act.getWidget().getId() + "-" + act.getActionType());
                }
            }

            for (WidgetAction act : actionsOther) {
                if (act.getWidget() != null) {
                    if (act.getWidget().getClazz().contains("Button"))
                        setActOther.add(act.getWidget().getId() + "-" + act.getActionType() + "-" + act.getWidget().getText());
                    else
                        setActOther.add(act.getWidget().getId() + "-" + act.getActionType());
                }
            }

            if (setActThis.size() == setActOther.size()) {

                for (String strActThis : setActThis) {
                    if (!setActOther.contains(strActThis)) {
                        //MATE.log_acc("Strobe 3: State "+this.id+"different from State: "+object.getId());
                        return false;
                    }
                }

                Map<String, Widget> editablesThis = this.getEditableWidgets();
                Map<String, Widget> editablesOther = screenState.getEditableWidgets();

                for (Widget wdgThis : editablesThis.values()) {

                    Widget wdgOther = editablesOther.get(wdgThis.getId());
                    if (wdgOther == null) {
                        //MATE.log_acc("Strobe 4 State "+this.id+"different from State: "+object.getId());
                        return false;
                    }

                    if (wdgOther.isEmpty() != wdgThis.isEmpty()) {
                        //MATE.log_acc("Strobe 5: State "+this.id+"different from State: "+object.getId());
                        return false;
                    }
                }

                //as for the checkables it considers two GUIs equals if they have the same objects checked
                Map<String, Widget> checkablesThis = this.getCheckableWidgets();
                Map<String, Widget> checkablesOther = screenState.getCheckableWidgets();
                for (Widget wdgThis : checkablesThis.values()) {
                    Widget wdgOther = checkablesOther.get(wdgThis.getId());
                    if (wdgOther == null)
                        return false;
                    if (wdgOther.isChecked() != wdgThis.isChecked())
                        return false;
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Returns the screen state type.
     *
     * @return Returns the screen state types.
     */
    public ScreenStateType getType() {
        return ScreenStateType.ACTION_SCREEN_STATE;
    }

    /**
     * Returns a mapping of editable widgets, where the key is the widget id
     * and the value the actual widget.
     *
     * @return Returns a mapping of editable widgets.
     */
    private Map<String, Widget> getEditableWidgets(){
        Map<String, Widget> editableWidgets = new Hashtable<String, Widget>();
        for (Widget widget : widgets) {
            if (widget.isEditable())
                editableWidgets.put(widget.getId(), widget);
        }
        return editableWidgets;
    }

    /**
     * Returns a mapping of checkable widgets, where the key is the widget id
     * and the value the actual widget.
     *
     * @return Returns a mapping of checkable widgets.
     */
    private Map<String, Widget> getCheckableWidgets(){
        Map<String, Widget> checkableWidgets = new Hashtable<String, Widget>();
        for (Widget widget : widgets) {
            if (widget.isCheckable() || widget.isChecked())
                checkableWidgets.put(widget.getId(), widget);
        }
        return checkableWidgets;
    }

    /**
     * Checks whether two screen states have a different color by
     * comparing pairwise the widgets.
     *
     * @param visitedState The screen state to check against.
     * @return Returns {@code true} if the screen states have a different color,
     *          otherwise {@code false} is returned.
     */
    @Override
    public boolean differentColor(IScreenState visitedState) {

        if (visitedState == null) {
            return true;
        }

        List<Widget> thisWidgets = this.getWidgets();
        List<Widget> otherWidgets = visitedState.getWidgets();

        boolean found = false;

        // compare pairwise the widgets
        for (Widget wThis: thisWidgets) {
            for (Widget wOther: otherWidgets) {
                // check equality by id and text
                if (wThis.getId().equals(wOther.getId()) &&
                        wThis.getText().equals(wOther.getText())) {

                    found = true;

                    if (!wOther.getColor().equals(wThis.getColor()) &&
                            !wOther.isFocused() &&
                            wThis.isFocused() == wOther.isFocused() &&
                            wOther.getHint().equals(wThis.getHint()) &&
                            wOther.getContentDesc().equals(wThis.getContentDesc())) {
                        return true;
                    }
                }
            }

            if (!found) {
                // search by text
                for (Widget wOther: otherWidgets) {

                    if (wThis.getText().equals(wOther.getText())) {
                        found = true;

                        if (!wOther.getColor().equals(wThis.getColor()) &&
                                !wOther.isFocused() &&
                                wThis.isFocused() == wOther.isFocused() &&
                                wOther.getHint().equals(wThis.getHint()) &&
                                wOther.getContentDesc().equals(wThis.getContentDesc()))
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
