package org.mate.accessibility.check.bbc.widgetbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.Widget;

public class HiddenDecorativeContentCheck implements IWidgetAccessibilityCheck {
    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (widget.isTextViewType())
            return null;

        if (!widget.isActionable() && widget.isImportantForAccessibility()){
            return new AccessibilityViolation(AccessibilityViolationType.HIDDEN_DECORATIVE_CONTENT,widget,state,"");
        }

        if (!widget.isVisible() && widget.isImportantForAccessibility()){
            return new AccessibilityViolation(AccessibilityViolationType.HIDDEN_DECORATIVE_CONTENT,widget,state,"");
        }

        return null;
    }
}
