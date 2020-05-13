package org.mate.accessibility.check.wcag;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IAccessibilityViolationChecker;
import org.mate.accessibility.check.wcag.operable.inputmodalities.TargetSizeCheck;
import org.mate.accessibility.check.wcag.operable.navigable.FocusVisibleCheck;
import org.mate.accessibility.check.wcag.perceivable.adaptable.IdentifyInputPurposeCheck;
import org.mate.accessibility.check.wcag.perceivable.distinguishable.ContrastMinimumEnhancedCheck;
import org.mate.accessibility.check.wcag.perceivable.distinguishable.NonTextContrastCheck;
import org.mate.accessibility.check.wcag.perceivable.textalternative.NonTextContentCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityViolationCheckerWCAG implements IAccessibilityViolationChecker {

    private List<IWCAGCheck> accChecks;

    public AccessibilityViolationCheckerWCAG(){
        accChecks = new ArrayList<IWCAGCheck>();

        //1 - PERCEIVABLE

        //1.1.1
        accChecks.add(new NonTextContentCheck());

        //1.3.5
        accChecks.add(new IdentifyInputPurposeCheck());

        //1.4.3 & 1.4.7
        accChecks.add(new ContrastMinimumEnhancedCheck());

        // 1.4.11
        accChecks.add(new NonTextContrastCheck());

        //2.4.7
        accChecks.add(new FocusVisibleCheck());

        //2.5.5
        accChecks.add(new TargetSizeCheck());

    }

    public List<AccessibilityViolation> runAccessibilityChecks(IScreenState state) {

        MATE.log("RUN WCAG CHECKS");

        List<AccessibilityViolation> violations = new ArrayList<AccessibilityViolation>();

        for (Widget widget: state.getWidgets()){
            //MATE.log(">>> " + widget.getText());
            for (IWCAGCheck widgetCheck: accChecks){
                //if (widget.getText().contains("clicar aqui"))
                  //  MATE.log("CHECK: ");
                AccessibilityViolation violation = widgetCheck.check(state,widget);
                if (violation!=null) {
                    violations.add(violation);
                    violation.reportFlaw();
                    MATE.log("VIOLATION FOUND: " + violation.getType().getValue() + " - " + widget.getClazz() + "  " + widget.getId() + " - " + widget.getText() + "  VISIBLE TO TB: " + widget.isScreenReaderFocusable() + "  ACCF: " + widget.isAccessibilityFocused() + "  IFA: " + widget.isImportantForAccessibility());
                    if (!violation.getInfo().equals(""))
                        MATE.log(" -- extra info: " + violation.getInfo() + " " + widget.getBounds());
                }
            }
            //MATE.log("<<< " + widget.getText());
        }
        return violations;
    }
}
