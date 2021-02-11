package org.mate.exploration.manual;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationChecker;
import org.mate.accessibility.check.bbc.widgetbased.MultipleContentDescCheck;
import org.mate.accessibility.check.bbc.widgetbased.TextContrastRatioAccessibilityCheck;
import org.mate.exploration.Algorithm;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.state.ScreenStateType;
import org.mate.utils.Utils;

/**
 * Enables the manual exploration of an app.
 */
public class ManualExploration implements Algorithm {

    private final boolean enableAccessibilityChecks;

    public ManualExploration(boolean enableAccessibilityChecks) {
        this.enableAccessibilityChecks = enableAccessibilityChecks;
    }

    @Override
    public void run() {

        Action manualAction = new WidgetAction(ActionType.MANUAL_ACTION);

        while (true) {

            // the interval the user has to time to explore the app (before the screen state
            // is re-evaluated)
            Utils.sleep(1000);

            IScreenState state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);

            boolean foundNewState  = MATE.uiAbstractionLayer.checkIfNewState(state);

            if (foundNewState) {

                MATE.log("Widgets on screen: ");
                for (Widget w: state.getWidgets()) {
                    MATE.log(w.getClazz() + "-" + w.getId() + "-" + w.getText()
                            + "-" + w.getBounds().toShortString());
                }

                // this basically simulates a dummy action, which in turn causes the state model
                // to check for updates
                MATE.uiAbstractionLayer.executeAction(manualAction);

                state = MATE.uiAbstractionLayer.getLastScreenState();
                MATE.log_acc("New state: " + state.getId());
                MATE.log_acc("Visited activity: " + state.getActivityName());

                // record screenshots of new states
                Registry.getEnvironmentManager().takeScreenshot(state.getPackageName(), state.getId());

                if (enableAccessibilityChecks) {
                    AccessibilityViolationChecker.runAccessibilityChecks(state);

                    MultipleContentDescCheck multipleContentDescCheck
                            = new MultipleContentDescCheck();
                    TextContrastRatioAccessibilityCheck contrastChecker
                            = new TextContrastRatioAccessibilityCheck();

                    for (Widget widget: state.getWidgets()) {

                        AccessibilityViolation contrastRatioViolationFound
                                = contrastChecker.check(state, widget);

                        if (contrastRatioViolationFound != null)
                            AccessibilitySummaryResults
                                    .addAccessibilityFlaw("ACCESSIBILITY_CONTRAST_FLAW",
                                            widget, contrastRatioViolationFound.getInfo());

                        AccessibilityViolation multipleContentDescViolationFound
                                = multipleContentDescCheck.check(state, widget);

                        if (multipleContentDescViolationFound != null)
                            AccessibilitySummaryResults
                                    .addAccessibilityFlaw("DUPLICATE_SPEAKABLE_TEXT_FLAW",
                                            widget,"");
                    }
                }
            }
        }
    }
}
