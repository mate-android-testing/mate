package org.mate.accessibility.check.bbc.widgetbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.Widget;

public class DescriptiveLinksCheck implements IWidgetAccessibilityCheck {



    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (widget.isActionable() && !widget.getText().equals("") && widget.isImportantForAccessibility()){
            boolean foundViolation = false;
            String conflictantIds = "";
            for (Widget other: state.getWidgets()){
                if (!widget.getId().equals(other.getId())){
                    if (widget.getText().equals(other.getText())){
                        String widgetCD = widget.getContentDesc();
                        String widgetHint = widget.getHint();
                        String otherCD = other.getContentDesc();
                        String otherHint = other.getHint();

                        if (widgetCD.equals("")&&widgetHint.equals("")&&otherCD.equals("")&&otherHint.equals("")){
                            foundViolation = true;
                            conflictantIds+=other.getId()+" ";
                        }
                        else{
                            if (widgetCD.equals(otherCD) && widgetHint.equals(otherHint) && widgetCD.equals(otherHint) && widgetHint.equals(otherCD)){
                                foundViolation = true;
                                conflictantIds+=other.getId()+" ";
                            }
                        }
                    }
                }
            }
            if (foundViolation)
                return new AccessibilityViolation(AccessibilityViolationType.DESCRIPTIVE_LINKS,widget,state,conflictantIds);

        }

        return null;
    }
}
