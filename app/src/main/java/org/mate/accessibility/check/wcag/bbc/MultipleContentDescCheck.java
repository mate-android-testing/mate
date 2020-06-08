package org.mate.accessibility.check.wcag.bbc;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marceloeler on 26/07/17.
 */

public class MultipleContentDescCheck implements IWCAGCheck {

    private List<String> allDescsAndHints;

    public MultipleContentDescCheck(){
        allDescsAndHints = null;
    }

    private int count(String text){

        int total = 0;
        for (int i=0; i<allDescsAndHints.size(); i++)
            if (allDescsAndHints.get(i).equals(text))
                total++;
        return total;
    }

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (allDescsAndHints==null)
            detectLabels(state);

        if (widget.getContentDesc().equals("") && widget.getHint().equals(""))
            return null;

        if (!widget.getContentDesc().equals("")) {
            if (count(widget.getContentDesc()) > 1)
                return new AccessibilityViolation(AccessibilityViolationType.DUPLICATE_CONTENT_DESCRIPTION,widget,state,widget.getContentDesc());
        }

        if (!widget.getHint().equals("")){
            if(count(widget.getHint())>1)
                return new AccessibilityViolation(AccessibilityViolationType.DUPLICATE_CONTENT_DESCRIPTION,widget,state,widget.getHint());
        }

        return null;
    }

    private void detectLabels(IScreenState state) {
        allDescsAndHints = new ArrayList<>();
        for (Widget widget: state.getWidgets()){

            if (!widget.getContentDesc().equals(""))
                allDescsAndHints.add(widget.getContentDesc());

            if (!widget.getHint().equals("")) {
                if (!widget.getContentDesc().equals(widget.getHint()))
                    allDescsAndHints.add(widget.getHint());
            }
        }
    }

}
