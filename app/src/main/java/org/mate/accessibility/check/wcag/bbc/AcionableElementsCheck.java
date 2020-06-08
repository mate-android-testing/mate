package org.mate.accessibility.check.wcag.bbc;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class AcionableElementsCheck implements IWCAGCheck {

    private boolean candidateToBeChecked(Widget widget){
        if (widget.getClazz().contains("RadioButton"))
            return false;
        if (widget.getClazz().contains("Spinner"))
            return false;
        if (widget.getClazz().contains("Check"))
            return false;
        return true;
    }

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {


        if (!candidateToBeChecked(widget))
            return null;

        boolean checkClickableText=false;
        if (widget.getClazz().equals("android.widget.TextView") && widget.isClickable() && !widget.isEditable() && !widget.mightBeImage()){
            checkClickableText=true;
            //return new AccessibilityViolation(AccessibilityViolationType.ACTIONABLE_ELEMENTS,widget,state,"Clickable text, not button");
        }

        //if (check whether background color of the button is the same as the screen background)
        double matchesBackgroundColor = 0;

        matchesBackgroundColor = Registry.getEnvironmentManager().matchesSurroundingColor(state.getPackageName(),state.getId(),widget);

        if ((widget.isClickable() && widget.getClazz().contains("Button") && !widget.getText().equals(""))|| checkClickableText) {
            //MATE.log("CHECKS BACKGROUND COLOR = " + widget.getClazz() + " " + widget.getText());
           // MATE.log("   matching: " + matchesBackgroundColor);
            if (matchesBackgroundColor>0.5) {
                AccessibilityViolation violation = new AccessibilityViolation(AccessibilityViolationType.ACTIONABLE_ELEMENTS, widget, state, "It does not look like a button");
                violation.setWarning(true);
                return violation;
            }
        }

        return null;
    }
}
