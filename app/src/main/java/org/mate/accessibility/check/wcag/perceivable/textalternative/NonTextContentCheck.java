package org.mate.accessibility.check.wcag.perceivable.textalternative;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;

//Success Criterion 1.1.1 Non-text Content
//https://www.w3.org/TR/WCAG21/#non-text-content
public class NonTextContentCheck implements IWCAGCheck {

    private List<String> labeledBy;

    private boolean applicable(Widget widget){


        boolean buttonType = widget.isButtonType();
        boolean imageButtonType = widget.isImageButtonType();
        boolean imageSwitcherType = widget.isImageSwitcherType();
        boolean imageType = widget.isImageType();
        boolean spinnerType = widget.isSpinnerType();
        boolean editType = 	widget.isEditable();
        boolean textViewType = widget.isTextViewType();


        if (buttonType || imageButtonType || imageSwitcherType || imageType || spinnerType || editType || textViewType){
            //if (widget.isImportantForAccessibility())
                return true;
        }




        //if (widget.isClickable() || widget.isEditable() || widget.isCheckable())
        //   return true;

        return false;
    }

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {



        labeledBy = new ArrayList<String>();
        for (Widget w: state.getWidgets()){
            if (!w.getLabelFor().equals("")) {
               labeledBy.add(w.getLabelFor());
            }
        }

        if (!applicable(widget)){
            return null;
        }



        if ((widget.isButtonType() || widget.isTextViewType())&&!widget.isEditable()){
            if (!widget.getText().equals("")){
                return null;
            }
        }
        

        if (!widget.getHint().equals("")){
            return null;
        }

        //covered by EditableContentDesc
        if (!widget.getContentDesc().equals("")){
            if (!widget.isEditable())
                return null;
            else
                return new AccessibilityViolation(AccessibilityViolationType.EDITABLE_CONTENT_DESC,widget,state,"");
        }



        if (!widget.getLabeledBy().equals("")) {

            return null;
        }

        if (labeledBy.contains(widget.getResourceID())) {
            //MATE.log(" ACC CHECK LABEL: has label by: id)");
            int index = labeledBy.indexOf(widget.getResourceID());
            return null;
        }

        if (widget.isImageType() || widget.isImageSwitcherType() || widget.mightBeImage()){
            return new AccessibilityViolation(AccessibilityViolationType.NON_TEXT_CONTENT,widget,state,"");
        }

        return new AccessibilityViolation(AccessibilityViolationType.NON_TEXT_CONTENT,widget,state,"");
    }
}
