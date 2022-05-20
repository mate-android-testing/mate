package org.mate.accessibility.check.bbc.screenbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IScreenAccessibilityCheck;
import org.mate.commons.utils.MATELog;
import org.mate.state.IScreenState;

import java.util.ArrayList;
import java.util.List;

public class ScreenBasedAccessibilityViolationChecker {

    public ScreenBasedAccessibilityViolationChecker(){

    }


    private static List<IScreenAccessibilityCheck> screenBasedChecks = null;

    public static List<IScreenAccessibilityCheck> createScreenBasedAccessibilityList(){
        if (screenBasedChecks==null){
            screenBasedChecks = new ArrayList<IScreenAccessibilityCheck>();
            screenBasedChecks.add(new ColourMeaningAccessibilityCheck());
            screenBasedChecks.add(new RadioGroupCheck());
            screenBasedChecks.add(new ErrorMessageScreenCheck());
        }

        return screenBasedChecks;
    }


    public static List<AccessibilityViolation> runAccessibilityChecks(IScreenState state) {

        List<AccessibilityViolation> violations = new ArrayList<AccessibilityViolation>();
        screenBasedChecks = createScreenBasedAccessibilityList();
        MATELog.log(">>SCREEN BASED CHECKS");

        for (IScreenAccessibilityCheck screenCheck: screenBasedChecks){
            AccessibilityViolation violation = screenCheck.check(state);
            if (violation!=null) {
                violations.add(violation);
                violation.reportFlaw();
                MATELog.log("VIOLATION FOUND: " + violation.getType().getValue() + " " + violation.getInfo() );
                if (!violation.getInfo().equals(""))
                    MATELog.log(" -- extra info: " + violation.getInfo());
            }

        }

        MATELog.log("<<SCREEN BASED CHECKS");
        return violations;
    }
}
