package org.mate.accessibility.check.wcag.bbc;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class PhantomElementCheck implements IWCAGCheck {
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
