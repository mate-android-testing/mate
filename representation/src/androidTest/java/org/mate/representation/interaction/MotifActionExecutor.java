package org.mate.representation.interaction;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;

public class MotifActionExecutor extends ActionExecutor {

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    public boolean perform(Action action) throws AUTCrashException {
        // executeAction((MotifAction) action);
        return false;
    }

    /**
     * Executes the given motif action.
     *
     * @param action The given motif action.
     * @throws AUTCrashException If the app crashes.
     */
    /*private void executeAction(MotifAction action) throws AUTCrashException {

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
    }*/



    /**
     * Executes the motif action 'fill form and click submit' as used in the Sapienz paper.
     *
     * @param action The given motif action.
     */
    // private void handleFillFormAndSubmit(MotifAction action) {
    //
    //     if (Properties.WIDGET_BASED_ACTIONS()) {
    //
    //         List<UIAction> widgetActions = action.getUIActions();
    //
    //         for (int i = 0; i < widgetActions.size(); i++) {
    //             WidgetAction widgetAction = (WidgetAction) widgetActions.get(i);
    //             if (i < widgetActions.size() - 1) {
    //                 handleEdit(widgetAction.getWidget());
    //             } else {
    //                 // the last widget action represents the click on the submit button
    //                 handleClick(widgetAction.getWidget());
    //             }
    //         }
    //     } else {
    //
    //         if (Registry.isReplayMode()) {
    //             // we simply replay the recorded primitive actions of the motif gene
    //
    //             List<UIAction> primitiveActions = action.getUIActions();
    //
    //             for (int i = 0; i < primitiveActions.size(); i++) {
    //                 PrimitiveAction primitiveAction = (PrimitiveAction) primitiveActions.get(i);
    //                 if (i < primitiveActions.size() - 1) {
    //                     handleEdit(primitiveAction);
    //                 } else {
    //                     // the last primitive action represents the click on the submit button
    //                     handleClick(primitiveAction);
    //                 }
    //             }
    //         } else {
    //
    //             /*
    //              * In the case we use primitive actions we stick to a more 'dynamic' approach. Instead of
    //              * iterating over fixed widgets, we explore the current screen for all available input
    //              * fields and buttons. Then, we fill all input fields and choose a random button for
    //              * clicking. Finally, we save the executed actions for a deterministic replaying.
    //              */
    //             IScreenState screenState = Registry.getUiAbstractionLayer().getLastScreenState();
    //             String currentActivity = screenState.getActivityName();
    //
    //             List<Widget> inputFields = screenState.getWidgets().stream()
    //                     .filter(Widget::isEditTextType)
    //                     .collect(Collectors.toList());
    //
    //             List<Widget> buttons = screenState.getWidgets().stream()
    //                     .filter(Widget::isButtonType)
    //                     .collect(Collectors.toList());
    //
    //             if (!inputFields.isEmpty() && !buttons.isEmpty()) {
    //
    //                 // we choose a button randomly on which we finally click
    //                 Widget button = Randomness.randomElement(buttons);
    //
    //                 List<UIAction> uiActions = new ArrayList<>();
    //
    //                 // execute 'type text' actions and save
    //                 inputFields.stream().forEach(widget -> {
    //                     PrimitiveAction typeText = new PrimitiveAction(widget.getX(), widget.getY(),
    //                             ActionType.TYPE_TEXT, currentActivity);
    //                     handleEdit(typeText);
    //                     uiActions.add(typeText);
    //                 });
    //
    //                 // execute click and save
    //                 PrimitiveAction click = new PrimitiveAction(button.getX(), button.getY(),
    //                         ActionType.CLICK, currentActivity);
    //                 handleClick(click);
    //                 uiActions.add(click);
    //
    //                 // record the actions for a possible deterministic replaying
    //                 action.setUiActions(uiActions);
    //             }
    //         }
    //     }
    // }

    /**
     * Performs the spinner scrolling motif action, i.e. one combines the clicking and selecting
     * of another entry in the drop-down menu.
     *
     * @param spinnerWidget The selected spinner.
     * @param selectedWidget The currently selected entry of nested the drop-down menu.
     */
    // private void handleSpinnerScrolling(Widget spinnerWidget, Widget selectedWidget) {
    //
    //     // click on the spinner first to open the drop-down menu
    //     UiObject2 spinner = findObject(spinnerWidget);
    //
    //     if (spinner == null) {
    //         // we fall back to single click mechanism
    //         MATELog.log_warn("Spinner element couldn't be found!");
    //         handleClick(spinnerWidget);
    //         return;
    //     }
    //
    //     Boolean success = spinner.clickAndWait(Until.newWindow(), 500);
    //
    //     if (success != null && success) {
    //
    //         UiObject2 selectedEntry = findObject(selectedWidget);
    //
    //         if (selectedEntry == null) {
    //             // we fall back to single click mechanism
    //             MATELog.log_warn("Selected entry of spinner couldn't be found!");
    //             handleClick(spinnerWidget);
    //             return;
    //         }
    //
    //         // NOTE: We can't re-use the spinner object; it is not valid anymore!
    //         UiObject2 dropDownMenu = selectedEntry.getParent();
    //
    //         if (dropDownMenu.getChildren().isEmpty()) {
    //             // we fall back to single click mechanism
    //             MATELog.log_warn("Spinner without drop-down menu!");
    //             handleClick(spinnerWidget);
    //             return;
    //         }
    //
    //         /*
    //          * We need to make a deterministic selection, otherwise when we replay such an action,
    //          * we may end up in a different state, which may break replay execution. Thus, we
    //          * simply pick the next entry of the drop-down menu.
    //          */
    //         int index = dropDownMenu.getChildren().indexOf(selectedEntry);
    //         int nextIndex = (index + 1) % dropDownMenu.getChildren().size();
    //         UiObject2 newSelection = dropDownMenu.getChildren().get(nextIndex);
    //
    //         // click on new entry in order to select it
    //         newSelection.click();
    //     }
    // }

    /**
     * Performs a scrolling action on a spinner, i.e. one combines the clicking on the spinner to
     * open the drop-down menu (list view) and the selection of a (different) entry from the
     * drop-down menu.
     *
     * @param action The given motif action.
     */
    // private void handleSpinnerScrolling(MotifAction action) {
    //
    //     if (Properties.WIDGET_BASED_ACTIONS()) {
    //
    //         WidgetAction widgetAction = (WidgetAction) action.getUIActions().get(0);
    //
    //         // retrieve the spinner widget and the selected entry of the dropdown-menu
    //         Widget spinnerWidget = widgetAction.getWidget();
    //         Widget selectedWidget = spinnerWidget.getChildren().get(0);
    //
    //         handleSpinnerScrolling(spinnerWidget, selectedWidget);
    //     } else {
    //
    //         IScreenState screenState = Registry.getUiAbstractionLayer().getLastScreenState();
    //
    //         if (Registry.isReplayMode()) {
    //
    //             /*
    //              * It is possible that the spinner action wasn't actually executed at record time,
    //              * because there was no spinner available. In this case, we can't do anything else
    //              * than simply ignoring the action.
    //              */
    //             if (!action.getUIActions().isEmpty()) {
    //
    //                 // retrieve the recorded spinner
    //                 PrimitiveAction spinnerClickAction = (PrimitiveAction) action.getUIActions().get(0);
    //                 Optional<Widget> spinner = screenState.getWidgets().stream()
    //                         .filter(Widget::isClickable)
    //                         .filter(Widget::isSpinnerType)
    //                         .filter(widget -> widget.getX() == spinnerClickAction.getX())
    //                         .filter(widget -> widget.getY() == spinnerClickAction.getY())
    //                         .findAny();
    //
    //                 if (spinner.isPresent()) {
    //                     Widget spinnerWidget = spinner.get();
    //                     Widget selectedWidget = spinnerWidget.getChildren().get(0);
    //                     handleSpinnerScrolling(spinnerWidget, selectedWidget);
    //                 } else {
    //                     MATELog.log_warn("Couldn't locate spinner at location ("
    //                             + spinnerClickAction.getX() + "," + spinnerClickAction.getY() + ")!");
    //                 }
    //             }
    //         } else {
    //
    //             /*
    //              * If we deal with primitive actions, then we elect a random spinner widget of
    //              * the current screen. In addition, we need to record the executed actions in order to
    //              * make replaying deterministic.
    //              */
    //             List<Widget> spinners = screenState.getWidgets().stream()
    //                     .filter(Widget::isClickable)
    //                     .filter(Widget::isSpinnerType)
    //                     .collect(Collectors.toList());
    //
    //             /*
    //              * If no spinner is available on the current screen, we simply do nothing alike
    //              * a primitive action may have no effect, e.g. a click on a random coordinate which
    //              * area is not covered by any clickable widget.
    //              */
    //             if (!spinners.isEmpty()) {
    //
    //                 // pick a random spinner and retrieve the selected drop-down menu entry
    //                 Widget spinnerWidget = Randomness.randomElement(spinners);
    //                 Widget selectedWidget = spinnerWidget.getChildren().get(0);
    //
    //                 handleSpinnerScrolling(spinnerWidget, selectedWidget);
    //
    //                 PrimitiveAction spinnerClick = new PrimitiveAction(spinnerWidget.getX(),
    //                         spinnerWidget.getY(), ActionType.CLICK, screenState.getActivityName());
    //                 action.setUiActions(Collections.singletonList(spinnerClick));
    //             }
    //         }
    //     }
    // }
}
