package org.mate.accessibility.check.widgetbased;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

public class AcionableElementsCheck implements IWidgetAccessibilityCheck {
    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (widget.getClazz().equals("android.widget.TextView") && widget.isClickable() && !widget.isEditable() && !widget.mightBeImage()){
            return new AccessibilityViolation(AccessibilityViolationTypes.ACTIONABLE_ELEMENTS,widget,state,"Clickable text, not button");
        }

        //if (check whether background color of the button is the same as the screen background)
        double matchesBackgroundColor = Registry.getEnvironmentManager().matchesSurroundingColor(state.getPackageName(),state.getId(),widget);

        if (widget.isClickable() && widget.isButtonType()) {
            MATE.log("CHECKS BACKGROUND COLOR = " + widget.getClazz() + " " + widget.getText());
            MATE.log("   matching: " + matchesBackgroundColor);
            if (matchesBackgroundColor>0.5) {
                AccessibilityViolation violation = new AccessibilityViolation(AccessibilityViolationTypes.ACTIONABLE_ELEMENTS, widget, state, "Button background color is the same as the screen, match: " + matchesBackgroundColor);
                violation.setWarning(true);
                return violation;
            }
        }


        return null;
    }
}
