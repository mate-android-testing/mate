package org.mate.accessibility.check.bbc;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.screenbased.ScreenBasedAccessibilityViolationChecker;
import org.mate.accessibility.check.bbc.widgetbased.WidgetBasedAccessibilityViolationChecker;
import org.mate.commons.utils.MATELog;
import org.mate.state.IScreenState;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityViolationChecker {

    public static List<AccessibilityViolation> violations = new ArrayList<>();

    public static List<AccessibilityViolation> runAccessibilityChecks(IScreenState state){

        List<AccessibilityViolation> violations = new ArrayList<>();
        MATELog.log("RUN ALL ACC CHECKS");

        //checks that came from Google Testing Framework
        /*
        AccessibilityInfoChecker accChecker = new AccessibilityInfoChecker();
        AccessibilitySummaryResults.currentActivityName=state.getActivityName();
        AccessibilitySummaryResults.currentPackageName=state.getPackageName();
        accChecker.runAccessibilityTests(state);

         */

        //new checks

        //widget based
        MATELog.log("Widget based checks");
        List<AccessibilityViolation> widgetViolations = WidgetBasedAccessibilityViolationChecker.runAccessibilityChecks(state);
        MATELog.log("Screen based checks");
        List<AccessibilityViolation> screenViolations = ScreenBasedAccessibilityViolationChecker.runAccessibilityChecks(state);

        violations.addAll(widgetViolations);
        violations.addAll(screenViolations);
        return violations;
    }


}
