package org.mate.accessibility.check;

import org.mate.state.IScreenState;
import org.mate.ui.Widget;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by marceloeler on 26/07/17.
 */

public class MultipleContentDescCheck implements IWidgetAccessibilityCheck {

    private List<String> allDescsAndHints;

    public MultipleContentDescCheck(IScreenState state){
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

    private int count(String text){

        int total = 0;
        for (int i=0; i<allDescsAndHints.size(); i++)
            if (allDescsAndHints.get(i).equals(text))
                total++;
        return total;
    }

    @Override
    public boolean check(Widget widget) {
        if (widget.getContentDesc().equals("") && widget.getHint().equals(""))
            return true;

        if (!widget.getContentDesc().equals("")) {
            if (count(widget.getContentDesc()) > 1)
                return false;
        }

        if (!widget.getHint().equals("")){
            if(count(widget.getHint())>1)
                return false;
        }

        return true;
    }
}
