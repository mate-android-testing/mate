package org.mate.accessibility.check.screenbased;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.accessibility.check.widgetbased.ContrastRatioAccessibilityCheck;
import org.mate.accessibility.check.widgetbased.FormControlLabelCheck;
import org.mate.accessibility.check.widgetbased.FormLayoutAccessibilityCheck;
import org.mate.accessibility.check.widgetbased.IScreenAccessibilityCheck;
import org.mate.accessibility.check.widgetbased.IWidgetAccessibilityCheck;
import org.mate.accessibility.check.widgetbased.InputTypeCheck;
import org.mate.accessibility.check.widgetbased.MultipleContentDescCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

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
        MATE.log("RUN WIDGET BASED CHECKS");

        for (IScreenAccessibilityCheck screenCheck: screenBasedChecks){
            AccessibilityViolation violation = screenCheck.check(state);
            if (violation!=null) {
                MATE.log("VIOLATION FOUND: " + AccessibilityViolationTypes.NAMES[violation.getType()] + " " + violation.getInfo() );
                MATE.log(" -- extra info: " + violation.getInfo());
            }

        }

    }
}
