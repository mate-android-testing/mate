package org.mate.accessibility.check.wcag.operable.navigable;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class PageTitledCheck implements IWCAGCheck {
    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (state.getScreenTitle().equals("")){
            return new AccessibilityViolation(AccessibilityViolationType.PAGE_TITLED,state,"");
        }
        return null;
    }
}
