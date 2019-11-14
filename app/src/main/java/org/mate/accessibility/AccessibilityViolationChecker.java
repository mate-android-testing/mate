package org.mate.accessibility;

import org.mate.MATE;
import org.mate.accessibility.check.widgetbased.ContrastRatioAccessibilityCheck;
import org.mate.accessibility.check.widgetbased.FormControlLabelCheck;
import org.mate.accessibility.check.widgetbased.IWidgetAccessibilityCheck;
import org.mate.accessibility.check.widgetbased.WidgetBasedAccessibilityViolationChecker;
import org.mate.state.IScreenState;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityViolationChecker {





    public static void runAccessibilityChecks(IScreenState state){

        MATE.log("RUN ALL ACC CHECKS");

        //checks that came from Google Testing Framework
        /*
        AccessibilityInfoChecker accChecker = new AccessibilityInfoChecker();
        AccessibilitySummaryResults.currentActivityName=state.getActivityName();
        AccessibilitySummaryResults.currentPackageName=state.getPackageName();
        accChecker.runAccessibilityTests(state);

         */

        //new checks

        //widget based
        WidgetBasedAccessibilityViolationChecker.runAccessibilityChecks(state);
    }


}
