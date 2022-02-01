package org.mate.accessibility.check.bbc.screenbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.accessibility.check.IScreenAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.Widget;

public class RadioGroupCheck implements IScreenAccessibilityCheck {
    @Override
    public AccessibilityViolation check(IScreenState state) {

        int radioButtonCount = 0;
        int radioGroupCount = 0;
        for (Widget widget: state.getWidgets()){
            if (widget.getClazz().contains("RadioButton")){
                radioButtonCount++;
            }

            if (widget.getClazz().contains("RadioGroup")){
                radioGroupCount++;
            }
        }

        if (radioButtonCount>0 && radioGroupCount==0)
            return new AccessibilityViolation(AccessibilityViolationType.RADIOGROUPCHECK,state,"");

        return null;
    }
}
