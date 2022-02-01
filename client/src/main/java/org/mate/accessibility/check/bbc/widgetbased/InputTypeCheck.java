package org.mate.accessibility.check.bbc.widgetbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.Widget;

public class InputTypeCheck implements IWidgetAccessibilityCheck {

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (widget.isEditable()){
            if (widget.getInputType()==131073){
                return new AccessibilityViolation(AccessibilityViolationType.MISSING_INPUT_TYPE,widget,state,"");
            }
        }
        return null;
    }
}
