package org.mate.accessibility.check.bbc.widgetbased;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.interaction.action.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class WidgetBasedAccessibilityViolationChecker {


    public WidgetBasedAccessibilityViolationChecker(){}


    private static List<IWidgetAccessibilityCheck> widgetBasedChecks = null;

    public static List<IWidgetAccessibilityCheck> createWidgetBasedAccessibilityList(){
        if (widgetBasedChecks==null){
            widgetBasedChecks = new ArrayList<IWidgetAccessibilityCheck>();
            widgetBasedChecks.add(new FormControlLabelCheck());
            widgetBasedChecks.add(new TextContrastRatioAccessibilityCheck());
            widgetBasedChecks.add(new InputTypeCheck());
            widgetBasedChecks.add(new FormLayoutAccessibilityCheck());
            widgetBasedChecks.add(new MultipleContentDescCheck());
            widgetBasedChecks.add(new SpacingAccessibilityCheck());
            widgetBasedChecks.add(new DescriptiveLinksCheck());
            widgetBasedChecks.add(new VisibleFocusCheck());
            widgetBasedChecks.add(new TargetSizeAccessibilityCheck());
            widgetBasedChecks.add(new ErrorMessageCheck());
            widgetBasedChecks.add(new AcionableElementsCheck());
            widgetBasedChecks.add(new PhantomElementCheck());
            //widgetBasedChecks.add(new ManagingFocusCheck());
        }

        return widgetBasedChecks;
    }


    public static List<AccessibilityViolation> runAccessibilityChecks(IScreenState state) {

        List<AccessibilityViolation> violations = new ArrayList<AccessibilityViolation>();

        widgetBasedChecks = createWidgetBasedAccessibilityList();
        MATE.log(">>WIDGET BASED CHECKS");
        for (Widget widget: state.getWidgets()){

            for (IWidgetAccessibilityCheck widgetCheck: widgetBasedChecks){
                AccessibilityViolation violation = widgetCheck.check(state,widget);
                if (violation!=null) {
                    violations.add(violation);
                    violation.reportFlaw();
                    MATE.log("VIOLATION FOUND: " + violation.getType().getValue() + " - " + widget.getClazz() + "  " + widget.getId() + " - " + widget.getText() + "  VISIBLE TO TB: " + widget.isScreenReaderFocusable() + "  ACCF: " + widget.isAccessibilityFocused() + "  IFA: " + widget.isImportantForAccessibility());
                    if (!violation.getInfo().equals(""))
                        MATE.log(" -- extra info: " + violation.getInfo() + " " + widget.getBounds().toShortString());
                }
            }
        }
        MATE.log("<<WIDGET BASED CHECKS");
        return violations;
    }
}
