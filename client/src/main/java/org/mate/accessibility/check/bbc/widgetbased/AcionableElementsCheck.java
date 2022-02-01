package org.mate.accessibility.check.bbc.widgetbased;

import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.commons.utils.MATELog;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.Widget;

public class AcionableElementsCheck implements IWidgetAccessibilityCheck {
    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (!widget.isImportantForAccessibility())
            return null;

        boolean checkClickableText=false;
        if (widget.getClazz().equals("android.widget.TextView") && widget.isClickable() && !widget.isEditable() && !widget.mightBeImage()){
            checkClickableText=true;
            //return new AccessibilityViolation(AccessibilityViolationType.ACTIONABLE_ELEMENTS,widget,state,"Clickable text, not button");
        }

        //if (check whether background color of the button is the same as the screen background)
        double matchesBackgroundColor = 0;

        matchesBackgroundColor = Registry.getEnvironmentManager().matchesSurroundingColor(state.getPackageName(),state.getId(),widget);

        if ((widget.isClickable() && widget.getClazz().contains("Button")&& !widget.getText().equals(""))|| checkClickableText) {
            MATELog.log("CHECKS BACKGROUND COLOR = " + widget.getClazz() + " " + widget.getText());
            MATELog.log("   matching: " + matchesBackgroundColor);
            if (matchesBackgroundColor>0.5) {
                AccessibilityViolation violation = new AccessibilityViolation(AccessibilityViolationType.ACTIONABLE_ELEMENTS, widget, state, "Button background color is the same as the screen, match: " + matchesBackgroundColor);
                violation.setWarning(true);
                return violation;
            }
        }

        return null;
    }
}
