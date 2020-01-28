package org.mate.accessibility.check.widgetbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationType;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class PhantomElementCheck implements IWidgetAccessibilityCheck {
    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (widget.isImportantForAccessibility() &&
                !widget.isVisibleToUser() &&
                widget.isClickable()){
            return new AccessibilityViolation(AccessibilityViolationType.PHANTOM_ELEMENT,widget,state,widget.getId());
        }
        return null;
    }
}
