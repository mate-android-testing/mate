package org.mate.state.executables;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.intent.IntentProvider;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.intent.IntentAction;
import org.mate.interaction.action.intent.IntentBasedAction;
import org.mate.interaction.action.intent.SystemAction;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Models a screen state and maintains the list of applicable widget actions.
 */
public class ActionsScreenState extends AbstractScreenState {

    /**
     * Defines the ui actions that are applicable on the associated screen.
     */
    private List<UIAction> actions;

    /**
     * Defines the applicable intent actions on any screen.
     */
    private static final List<IntentBasedAction> intentBasedActions;

    /**
     * Defines the applicable system actions on any screen.
     */
    private static final List<SystemAction> systemActions;

    /**
     * Defines the applicable dynamic receiver actions on any screen.
     */
    private static final List<IntentAction> dynamicReceiverIntentActions;

    /**
     * Represents the app screen with its widgets.
     */
    private final AppScreen appScreen;

    static {
        // intent actions are applicable independent of the underlying screen state
        if (Properties.USE_INTENT_ACTIONS()) {
            final IntentProvider intentProvider = new IntentProvider();
            intentBasedActions = intentProvider.getIntentBasedActions();
            systemActions = intentProvider.getSystemActions();
            dynamicReceiverIntentActions = intentProvider.getDynamicReceiverIntentActions();
        } else {
            intentBasedActions = new ArrayList<>();
            systemActions = new ArrayList<>();
            dynamicReceiverIntentActions = new ArrayList<>();
        }
    }

    /**
     * Creates a new screen state based on the given {@link AppScreen}.
     *
     * @param appScreen The given app screen.
     */
    public ActionsScreenState(AppScreen appScreen) {
        super(appScreen.getPackageName(), appScreen.getActivityName(), appScreen.getWidgets());
        this.appScreen = appScreen;
        this.actions = null; // initialized lazily
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
            actions = getWidgetAndUIActions();
        }

        List<WidgetAction> widgetActions = new ArrayList<>();
        for (UIAction uiAction : actions) {
            if (uiAction instanceof WidgetAction) {
                widgetActions.add((WidgetAction) uiAction);
            }
        }
        return widgetActions;
    }

    /**
     * Returns the list of motif actions (genes).
     *
     * @return Returns the list of motif actions.
     */
    @Override
    public List<MotifAction> getMotifActions() {
        return extractMotifActions(getWidgetActions());
    }

    /**
     * Retrieves the list of actions that are applicable on the underlying screen. This list is
     * composed of ui and intent actions.
     *
     * @return Returns the list of all applicable actions on the underlying screen.
     */
    @Override
    public List<Action> getActions() {

        final List<Action> actions = new ArrayList<>();

        if (Properties.USE_UI_ACTIONS()) {
            actions.addAll(getWidgetAndUIActions());
        }

        if (Properties.USE_INTENT_ACTIONS()) {
            actions.addAll(getIntentActions());
        }

        if (Properties.USE_MOTIF_ACTIONS()) {
            actions.addAll(getMotifActions());
        }

        return actions;
    }

    /**
     * Extracts the list of applicable ui actions on the underlying screen.
     *
     * @return Returns the list of ui actions.
     */
    @Override
    public List<UIAction> getUIActions() {

        final List<UIAction> actions = new ArrayList<>();

        if (Properties.USE_UI_ACTIONS()) {
            actions.addAll(getWidgetAndUIActions());
        }

        if (Properties.USE_MOTIF_ACTIONS()) {
            actions.addAll(getMotifActions());
        }

        return actions;
    }

    /**
     * Extracts the list of applicable widget and ui actions on the underlying screen.
     *
     * @return Returns the list of widget and ui actions.
     */
    private List<UIAction> getWidgetAndUIActions() {

        // check whether the actions have been requested once
        if (actions != null) {
            return actions;
        }

        if (activityName.contains("GoogleOAuthActivity")) {
            // we can't authenticate here, so only allow to press 'BACK'
            return Collections.singletonList(new UIAction(ActionType.BACK, activityName));
        }
        
        MATE.log_debug("Retrieving widget actions for screen state...");
        MATE.log_debug("Number of all widgets: " + this.widgets.size());

        // Check whether there is a drawer layout used for the navigation.
        final Widget drawerLayout = getWidgets().stream()
                .filter(Widget::isDrawerLayout)
                .findFirst()
                .orElse(null);

        // See https://developer.android.com/reference/androidx/drawerlayout/widget/DrawerLayout.
        final Predicate<Widget> isNavigationWidget = w -> {
            // Although there is a convention that the first child should refer to the main content
            // view and the second child to the navigation menu, this rule is not followed by all
            // apps. To circumvent this problem, we consider the size of the widgets and assume
            // that the smaller widget in width refers to the navigation menu.
            final Widget firstChild = drawerLayout.getChildren().get(0);
            final Widget secondChild = drawerLayout.getChildren().get(1);
            final Widget navigationElementContainer = firstChild.getWidth() < secondChild.getWidth()
                    ? firstChild : secondChild;
            return w.isSonOf(parent -> parent.equals(navigationElementContainer));
        };

        List<Widget> widgets = new ArrayList<>();

        for (Widget widget : this.widgets) {
            /*
             * We ignore here primarily all widgets that are not visible and don't represent leaf
             * widgets in the ui hierarchy. There are four exceptions to this rule:
             *
             * 1) A spinner widget is not a leaf widget but represents a candidate for a widget
             * action. The other possibility would be to apply the action to the text view
             * that is the child element of the spinner.
             * 2) There are widgets like android.support.v7.app.ActionBar$Tab that are
             * (long-)clickable but don't represent leaf widgets. We should apply the action to
             * the widget that is (long-)clickable and ignore the child widget instead, since
             * a static analysis of event handlers can't properly match the widget otherwise.
             * 3) Likewise, it may can happen that checkable widgets are no leaf widgets.
             * 4) Same like spinner widgets, scroll views are no leaf widgets.
             *
             * Note that we can't rely upon certain widget attributes, e.g. clickable or enabled,
             * to pre-exclude further widgets, because those attributes are static and may change
             * until we define actions on them, e.g. a button might be initially not clickable until
             * certain forms are filled out.
             *
             * Note that certain widgets can be already parsed but are not visible at that time.
             * For instance, items in a scroll view can fall below the visible screen. It is not
             * necessary to check for the bounds (Y coordinates), the 'visible' property seems to be
             * stable in that case.
             */
            if ((widget.isLeafWidget() || widget.isSpinnerType() || widget.isActionable()
                    || widget.isScrollView() || widget.isScrollable())
                    && widget.isVisible()) {
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
                continue;
            }

            /*
            * There is a specific combinations of two text views that describe a setting with a
            * headline and a summary. Instead of defining the click on both text views, it is
            * sufficient to define it on the parent widget. This should simply reduce the number of
            * redundant actions.
             */
            if (widget.isSettingsOption()) {
                widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
                continue;
            }

            if (widget.isSonOf(Widget::isSettingsOption)) {
                // we define the click action directly on the parent widget
                continue;
            }

            /*
            * It can happen that multiple sibling widgets are completely overlapping each other.
            *  We should define only for a single widget an action.
             */
            if (hasOverlappingSiblingWidgetAction(widget, widgetActions)) {
                continue;
            }

            /*
             * NOTE: For a navigation menu a drawer layout might be used. A drawer stores in its
             * child nodes the navigation menu and the content of the current screen and shows
             * depending on the state either the navigation menu or the content of the current screen.
             * However, the UIAutomator API isn't aware of this and returns the widgets of both views,
             * while we can only interact with one set of widgets at a time. Although there is a
             * convention that states that the main content view should be stored in the first child
             * and the navigation menu in the second child, there are apps that don't follow this
             * rule, thus we can't rely on this to determine which widgets belong to the navigation
             * view. Instead, we consider the widths of children. The child widget that spans over
             * the entire screen refers to the main content view, while the smaller widget refers to
             * the navigation menu.
             */
            if (drawerLayout != null && drawerLayout.getChildren().size() > 1
                    && !isNavigationWidget.test(widget)) {
                continue;
            }

            if (widget.isSpinnerType() && widget.hasChildren()) {

                if (widget.isClickable() || widget.isLongClickable() || widget.isScrollable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.CHANGE_SPINNER));
                }

                // it doesn't make sense to add another action to spinner instance
                continue;
            }

            // NOTE: It seems rather so that children of a spinner are bare text views that should
            // not convey any action, only the list view that is spanned by the spinner contains
            // clickable items, but there might be exceptions from this rule.
            if (widget.isLeafWidget() && widget.isSonOfSpinner()) {

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

            // There are scroll views that are not scrollable at all, but the property 'isScrollable'
            // is more reliable in such cases.
            if (widget.isScrollable() && !widget.isSpinnerType() && !widget.isSonOfScrollable()) {

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
            if (widget.isLeafWidget() && widget.isSonOfListView()) {

                if (widget.isClickable() || widget.isCheckable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
                }

                if (widget.isLongClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.LONG_CLICK));
                }

                Widget parent = widget.getParent();

                while (!parent.isListViewType()) {
                    parent = parent.getParent();
                }

                // inherit the clickable properties of the list view
                if (parent.isLongClickable()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.LONG_CLICK));
                }

                // make widget in any case clickable
                widgetActions.add(new WidgetAction(widget, ActionType.CLICK));

                continue;
            }

            /*
             * It can happen that leaf widgets actually represent containers like
             * a linear layout in order to fill or introduce a gap.
             */
            if (widget.isLeafWidget() && widget.isContainer()) {
                continue;
            }

            /*
            * This happens for instance for androidx.appcompat.app.ActionBar$Tab. It seems like
            * this type of widget is used for navigation and the underlying text view is merely
            * defining the text. We can save here defining a click action for both the text view
            * and the overlying navigation widget.
             */
            if (widget.isSonOf(Widget::isActionable) && !widget.isSonOfActionableContainer()) {
                // we define the action directly on the parent widget
                continue;
            }

            /*
            * There might be leaf widgets like a combination of a image view (symbol) and a text
            * view that are part of a common actionable layout. Instead of propagating the actions
            * of the shared layout to the individual leaf widgets, we apply the actions directly on
            * the parent. This should reduce the number of redundant actions.
             */
            if (widget.isLeafWidget() && widget.isSonOfActionableContainer()) {
                // we define the action directly on the parent widget
                continue;
            }

            /*
             * There are edit text widgets that cannot be modified directly but via a button, e.g.
             * when setting a date in such a field. In those cases, the edit text widget is likely
             * not enabled.
             */
            if (widget.isEditTextType()) {

                if (widget.isEnabled()) {
                    widgetActions.add(new WidgetAction(widget, ActionType.TYPE_TEXT));
                    widgetActions.add(new WidgetAction(widget, ActionType.CLEAR_TEXT));
                }

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

            if (widget.isSeekBar() || widget.isRatingBar()) {
                widgetActions.add(new WidgetAction(widget, ActionType.CHANGE_SEEK_BAR));

                // it doesn't make sense to add another action to seek or rating bars
                continue;
            }

            // These comprises any widget that is checkable from check boxes to checkable text views.
            if (widget.isCheckable() || widget.isCheckableType() || widget.isSwitch()) {
                widgetActions.add(new WidgetAction(widget, ActionType.CHANGE_CHECKABLE));

                // it doesn't make sense to add another action to a checkable widget
                continue;
            }

            // TODO: Use static analysis to detect whether click/long click refer to the same
            //  event handler.
            if (widget.isClickable()) {
                widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
            }

            if (widget.isLongClickable()) {
                widgetActions.add(new WidgetAction(widget, ActionType.LONG_CLICK));
            }

            if (widget.isLeafWidget()
                    // ignore placeholder widgets
                    && widget.getHeight() > 5
                    && (widget.hasText() || widget.hasContentDescription())) {
                /*
                 * Right now, we can't tell whether any kind of view widget should be clickable
                 * or not, thus we assign to each leaf widget the click action. In the future,
                 * we should rely on an additional static analysis of the byte code to verify
                 * which leaf widget, in particular which text view, defines an event handler
                 * and thus should be clickable.
                 */
                widgetActions.add(new WidgetAction(widget, ActionType.CLICK));
            }
        }

        MATE.log_debug("Number of widget actions: " + widgetActions.size());

        List<UIAction> uiActions = new ArrayList<>(widgetActions);
        uiActions.addAll(getIndependentUIActions());
        actions = uiActions;
        return actions;
    }

    /**
     * Retrieves the applicable motif actions (genes) for the current screen based on the extracted
     * widget actions.
     *
     * @param widgetActions The extracted widget actions of the current screen.
     * @return Returns a list of applicable motif genes.
     */
    private List<MotifAction> extractMotifActions(List<WidgetAction> widgetActions) {

        final List<MotifAction> motifActions = new ArrayList<>();
        motifActions.addAll(extractFillFormAndSubmitActions(widgetActions));
        motifActions.addAll(extractMenuAndSelectItemActions(widgetActions));
        motifActions.addAll(extractSortAndSelectSortOrderActions(widgetActions));
        motifActions.addAll(extractOpenNavigationAndSelectOptionActions(widgetActions));
        motifActions.addAll(extractTypeTextAndPressEnterActions(widgetActions));
        motifActions.addAll(extractChangeRadioGroupSelectionActions(widgetActions));
        motifActions.addAll(extractChangeListViewSelectionActions(widgetActions));
        motifActions.addAll(extractChangeSeekBarsActions(widgetActions));
        motifActions.addAll(extractDatePickerActions(widgetActions));
        motifActions.addAll(extractChangeCheckableActions(widgetActions));
        motifActions.addAll(extractChangeSpinnerActions(widgetActions));
        motifActions.addAll(extractFillFormAndScrollActions(widgetActions));
        return motifActions;
    }

    /**
     * Extracts the possible swap list items motif actions. This motif action swaps two list items.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible swap list items motif actions if any.
     */
    @SuppressWarnings("unused")
    private List<MotifAction> extractSwapListItemsActions(final List<WidgetAction> widgetActions) {

        final List<MotifAction> swapListItemsActions = new ArrayList<>();

        /*
        * TODO: Unfortunately there is no proper way to detect whether list items are swappable.
        *  Although most implementations use an image view positioned at the right of a list view
        *  item that can be dragged and dropped, there is no common name such that we could identify
        *  these swappable widgets. The only viable option right now would be to overapproximate
        *  and define for each list view such an action even if not applicable.
         */

        // TODO: There might be multiple list views visible, thus we should group the list view items
        //  accordingly and allow only swapping between two items of the same list view.
        final List<Widget> listItems = getWidgets().stream()
                .filter(Widget::isLeafWidget)
                .filter(Widget::isImageView)
                // TODO: Find proper identifier for swappable widgets.
                .filter(widget -> widget.getContentDesc().equalsIgnoreCase("swap"))
                // TODO: In most cases it is the right-most (last) child.
                .filter(widget -> widget.isSonOf(Widget::isRecyclerViewType)
                        || widget.isSonOf(Widget::isListViewType))
                .collect(Collectors.toList());

        if (listItems.size() > 1) { // there are at least two list items
            final MotifAction swapListItems = new MotifAction(ActionType.SWAP_LIST_ITEMS, activityName);
            swapListItems.setWidgets(listItems);
            swapListItemsActions.add(swapListItems);
        }

        return swapListItemsActions;
    }

    /**
     * Extracts the possible fill form and scroll motif actions. This motif action combines filling
     * out a form and scrolling down until in a repeated manner until the end of the page is reached.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible fill form and scroll motif actions if any.
     */
    private List<MotifAction> extractFillFormAndScrollActions(final List<WidgetAction> widgetActions) {

        final List<MotifAction> fillFormAndScrollActions = new ArrayList<>();

        final List<WidgetAction> textInsertActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getWidget().isEditTextType())
                .filter(widgetAction -> widgetAction.getActionType() != ActionType.CLEAR_TEXT)
                .collect(Collectors.toList());

        final WidgetAction scrollAction = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getActionType() == ActionType.SWIPE_UP)
                // the scrolling view should span over the entire screen
                .filter(widgetAction -> appScreen.getWidth() == widgetAction.getWidget().getWidth())
                .findFirst().orElse(null);

        if (!textInsertActions.isEmpty() && scrollAction != null) {
            final MotifAction fillFormAndScrollAction = new MotifAction(ActionType.FILL_FORM_AND_SCROLL,
                    activityName, Collections.singletonList(scrollAction));
            fillFormAndScrollActions.add(fillFormAndScrollAction);
        }

        return fillFormAndScrollActions;
    }

    /**
     * Extracts the possible change checkables motif actions. This motif action changes multiple
     * checkables visible on the current screen.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible change checkables motif actions if any.
     */
    private List<MotifAction> extractChangeCheckableActions(final List<WidgetAction> widgetActions) {

        final List<MotifAction> changeCheckableActions = new ArrayList<>();

        final List<WidgetAction> changeCheckableWidgetActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getActionType() == ActionType.CHANGE_CHECKABLE)
                .collect(Collectors.toList());

        if (changeCheckableWidgetActions.size() > 1) { // at least two checkables need to be present

            final MotifAction changeCheckBoxAction = new MotifAction(ActionType.CHANGE_CHECKABLES,
                    activityName, Collections.unmodifiableList(changeCheckableWidgetActions));

            changeCheckableActions.add(changeCheckBoxAction);
        }

        return changeCheckableActions;
    }

    /**
     * Extracts the possible change spinners motif actions. This motif action changes multiple
     * entries of spinners at once.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible change spinners motif actions if any.
     */
    private List<MotifAction> extractChangeSpinnerActions(final List<WidgetAction> widgetActions) {

        final List<MotifAction> changeSpinnerActions = new ArrayList<>();

        final List<WidgetAction> changeSpinnerWidgetActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getActionType() == ActionType.CHANGE_SPINNER)
                .collect(Collectors.toList());

        if (changeSpinnerWidgetActions.size() > 1) { // at least two spinners need to be present

            final MotifAction changeSpinnerAction = new MotifAction(ActionType.CHANGE_SPINNERS,
                    activityName, Collections.unmodifiableList(changeSpinnerWidgetActions));

            changeSpinnerActions.add(changeSpinnerAction);
        }

        return changeSpinnerActions;
    }

    /**
     * Extracts the possible change date motif actions. This motif action changes the selected date
     * of a date picker.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible change date motif actions if any.
     */
    private List<MotifAction> extractDatePickerActions(final List<WidgetAction> widgetActions) {

        final List<MotifAction> datePickerActions = new ArrayList<>();

        if (widgets.stream().anyMatch(Widget::isDatePicker)) {

            final Predicate<WidgetAction> prevAndNextButtonMatcher = widgetAction -> {
                final Widget widget = widgetAction.getWidget();
                return widget.isImageButtonType()
                        && (widget.getContentDesc().toLowerCase().contains("next")
                        || widget.getContentDesc().toLowerCase().contains("previous"));
            };

            final List<WidgetAction> prevAndNextButtonClickActions = widgetActions.stream()
                    .filter(prevAndNextButtonMatcher)
                    .filter(widgetAction -> widgetAction.getActionType() == ActionType.CLICK)
                    .collect(Collectors.toList());

            final MotifAction datePickerAction
                    = new MotifAction(ActionType.CHANGE_DATE, activityName,
                    Collections.unmodifiableList(prevAndNextButtonClickActions));
            datePickerActions.add(datePickerAction);
        }

        return datePickerActions;
    }

    /**
     * Extracts the possible change seek bars motif actions. This motif action changes multiple
     * seek bars at once.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible change seek bars motif actions if any.
     */
    private List<MotifAction> extractChangeSeekBarsActions(final List<WidgetAction> widgetActions) {

        final List<MotifAction> changeSeekBarsActions = new ArrayList<>();

        final List<WidgetAction> changeSeekBarActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getActionType() == ActionType.CHANGE_SEEK_BAR)
                .collect(Collectors.toList());

        if (changeSeekBarActions.size() > 1) { // there are at least two seek bars
            final MotifAction changeSeekBarsAction
                    = new MotifAction(ActionType.CHANGE_SEEK_BARS, activityName,
                    Collections.unmodifiableList(changeSeekBarActions));
            changeSeekBarsActions.add(changeSeekBarsAction);
        }

        return changeSeekBarsActions;
    }

    /**
     * Extracts the possible change list view selection motif actions. This motif action changes
     * the current selection of a list view / recycler view by clicking on a random list item.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible list view selection motif actions if any.
     */
    private List<MotifAction> extractChangeListViewSelectionActions(
            final List<WidgetAction> widgetActions) {

        final List<MotifAction> changeListViewSelectionActions = new ArrayList<>();

        final Predicate<WidgetAction> listViewItemActionsMatcher = widgetAction -> {
            final Widget widget = widgetAction.getWidget();
            return widgetAction.getActionType() == ActionType.CLICK
                    && widget.isEnabled()
                    && widget.isLeafWidget()
                    && widget.isTextViewType()
                    && widget.hasText()
                    && widget.hasResourceID()
                    && (widget.isSonOf(Widget::isListViewType)
                        || widget.isSonOf(Widget::isRecyclerViewType));
        };

        final List<WidgetAction> listViewItemActions = widgetActions.stream()
                .filter(listViewItemActionsMatcher)
                .collect(Collectors.toList());

        if (!listViewItemActions.isEmpty()) {
            final MotifAction changeListViewSelectionAction
                    = new MotifAction(ActionType.CHANGE_LIST_VIEW_SELECTION, activityName,
                    Collections.unmodifiableList(listViewItemActions));
            changeListViewSelectionActions.add(changeListViewSelectionAction);
        }

        return changeListViewSelectionActions;
    }

    /**
     * Extracts the possible change radio group selections motif actions. This motif action changes
     * the different radio group selections by clicking on a random radio button per radio group.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible change radio group selections motif actions if any.
     */
    private List<MotifAction> extractChangeRadioGroupSelectionActions(
            final List<WidgetAction> widgetActions) {

        final List<MotifAction> changeRadioGroupSelectionActions = new ArrayList<>();

        final Predicate<WidgetAction> radioButtonActionMatcher = widgetAction -> {
            final Widget widget = widgetAction.getWidget();
            return widgetAction.getActionType() == ActionType.CLICK
                    && widget.isEnabled()
                    && widget.isLeafWidget()
                    && widget.isRadioButtonType()
                    && widget.isSonOf(Widget::isRadioGroupType);
        };

        final List<WidgetAction> radioButtonActions = widgetActions.stream()
                .filter(radioButtonActionMatcher)
                .collect(Collectors.toList());
        
        // TODO: Store the radio button action per radio group in the motif action.

        if (!radioButtonActions.isEmpty()) {
            final MotifAction changeRadioGroupSelectionAction
                    = new MotifAction(ActionType.CHANGE_RADIO_GROUP_SELECTIONS, activityName);
            changeRadioGroupSelectionActions.add(changeRadioGroupSelectionAction);
        }

        return changeRadioGroupSelectionActions;
    }

    /**
     * Extracts the possible type text and press enter motif actions. This motif action
     * combines the text insertion and pressing enter.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible type text and press enter motif actions if any.
     */
    private List<MotifAction> extractTypeTextAndPressEnterActions(
            final List<WidgetAction> widgetActions) {

        final List<MotifAction> typeTextAndPressEnterActions = new ArrayList<>();

        final List<WidgetAction> typeTextActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getActionType() == ActionType.TYPE_TEXT)
                .collect(Collectors.toList());

        typeTextActions.stream().forEach(typeTextAction -> {
            MotifAction typeTextAndPressEnterAction
                    = new MotifAction(ActionType.TYPE_TEXT_AND_PRESS_ENTER, activityName,
                    Collections.singletonList(typeTextAction));
            typeTextAndPressEnterActions.add(typeTextAndPressEnterAction);
        });

        return typeTextAndPressEnterActions;
    }

    /**
     * Extracts the possible open navigation menu and option selection motif actions. This motif action
     * combines the clicking on the navigation menu and selecting a possibly different option.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible open navigation menu and select option motif actions if any.
     */
    private List<MotifAction> extractOpenNavigationAndSelectOptionActions(
            final List<WidgetAction> widgetActions) {

        final List<MotifAction> openNavigationAndSelectOptionActions = new ArrayList<>();

        final Predicate<String> contentMatcher = content -> (content.toLowerCase().contains("navigation")
                || content.toLowerCase().contains("nav")
                || content.toLowerCase().contains("open"))
                && !content.toLowerCase().contains("close");

        final Widget drawerLayout = widgets.stream()
                .filter(Widget::isDrawerLayout)
                // The navigation menu has exactly one child when not opened.
                .filter(widget -> widget.getChildren().size() == 1)
                .findFirst()
                .orElse(null);

        if (drawerLayout != null) {

            // Locate the navigation menu.
            final List<WidgetAction> navigationMenuClickActions = widgetActions.stream()
                    .filter(widgetAction -> widgetAction.getWidget().isImageButtonType()
                            && widgetAction.getActionType() == ActionType.CLICK
                            && contentMatcher.test(widgetAction.getWidget().getContentDesc())
                            && appScreen.getMenuBarBoundingBox().contains(
                            widgetAction.getWidget().getBounds()))
                    .collect(Collectors.toList());

            navigationMenuClickActions.stream().forEach(navigationMenuClickAction -> {
                MotifAction openNavigationAndOptionSelectAction
                        = new MotifAction(ActionType.OPEN_NAVIGATION_AND_OPTION_SELECTION, activityName,
                        Collections.singletonList(navigationMenuClickAction));
                openNavigationAndSelectOptionActions.add(openNavigationAndOptionSelectAction);
            });
        }

        return openNavigationAndSelectOptionActions;
    }

    /**
     * Extracts the possible open sort menu and sort order select motif actions. This motif action
     * combines the clicking on the sort menu and selecting a possibly different sort order.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible open sort menu and select sort order motif actions if any.
     */
    private List<MotifAction> extractSortAndSelectSortOrderActions(final List<WidgetAction> widgetActions) {

        final List<MotifAction> sortClickAndSelectSortOrderActions = new ArrayList<>();

        // Locate the sort order menu.
        final List<WidgetAction> sortClickActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getWidget().isTextViewType()
                        && widgetAction.getActionType() == ActionType.CLICK
                        && widgetAction.getWidget().getContentDesc().equals("Sort"))
                .collect(Collectors.toList());

        sortClickActions.stream().forEach(menuClickAction -> {
            MotifAction menuClickAndItemSelectAction
                    = new MotifAction(ActionType.SORT_MENU_CLICK_AND_SORT_ORDER_SELECTION, activityName,
                    Collections.singletonList(menuClickAction));
            sortClickAndSelectSortOrderActions.add(menuClickAndItemSelectAction);
        });

        return sortClickAndSelectSortOrderActions;
    }

    /**
     * Extracts the possible menu and item select motif actions. A menu and select item motif action
     * combines the clicking on the menu and selecting a not yet selected menu item.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible extract menu and select item motif actions if any.
     */
    private List<MotifAction> extractMenuAndSelectItemActions(final List<WidgetAction> widgetActions) {

        final List<MotifAction> menuClickAndItemSelectActions = new ArrayList<>();

        // Locate the menu item, alternatively one could invoke the MENU ui action.
        final List<WidgetAction> menuClickActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getWidget().isImageView()
                        && widgetAction.getActionType() == ActionType.CLICK
                        && widgetAction.getWidget().getContentDesc().equals("More options"))
                .collect(Collectors.toList());

        menuClickActions.stream().forEach(menuClickAction -> {
            MotifAction menuClickAndItemSelectAction
                    = new MotifAction(ActionType.MENU_CLICK_AND_ITEM_SELECTION, activityName,
                    Collections.singletonList(menuClickAction));
            menuClickAndItemSelectActions.add(menuClickAndItemSelectAction);
        });

        return menuClickAndItemSelectActions;
    }

    /**
     * NOTE: This motif action have been replaced by a widget action for a single spinner and a
     * motif action for changing multiple spinners at once.
     *
     * Extracts the possible spinner scroll motif actions. A scroll motif action combines the
     * clicking and scrolling on a spinner. Without this motif action, one has to click first on a
     * spinner, which in turn opens the drop-down menu, and click or scroll to select a different
     * entry.
     *
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible spinner motif actions if any.
     */
    @SuppressWarnings("unused")
    private List<MotifAction> extractSpinnerScrollActions(List<WidgetAction> widgetActions) {

        List<MotifAction> spinnerScrollActions = new ArrayList<>();

        List<WidgetAction> spinnerClickActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getWidget().isClickable()
                        && widgetAction.getWidget().isSpinnerType())
                .filter(widgetAction -> widgetAction.getWidget().hasChildren())
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
     * @param widgetActions The list of extracted widget actions.
     * @return Returns the possible 'fill form and click submit button' actions if any.
     */
    private List<MotifAction> extractFillFormAndSubmitActions(List<WidgetAction> widgetActions) {

        final List<MotifAction> fillFormAndSubmitActions = new ArrayList<>();

        /*
         * TODO: Extract only those editable widgets and buttons that belong to the same form.
         *  We should exploit the characteristics of the ui tree for that purpose.
         */

        final List<WidgetAction> textInsertActions = widgetActions.stream()
                .filter(widgetAction -> widgetAction.getWidget().isEditTextType())
                .filter(widgetAction -> widgetAction.getActionType() != ActionType.CLEAR_TEXT)
                .collect(Collectors.toList());

        final Predicate<WidgetAction> clickableButtons = widgetAction -> {
            final Widget widget = widgetAction.getWidget();
            return widget.isButtonType()
                    // Certain buttons, e.g. CheckBoxes or RadioButtons, are checkable, but not
                    // desired here, i.e. we want to press only a submit/save button (regular buttons).
                    && !widget.isCheckableType()
                    && !appScreen.getMenuBarBoundingBox().contains(widget.getBounds());
        };

        // The 'save' button might be represented as a text view in the menu bar.
        final Predicate<WidgetAction> clickableTextView = widgetAction -> {
            final Widget widget = widgetAction.getWidget();
            return widget.isTextViewType()
                    && appScreen.getMenuBarBoundingBox().contains(widget.getBounds())
                    && (widget.getContentDesc().equalsIgnoreCase("Save changes")
                        || widget.getContentDesc().equalsIgnoreCase("Save"));
        };

        final List<WidgetAction> clickableActions = widgetActions.stream()
                .filter(clickableButtons.or(clickableTextView))
                .filter(widgetAction -> widgetAction.getActionType() == ActionType.CLICK)
                .collect(Collectors.toList());

        if (!textInsertActions.isEmpty() && !clickableActions.isEmpty()) {

            clickableActions.stream().forEach(clickableAction -> {
                final List<UIAction> actions = new ArrayList<>(textInsertActions);
                actions.add(clickableAction);
                final MotifAction fillFormAndSubmitAction
                        = new MotifAction(ActionType.FILL_FORM_AND_SUBMIT, activityName, actions);
                fillFormAndSubmitActions.add(fillFormAndSubmitAction);
            });
        } else if (!textInsertActions.isEmpty()) { // no submit/save button discovered
            final List<UIAction> actions = new ArrayList<>(textInsertActions);
            final MotifAction fillFormAction
                    = new MotifAction(ActionType.FILL_FORM, activityName, actions);
            fillFormAndSubmitActions.add(fillFormAction);
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
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns the list of ui actions that are applicable independent of any widgets.
     *
     * @return Returns the list of ui actions applicable on any screen.
     */
    private List<UIAction> getIndependentUIActions() {

        List<UIAction> uiActions = new ArrayList<>();
        uiActions.add(new UIAction(ActionType.BACK, activityName));
        uiActions.add(new UIAction(ActionType.MENU, activityName));
        uiActions.add(new UIAction(ActionType.TOGGLE_ROTATION, activityName));
        // uiActions.add(new UIAction(ActionType.HOME, activityName));
        // uiActions.add(new UIAction(ActionType.SEARCH, activityName));
        // uiActions.add(new UIAction(ActionType.QUICK_SETTINGS, activityName));
        // uiActions.add(new UIAction(ActionType.NOTIFICATIONS, activityName));
        // uiActions.add(new UIAction(ActionType.SLEEP, activityName));
        // uiActions.add(new UIAction(ActionType.WAKE_UP, activityName));
        uiActions.add(new UIAction(ActionType.DELETE, activityName));
        // uiActions.add(new UIAction(ActionType.DPAD_CENTER, activityName));
        // uiActions.add(new UIAction(ActionType.DPAD_DOWN, activityName));
        // uiActions.add(new UIAction(ActionType.DPAD_UP, activityName));
        // uiActions.add(new UIAction(ActionType.DPAD_LEFT, activityName));
        // uiActions.add(new UIAction(ActionType.DPAD_RIGHT, activityName));
        uiActions.add(new UIAction(ActionType.ENTER, activityName));
        return uiActions;
    }

    /**
     * Retrieves the applicable intent actions.
     *
     * @return Returns the list of applicable intent actions.
     */
    @Override
    public List<IntentAction> getIntentActions() {
        final List<IntentAction> intentActions = new ArrayList<>();
        intentActions.addAll(getIntentBasedActions());
        intentActions.addAll(getSystemActions());
        intentActions.addAll(dynamicReceiverIntentActions);
        return intentActions;
    }

    /**
     * Retrieves the applicable intent-based actions.
     *
     * @return Returns the list of applicable intent-based actions.
     */
    @Override
    public List<IntentBasedAction> getIntentBasedActions() {
        return intentBasedActions;
    }

    /**
     * Retrieves the applicable system actions.
     *
     * @return Returns the list of applicable system actions.
     */
    @Override
    public List<SystemAction> getSystemActions() {
        return systemActions;
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
