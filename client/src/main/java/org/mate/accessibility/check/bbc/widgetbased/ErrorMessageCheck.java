package org.mate.accessibility.check.bbc.widgetbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.Widget;

import java.util.List;


public class ErrorMessageCheck implements IWidgetAccessibilityCheck {


    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (!widget.getErrorText().equals("") &&
                widget.getContentDesc().equals("") &&
                widget.getHint().equals("")){
            return new AccessibilityViolation(AccessibilityViolationType.ERROR_MESSAGE,widget,state,"Error messages must have content description/hint set to guide users");
        }
        return null;
    }

    private boolean differentContext(IScreenState state, IScreenState visitedState) {

        List<Widget> thisWidgets = state.getWidgets();
        List<Widget> otherWidgets = visitedState.getWidgets();

        boolean found = false;
        for (Widget wThis: thisWidgets){
            //search by id
            for (Widget wOther: otherWidgets){
                if (wThis.getId().equals(wOther.getId()) &&
                        !wThis.getErrorText().equals(wOther.getErrorText()) &&
                        wOther.getContentDesc().equals(wThis.getContentDesc()) &&
                        wOther.getHint().equals(wThis.getHint())) {
                    return true;
                }
            }
        }
        return false;
    }
}
