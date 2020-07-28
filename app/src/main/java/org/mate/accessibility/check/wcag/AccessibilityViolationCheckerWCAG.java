package org.mate.accessibility.check.wcag;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IAccessibilityViolationChecker;
import org.mate.accessibility.check.wcag.bbc.DescriptiveLinksCheck;
import org.mate.accessibility.check.wcag.bbc.AcionableElementsCheck;
import org.mate.accessibility.check.wcag.bbc.FormLayoutAccessibilityCheck;
import org.mate.accessibility.check.wcag.bbc.ManagingFocusCheck;
import org.mate.accessibility.check.wcag.bbc.MultipleContentDescCheck;
import org.mate.accessibility.check.wcag.bbc.PhantomElementCheck;
import org.mate.accessibility.check.wcag.bbc.SpacingAccessibilityCheck;
import org.mate.accessibility.check.wcag.operable.inputmodalities.TargetSizeCheck;
import org.mate.accessibility.check.wcag.operable.navigable.FocusVisibleCheck;
import org.mate.accessibility.check.wcag.operable.navigable.PageTitledCheck;
import org.mate.accessibility.check.wcag.perceivable.adaptable.IdentifyInputPurposeCheck;
import org.mate.accessibility.check.wcag.perceivable.adaptable.OrientationCheck;
import org.mate.accessibility.check.wcag.perceivable.distinguishable.ContrastMinimumEnhancedCheck;
import org.mate.accessibility.check.wcag.perceivable.distinguishable.NonTextContrastCheck;
import org.mate.accessibility.check.wcag.perceivable.distinguishable.UseOfColorCheck;
import org.mate.accessibility.check.wcag.perceivable.textalternative.NonTextContentCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityViolationCheckerWCAG implements IAccessibilityViolationChecker {

    private List<IWCAGCheck> widgetAccChecks;

    private List<IWCAGCheck> screenAccChecks;

    public AccessibilityViolationCheckerWCAG(){
        widgetAccChecks = new ArrayList<IWCAGCheck>();

        //1 - PERCEIVABLE

        //1.1.1
        widgetAccChecks.add(new NonTextContentCheck());

        //1.3.5
        widgetAccChecks.add(new IdentifyInputPurposeCheck());

        //1.4.3 & 1.4.7
        widgetAccChecks.add(new ContrastMinimumEnhancedCheck());

        // 1.4.11
        //widgetAccChecks.add(new NonTextContrastCheck());


        //2.4.7
        //widgetAccChecks.add(new FocusVisibleCheck());

        //2.5.5
        widgetAccChecks.add(new TargetSizeCheck());

        //BBC
        //widgetAccChecks.add(new AcionableElementsCheck());
        //widgetAccChecks.add(new DescriptiveLinksCheck());
        //widgetAccChecks.add(new FormLayoutAccessibilityCheck());
        //widgetAccChecks.add(new PhantomElementCheck());
        //widgetAccChecks.add(new ManagingFocusCheck());
        widgetAccChecks.add(new MultipleContentDescCheck());
        widgetAccChecks.add(new SpacingAccessibilityCheck());


        screenAccChecks = new ArrayList<IWCAGCheck>();

        //1.4.1
        screenAccChecks.add(new UseOfColorCheck());

        //1.3.4
        screenAccChecks.add(new OrientationCheck());

        //2.4.2
        screenAccChecks.add(new PageTitledCheck());

    }

    public List<AccessibilityViolation> runAccessibilityChecks(IScreenState state) {

        //MATE.log("RUN WCAG CHECKS");

        List<AccessibilityViolation> violations = new ArrayList<AccessibilityViolation>();

        //Screen Based Checks
        for (IWCAGCheck screenCheck: screenAccChecks){
            AccessibilityViolation violation = screenCheck.check(state,null);
            if (violation!=null) {
                violations.add(violation);
                violation.reportFlaw();
                //MATE.log("VIOLATION FOUND: " + violation.getType().getValue());
                //if (!violation.getInfo().equals(""))
                    //MATE.log(" -- extra info: " + violation.getInfo());
            }
        }

        //Widget Based Checks
        for (Widget widget: state.getWidgets()){
            //MATE.log(">>> " + widget.getText());
            for (IWCAGCheck widgetCheck: widgetAccChecks){
                //if (widget.getText().contains("clicar aqui"))
                  //  MATE.log("CHECK: ");
                AccessibilityViolation violation = widgetCheck.check(state,widget);
                if (violation!=null) {
                    violations.add(violation);
                    violation.reportFlaw();
                    //MATE.log("VIOLATION FOUND: " + violation.getType().getValue() + " - " + widget.getClazz() + "  " + widget.getId() + " - " + widget.getText() + "  VISIBLE TO TB: " + widget.isScreenReaderFocusable() + "  ACCF: " + widget.isAccessibilityFocused() + "  IFA: " + widget.isImportantForAccessibility());
                    //if (!violation.getInfo().equals(""))
                        //MATE.log(" -- extra info: " + violation.getInfo() + " " + widget.getBounds());
                }
            }
            //MATE.log("<<< " + widget.getText());
        }
        return violations;
    }
}
