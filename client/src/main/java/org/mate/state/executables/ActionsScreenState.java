package org.mate.state.executables;

import org.mate.MATE;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.MotifAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.state.ScreenStateType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Models a screen state and maintains the list of applicable widget actions.
 */
public class ActionsScreenState extends AbstractScreenState {

    /**
     * Defines the actions that are applicable on the associated screen.
     */
    private List<UIAction> actions;

    /**
     * Represents the app screen with its widgets.
     */
    private final AppScreen appScreen;

    /**
     * Creates a new screen state based on the given {@link AppScreen}.
     *
     * @param appScreen The given app screen.
     */
    public ActionsScreenState(AppScreen appScreen) {
        super(appScreen.getPackageName(), appScreen.getActivityName(), appScreen.getWidgets());
        this.appScreen = appScreen;
        this.actions = null;
    }

    /**
     * Returns the list of widget actions.
     *
     * @return Returns the list of widget actions.
     */
    @Override
    public List<WidgetAction> getWidgetActions() {

        // actions get init lazily
        if (actions == null) {
            actions = getActions();
        }

        List<WidgetAction> widgetActions = new ArrayList<>();
        for (UIAction uiAction : actions) {
            if (uiAction instanceof WidgetAction) {
                widgetActions.add((WidgetAction) uiAction);
            }
        }
        return Collections.unmodifiableList(widgetActions);
    }

    /**
     * Returns the list of motif actions (genes).
     *
     * @return Returns the list of motif actions.
     */
    @Override
    public List<MotifAction> getMotifActions() {

        // actions get init lazily
        if (actions == null) {
            actions = getActions();
        }

        List<MotifAction> motifActions = new ArrayList<>();
        for (UIAction uiAction : actions) {
            if (uiAction instanceof MotifAction) {
                motifActions.add((MotifAction) uiAction);
            }
        }
        return Collections.unmodifiableList(motifActions);
    }

    /**
     * Extracts the list of applicable ui actions on the underlying screen.
     *
     * @return Returns the list of ui actions.
     */
    @Override
    public List<UIAction> getActions() {

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
             * represent leaf widgets in the ui hierarchy. There are four exceptions to this rule:
             * 1) A spinner widget is not a leaf widget but represents a candidate for a widget
             * action. The other possibility would be to apply the action to the text view
             * that is the child element of the spinner.
             * 2) There are widgets like android.support.v7.app.ActionBar$Tab that are
             * (long-)clickable but don't represent leaf widgets. We should apply the action to
             * the widget that is (long-)clickable and ignore the child widget instead, since
             * a static analysis of event handlers can't properly match the widget otherwise.
             * 3) Likewise, it may can happen that checkable widgets are no leaf widgets.
             * 4) Same like spinner widgets, scroll views are no leaf widgets.
             */
            if ((!widget.hasChildren() || widget.isSpinnerType() || widget.isClickable()
                    || widget.isLongClickable() || widget.isCheckable()
                    || widget.isScrollView() || widget.isScrollable())
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
            logWidgetProperties(widget);

            /*
            * TODO: We assign a clickable and long-clickable action if
            *  a widget defines both attributes as true. However, in most cases
            *  the action will refer to the same event handler. We should base
            *  our selection on static analysis in the future.
             */

            /*
             * We should exclude widgets that are part of top status/symbol bar.
             * Otherwise, we may click unintentionally on the wifi symbol and cut off
             * the connection, which in turn breaks MATE's execution. For a device with a
             * resolution of 1080x1920 this represents the area [0,0][1080,72].
             */
            if (appScreen.getStatusBarBoundingBox().contains(widget.getBounds())) {
                MATE.log_debug("Widget within status bar: " + widget.getBounds());
                continue;
            }

            /*
            * It can happen that multiple sibling widgets are completely overlapping each other.
            *  We should define only for a single widget an action.
             */
            if (hasOverlappingSiblingWidgetAction(widget, widgetActions)) {
                MATE.log_debug("Overlapping sibling action!");
                continue;
            }

            if (widget.isSonOfSpinner()) {

                MATE.log_debug("Spinner widget defines scrolling action itself!");

                /*
                * A spinner typically hosts text views as entries, but it could happen
                * that buttons or checkboxes are encapsulated, so we need to check
                * for this properties here. The scrolling action is directly employed
                * on the spinner widget itself.
                 */
                if (widget.isClickable() || widget.isCheckable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
                }

                if (widget.isLongClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.LONG_CLICK));
                }

                // we define the scrolling action directly on the spinner widget
                continue;
            }

            /*
             * It can happen that leaf widgets actually represent containers like
             * a linear layout in order to fill or introduce a gap.
             */
            if (widget.isContainer()) {
                MATE.log_debug("Container as a leaf widget!");
                continue;
            }

            if ((widget.isSonOfLongClickable() || widget.isSonOfClickable()
                    || widget.isSonOfCheckable()) && !widget.isSonOfActionableContainer()) {
                MATE.log_debug("Parent widget defines the action!");
                // we define the action directly on the parent widget
                continue;
            }

            if (widget.isCheckableType()) {
                MATE.log_debug("Widget implements checkable interface!");
                widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
            }

            if (widget.isEditTextType()) {
                MATE.log_debug("Widget is an edit text instance!");
                widgetActions.add(new WidgetAction(widget, ActionType.TYPE_TEXT));

               /*
               * TODO: Use static analysis to detect whether an onclick handler is registered.
               * Editable widgets are by default also clickable and long-clickable, but
               * it is untypical to define such action as well. What should happen?
               * We can only imagine that some sort of pop up appears showing some additional
               * hint. Since it's uncommon that editable widgets define an onclick listener,
               * we ignore such action right now.
                */
               continue;
            }

            if (widget.isButtonType()) {
                MATE.log_debug("Widget is a button instance!");

                // TODO: Use static analysis to detect whether click/long click refer to the same
                //  event handler.
                if (widget.isClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
                }

                if (widget.isLongClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.LONG_CLICK));
                }
            }

            if (widget.isSpinnerType()) {
                MATE.log_debug("Widget is a spinner instance!");

                /*
                * TODO: Add a proper scrolling action. Right now we simply
                *  click on the spinner, which in turn opens a list view,
                *  on which we can click again. However, the better option would
                *  be some sort of motif gene that does this two step operation
                *  in one step.
                 */

                if (widget.isClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
                    // it doesn't make sense to add another action to spinner instance
                    continue;
                }

                if (widget.isLongClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.LONG_CLICK));
                    // it doesn't make sense to add another action to spinner instance
                    continue;
                }
            }

            if (widget.isScrollable() && !widget.isSpinnerType() && !widget.isSonOfScrollable()) {

                MATE.log_debug("Widget is a scrollview!");

                /*
                * Unfortunately, some apps misuse the intended scrolling mechanism, e.g.
                * a horizontal scroll view like android.support.v4.view.ViewPager is used for
                * vertical scrolling by nesting layouts, so it is not possible to determine
                * the direction of the scroll view. Thus, we add swipes for all directions.
                 */
                widgetActions.add(new WidgetAction(widget, ActionType.SWIPE_UP));
                widgetActions.add(new WidgetAction(widget, ActionType.SWIPE_DOWN));
                widgetActions.add(new WidgetAction(widget, ActionType.SWIPE_LEFT));
                widgetActions.add(new WidgetAction(widget, ActionType.SWIPE_RIGHT));

                // it doesn't make sense to add another action to scrollable widgets
                continue;
            }

            /*
             * The elements in a list view are typically of type android.widget.TextView
             * and not clickable according to the underlying AccessibilityNodeInfo object,
             * however those elements represent in most cases clickable widgets.
             */
            if (widget.isSonOfListView()) {
                widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
            }

            // TODO: might be redundant with isCheckableType()
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
        }

        MATE.log_debug("Number of widget actions: " + widgetActions.size());
        MATE.log_debug("Derived the following widget actions: " + widgetActions);

        List<UIAction> uiActions = new ArrayList<UIAction>(widgetActions);
        uiActions.addAll(getUIActions());
        uiActions.addAll(getMotifActions(widgetActions));
        actions = Collections.unmodifiableList(uiActions);
        return actions;
    }

    /**
     * Retrieves the applicable motif actions (genes) for the current screen based on the extracted
     * widget actions.
     *
     * @param widgetActions The extracted widget actions of the current screen.
     * @return Returns a list of applicable motif genes.
     */
    private List<MotifAction> getMotifActions(Set<WidgetAction> widgetActions) {

        List<MotifAction> motifActions = new ArrayList<>();
        motifActions.addAll(extractFillFormAndSubmitActions(widgetActions));
        motifActions.addAll(extractSpinnerScrollActions(widgetActions));

        // TODO: add further motif genes, e.g. scrolling on list views

        return motifActions;
    }

    /**
     * Extracts the possible spinner scroll motif actions. A scroll motif action combines the
     * clicking and scrolling on a spinner. Without this motif action, one has to click first on a
     * spinner, which in turn opens the drop-down menu, and click or scroll to select a different
     * entry.
     *
     * @param widgetActions The set of extracted widget actions.
     * @return Returns the possible spinner motif actions if any.
     */
    private List<MotifAction> extractSpinnerScrollActions(Set<WidgetAction> widgetActions) {

        List<MotifAction> spinnerScrollActions = new ArrayList<>();

        List<WidgetAction> spinnerClickActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getWidget().isClickable()
                        && widgetAction.getWidget().isSpinnerType())
                .collect(Collectors.toList());

        spinnerClickActions.stream().forEach(spinnerClickAction -> {
            MotifAction spinnerScrollAction
                    = new MotifAction(ActionType.SPINNER_SCROLLING, activityName,
                    Collections.singletonList(spinnerClickAction));
            spinnerScrollActions.add(spinnerScrollAction);
        });

        return spinnerScrollActions;
    }

    /**
     * Extracts the possible 'fill form and click submit button' motif actions.
     *
     * @param widgetActions The set of extracted widget actions.
     * @return Returns the possible 'fill form and click submit button' actions if any.
     */
    private List<MotifAction> extractFillFormAndSubmitActions(Set<WidgetAction> widgetActions) {

        List<MotifAction> fillFormAndSubmitActions = new ArrayList<>();

        /*
         * TODO: Extract only those editable widgets and buttons that belong to the same form.
         *  We should exploit the characteristics of the ui tree for that purpose.
         */

        List<WidgetAction> textInsertActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getWidget().isEditTextType())
                .collect(Collectors.toList());

        List<WidgetAction> clickableButtonActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getWidget().isButtonType()
                        && widgetAction.getWidget().isClickable())
                .collect(Collectors.toList());

        if (!textInsertActions.isEmpty() && !clickableButtonActions.isEmpty()) {

            clickableButtonActions.stream().forEach(clickableButtonAction -> {
                List<UIAction> actions = new ArrayList<>(textInsertActions);
                actions.add(clickableButtonAction);
                MotifAction fillFormAndSubmitAction
                        = new MotifAction(ActionType.FILL_FORM_AND_SUBMIT, activityName, actions);
                fillFormAndSubmitActions.add(fillFormAndSubmitAction);
            });
        }

        return fillFormAndSubmitActions;
    }

    @SuppressWarnings("debug")
    private void logWidgetProperties(Widget widget) {

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

    /**
     * Checks whether any sibling widget of the given widget defines already some widget action
     * and overlaps completely with the given widget.
     * This phenomenon was discovered on the bbc app, where multiple siblings were completely
     * overlapping (which is strange per-se) for a video container. One would expect that
     * completely overlapping widgets (same coordinates) are actually not in a siblings relation
     * but rather in a child-parent relation. Moreover, all of these siblings were leaf widgets,
     * which would cause our procedure to assign multiple actions to the same 'abstract' widget.
     *
     * @param widget The current widget.
     * @param widgetActions The widget actions collected so far.
     * @return Returns {@code true} if any sibling of the given widget already defines a widget
     *           action and overlaps with the given widget, otherwise {@code false} is returned.
     */
    private boolean hasOverlappingSiblingWidgetAction(Widget widget, Set<WidgetAction> widgetActions) {

        List<Widget> siblings = widget.getSiblings();

        for (WidgetAction widgetAction : widgetActions) {

            // check whether any sibling defines already an action
            if (siblings.contains(widgetAction.getWidget())) {
                // check whether any sibling overlaps with the current widget
                for (Widget sibling : siblings) {
                    if (sibling.getBounds().equals(widget.getBounds())) {
                        MATE.log_debug("Widget " + widget + " overlaps with " + sibling + "!");
                        return true;
                    }
                }
            }
        }

        return false;
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
        // uiActions.add(new UIAction(ActionType.SLEEP, activityName));
        // uiActions.add(new UIAction(ActionType.WAKE_UP, activityName));
        uiActions.add(new UIAction(ActionType.DELETE, activityName));
        uiActions.add(new UIAction(ActionType.DPAD_CENTER, activityName));
        uiActions.add(new UIAction(ActionType.DPAD_DOWN, activityName));
        uiActions.add(new UIAction(ActionType.DPAD_UP, activityName));
        uiActions.add(new UIAction(ActionType.DPAD_LEFT, activityName));
        uiActions.add(new UIAction(ActionType.DPAD_RIGHT, activityName));
        uiActions.add(new UIAction(ActionType.ENTER, activityName));
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
