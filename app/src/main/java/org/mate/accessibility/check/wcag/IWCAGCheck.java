package org.mate.accessibility.check.wcag;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public interface IWCAGCheck {
    public AccessibilityViolation check(IScreenState state, Widget widget);
    //public String violationChecked();
}
