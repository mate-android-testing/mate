package org.mate.accessibility.check.widgetbased;

import org.mate.MATE;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class FormControlLabelCheck implements IWidgetAccessibilityCheck {

    private List<Widget> widgets;
    private List<String> labeledBy;

    public FormControlLabelCheck(){

    }

    private boolean applicable(Widget widget){

        try {
            Class<?> clazz = Class.forName(widget.getClazz());
            MATE.log(clazz + "    son of: " + android.widget.TextView.class.isAssignableFrom(clazz));
            if (android.widget.TextView.class.isAssignableFrom(clazz)){
                if (widget.getText().equals(""))
                    return true;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        return false;
    }

    @Override
    public boolean check(IScreenState state, Widget widget) {

        labeledBy = new ArrayList<String>();
        for (Widget w: state.getWidgets()){
            labeledBy.add(w.getLabelFor());
        }

        if (!applicable(widget)){
            return true;
        }

        if (!widget.getHint().equals("")){
            return true;
        }

        //covered by EditableContentDesc
        // %if (!widget.getContentDesc().equals("")){
           // return false;
        //}

        if (!widget.getLabeledBy().equals("")) {
            return true;
        }

        if (labeledBy.contains(widget.getResourceID())) {
            return true;
        }

        return false;
    }

    @Override
    public String getType() {
        return "FORM CONTROL LABEL";
    }

    @Override
    public String getInfo() {
        return "";
    }
}
