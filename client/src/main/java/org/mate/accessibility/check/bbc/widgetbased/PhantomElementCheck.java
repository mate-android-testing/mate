package org.mate.accessibility.check.bbc.widgetbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.Widget;

public class PhantomElementCheck implements IWidgetAccessibilityCheck {
    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (widget.isImportantForAccessibility() &&
                !widget.isVisible() &&
                widget.isClickable()){
            return new AccessibilityViolation(AccessibilityViolationType.PHANTOM_ELEMENT,widget,state,widget.getId());
        }
        return null;
    }
}
