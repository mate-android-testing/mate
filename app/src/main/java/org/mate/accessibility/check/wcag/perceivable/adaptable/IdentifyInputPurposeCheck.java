package org.mate.accessibility.check.wcag.perceivable.adaptable;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class IdentifyInputPurposeCheck implements IWCAGCheck {


    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {
        if (widget.isEditable()){
            if (widget.getInputType()==131073){
                return new AccessibilityViolation(AccessibilityViolationType.IDENTIFY_INPUT_PURPOSE,widget,state,"");
            }
        }
        return null;
    }


}
