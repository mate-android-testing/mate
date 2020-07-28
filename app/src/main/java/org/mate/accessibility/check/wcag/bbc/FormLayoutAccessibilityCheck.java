package org.mate.accessibility.check.wcag.bbc;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.accessibility.check.bbc.AccessibilitySettings;
import org.mate.accessibility.check.bbc.widgetbased.FormControlLabelCheck;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;


/*
Activate the app with zoom enabled to two times magnification.
Gain focus on each individual form field.
Verify that the control is visually labelled.
Verify the label is in close proximity to the control.
Verify that the label placement is most effective for the layout (portrait or landscape).
Verify that the label of the field is announced properly by a screen reader and matches the label’s on-screen text.
Verify that the label when taken out of context clearly and uniquely describes the purpose of the control and the action the user must take.
Verify that any field constraints of the field are indicated in the accessible name announced by a screen reader.
Results

The following checks are all true:
On-screen controls are visually labelled with meaningful names which when taken out of context describe the control’s purpose;
The label must be in close proximity to the field;
The label must be placed in an effective location for the layout of the screen:
- Above the field for portrait,
- To the left of the field of landscape;
The label of the field is rendered properly via a screen reader and matches the label’s on-screen text;
Field constraints of the field are announced properly via a screen reader.

 */

public class FormLayoutAccessibilityCheck implements IWCAGCheck {

    private List<String> labeledBy;
    private int distance;


    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (!widget.isImportantForAccessibility())
            return null;

        distance = 0;
        Widget label = null;
        String labelID = "";


        //MATE.log(widget.getClazz() + " " + widget.isCheckable() + " " + widget.isEditable() + " "+widget.isSpinnerType());
        //only checks for checkable, editable and spinner type widgets
        if (!widget.isCheckable() && !widget.isEditable() && !widget.isSpinnerType()){
            return null;
        }


        //only proceed if the target widget has a label defined
        FormControlLabelCheck formLabelCheck = new FormControlLabelCheck();
        AccessibilityViolation violation = formLabelCheck.check(state,widget);
        if (violation!=null){
            return new AccessibilityViolation(AccessibilityViolationType.LABEL_NOT_DEFINED, widget, state, "");
        }

        labeledBy = new ArrayList<String>();
        for (Widget w: state.getWidgets()){
            labeledBy.add(w.getLabelFor());
        }

        boolean hasLabel = false;
        boolean hasHint = false;

        if (!widget.getHint().equals("")){
            hasHint = true;
        }

        if (!widget.getLabeledBy().equals("")) {
            labelID = widget.getLabeledBy();
            for (Widget w: state.getWidgets()){
                if (w.getId().equals(labelID)) {
                    label = w;
                    hasLabel=true;
                }
            }
        }

        if (labeledBy.contains(widget.getResourceID())) {

            for (Widget w: state.getWidgets()){
                if (w.getLabelFor().equals(widget.getId())){
                    label = w;
                    hasLabel = true;
                }
            }
        }

        if (hasLabel && label!=null){
            //check proximity
            //boolean isAbove = false;
            boolean isAtLeft = checkLeft(label,widget);
            boolean isAtRight = checkRight(label,widget);
            boolean isAbove = checkAbove(label,widget);

            if (!isAtLeft && !isAbove) {

                if (widget.isCheckable() || widget.getClazz().contains("android.widget.RadioButton")){
                    if (isAtRight)
                        return null;
                }

                if (hasHint)
                    return new AccessibilityViolation(AccessibilityViolationType.LABEL_FAR_FROM_INPUTTEXT, widget, state, String.valueOf(distance));
            }
        }
        else{
            if (hasHint){
                //MATE.log("NEEDS TO CHECK IF IT IS A FLOATING HINT, OR HIT IS VISIBLE - "+widget.getId()+" " + widget.getText()+ " " + widget.getClazz());
                if (widget.isShowingHintText())
                    return null;
                else
                    return new AccessibilityViolation(AccessibilityViolationType.LABEL_NOT_DEFINED, widget, state, "");
            }
        }
        return null;
    }

    private boolean checkRight(Widget label, Widget widget) {

        int x1l=label.getX1();
        int x2l=label.getX2();
        int y1l=label.getY1();
        int y2l=label.getY2();

        int x1e=widget.getX1();
        int x2e=widget.getX2();
        int y1e=widget.getY1();
        int y2e=widget.getY2();


        distance = Math.abs(x1l-x2e);
        if (distance<=AccessibilitySettings.maxLabelDistance){
            distance = Math.abs(y1l-y1e);
            if (distance<=AccessibilitySettings.maxLabelDistance){
                return true;
            }
        }
        return false;
    }

    private boolean checkAbove(Widget label, Widget widget) {
        int x1l=label.getX1();
        int x2l=label.getX2();
        int y1l=label.getY1();
        int y2l=label.getY2();

        int x1e=widget.getX1();
        int x2e=widget.getX2();
        int y1e=widget.getY1();
        int y2e=widget.getY2();


        //absolute difference between the beginning of the label and the widget cannot exceed a certain amount

        distance = Math.abs(y1e-y2l);
        if (distance<=AccessibilitySettings.maxLabelDistance){
            distance = Math.abs(x1l-x1e);
            if (distance<=AccessibilitySettings.maxLabelDistance){
                return true;
            }
        }
        return false;

    }

    private boolean checkLeft(Widget label, Widget widget) {

        int x1l=label.getX1();
        int x2l=label.getX2();
        int y1l=label.getY1();
        int y2l=label.getY2();

        int x1e=widget.getX1();
        int x2e=widget.getX2();
        int y1e=widget.getY1();
        int y2e=widget.getY2();


        distance = Math.abs(x1e-x2l);
        if (distance<=AccessibilitySettings.maxLabelDistance){
            distance = Math.abs(y1l-y1e);
            if (distance<=AccessibilitySettings.maxLabelDistance){
                return true;
            }
        }
        return false;
    }
}
