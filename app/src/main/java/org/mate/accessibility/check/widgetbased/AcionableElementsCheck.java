package org.mate.accessibility.check.widgetbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class AcionableElementsCheck implements IWidgetAccessibilityCheck {
    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (widget.isTextViewType() && widget.isClickable() && !widget.mightBeImage()){
            return new AccessibilityViolation(AccessibilityViolationTypes.ACTIONABLE_ELEMENTS,widget,state,"");
        }

        //if (check whether background color of the button is the same as the screen background)


        return null;
    }
}
