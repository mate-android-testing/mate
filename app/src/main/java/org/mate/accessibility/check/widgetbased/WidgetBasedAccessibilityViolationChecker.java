package org.mate.accessibility.check.widgetbased;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

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
            //widgetBasedChecks.add(new ManagingFocusCheck());
        }

        return widgetBasedChecks;
    }


    public static void runAccessibilityChecks(IScreenState state) {

        widgetBasedChecks = createWidgetBasedAccessibilityList();
        MATE.log(">>WIDGET BASED CHECKS");
        for (Widget widget: state.getWidgets()){

            for (IWidgetAccessibilityCheck widgetCheck: widgetBasedChecks){
                AccessibilityViolation violation = widgetCheck.check(state,widget);
                if (violation!=null) {
                    MATE.log("VIOLATION FOUND: " + AccessibilityViolationTypes.NAMES[violation.getType()] + " - " + widget.getClazz() + "  " + widget.getId() + " - " + widget.getText() + "  VISIBLE TO TB: " + widget.isScreenReaderFocusable() + "  ACCF: " + widget.isAccessibilityFocused() + "  IFA: " + widget.isImportantForAccessibility());
                    if (!violation.getInfo().equals(""))
                        MATE.log(" -- extra info: " + violation.getInfo());
                }
            }
        }
        MATE.log("<<WIDGET BASED CHECKS");

    }
}
