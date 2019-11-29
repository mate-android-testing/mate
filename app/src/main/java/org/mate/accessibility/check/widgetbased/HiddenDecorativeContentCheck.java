package org.mate.accessibility.check.widgetbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

public class HiddenDecorativeContentCheck implements IWidgetAccessibilityCheck {
    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (widget.isTextViewType())
            return null;

        if (!widget.isActionable() && widget.isImportantForAccessibility()){
            return new AccessibilityViolation(AccessibilityViolationTypes.HIDDEN_DECORATIVE_CONTENT,widget,state,"");
        }

        if (!widget.isVisibleToUser() && widget.isImportantForAccessibility()){
            return new AccessibilityViolation(AccessibilityViolationTypes.HIDDEN_DECORATIVE_CONTENT,widget,state,"");
        }

        return null;
    }
}
