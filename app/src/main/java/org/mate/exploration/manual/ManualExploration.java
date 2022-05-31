package org.mate.exploration.manual;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilitySummaryResults;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationChecker;
import org.mate.accessibility.check.bbc.widgetbased.MultipleContentDescCheck;
import org.mate.accessibility.check.bbc.widgetbased.TextContrastRatioAccessibilityCheck;
import org.mate.exploration.Algorithm;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.ActionType;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.state.IScreenState;
import org.mate.utils.Utils;

/**
 * Enables the manual exploration of an app.
 */
public class ManualExploration implements Algorithm {

    private final boolean enableAccessibilityChecks;
    private final UIAbstractionLayer uiAbstractionLayer;

    public ManualExploration(boolean enableAccessibilityChecks) {
        this.enableAccessibilityChecks = enableAccessibilityChecks;
        this.uiAbstractionLayer = Registry.getUiAbstractionLayer();
    }

    @Override
    public void run() {

        String currentActivity = Registry.getUiAbstractionLayer().getCurrentActivity();
        Action manualAction = new UIAction(ActionType.MANUAL_ACTION,
                currentActivity,
                Registry.getDeviceMgr().getCurrentFragments(currentActivity));

        while (true) {

            // check for new state
            if (uiAbstractionLayer.reachedNewState()) {

                IScreenState state = uiAbstractionLayer.getLastScreenState();

                MATE.log_debug("Widgets on screen: ");
                for (Widget w: state.getWidgets()) {
                    MATE.log(w.getClazz() + "-" + w.getId() + "-" + w.getText()
                            + "-" + w.getBounds().toShortString());
                }

                MATE.log_acc("New state: " + state.getId());
                MATE.log_acc("Activity: " + state.getActivityName());


                if (enableAccessibilityChecks) {

                    // record screenshots of new states
                    Registry.getEnvironmentManager().takeScreenshot(state.getPackageName(), state.getId());

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

            // the interval the user has to time to explore the app (before the screen state
            // is re-evaluated)
            Utils.sleep(5000);

            // this basically simulates a dummy action, which in turn causes the state model
            // to check for updates
            uiAbstractionLayer.executeAction(manualAction);
        }
    }
}
