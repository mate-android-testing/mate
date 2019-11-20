package org.mate.accessibility.check.widgetbased;

import org.mate.MATE;
import org.mate.accessibility.AccessibilitySettings;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;


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

public class FormLayoutAccessibilityCheck implements IWidgetAccessibilityCheck {

    private List<String> labeledBy;
    private int distance;


    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {


        distance = 0;
        Widget label = null;
        String labelID = "";

        if (!widget.isEditable()){
            return null;
        }

        labeledBy = new ArrayList<String>();
        for (Widget w: state.getWidgets()){
            labeledBy.add(w.getLabelFor());
        }

        boolean hasLabel = false;
        boolean hasHint = false;

        if (!widget.getLabeledBy().equals("")) {
            hasLabel=true;
            labelID = widget.getLabeledBy();
            for (Widget w: state.getWidgets()){
                if (w.getId().equals(labelID))
                    label = w;
            }
        }

        if (labeledBy.contains(widget.getResourceID())) {
            hasLabel = true;

            for (Widget w: state.getWidgets()){
                if (w.getLabelFor().equals(widget.getId())){
                    label = w;
                }
            }
        }

        if (!hasLabel) {
            MATE.log(" NO LABEL");
            return new AccessibilityViolation(AccessibilityViolationTypes.LABEL_NOT_DEFINED, widget, state, "");
        }
        else{
            //check proximity
            MATE.log("  CHECK LABEL PROXIMITY: " );
            MATE.log(" Label: " + label.getBounds() + "    : edit text: " + widget.getBounds());


            //boolean isAbove = false;

            boolean isAtLeft = checkLeft(label,widget);
            if (!isAtLeft)
                return new AccessibilityViolation(AccessibilityViolationTypes.LABEL_FAR_FROM_INPUTTEXT,widget,state,String.valueOf(distance));
        }

        if (!widget.getHint().equals("")){
            hasHint = true;
        }




        return null;
    }

    private boolean checkLeft(Widget label, Widget editbox) {

        int x1l=label.getX1();
        int x2l=label.getX2();
        int y1l=label.getY1();
        int y2l=label.getY2();

        int x1e=editbox.getX1();
        int x2e=editbox.getX2();
        int y1e=editbox.getY1();
        int y2e=editbox.getY2();

        if (x2l<=x1e){
            //check if they are in the same line

            if ((y1l >= y1e && y1l<=y2e) || (y2l >= y1e && y2l<=y2e) ||
                (y1e >= y1l && y1e<=y2l) || (y2e >= y1l && y2e<=y2l)) {

                //check distance

                distance = x1e - x2l;
                if (distance <= AccessibilitySettings.maxLabelDistance) {

                    return true;
                }
            }
        }
        return false;
    }
}
