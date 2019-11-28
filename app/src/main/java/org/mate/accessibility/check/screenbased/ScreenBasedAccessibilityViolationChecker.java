package org.mate.accessibility.check.screenbased;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
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
        }

        return screenBasedChecks;
    }


    public static void runAccessibilityChecks(IScreenState state) {

        screenBasedChecks = createScreenBasedAccessibilityList();
        MATE.log(">>SCREEN BASED CHECKS");

        for (IScreenAccessibilityCheck screenCheck: screenBasedChecks){
            AccessibilityViolation violation = screenCheck.check(state);
            if (violation!=null) {
                MATE.log("VIOLATION FOUND: " + AccessibilityViolationTypes.NAMES[violation.getType()] + " " + violation.getInfo() );
                MATE.log(" -- extra info: " + violation.getInfo());
            }

        }

        MATE.log("<<SCREEN BASED CHECKS");

    }
}
