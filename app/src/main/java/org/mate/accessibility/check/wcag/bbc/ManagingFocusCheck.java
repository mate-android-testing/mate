package org.mate.accessibility.check.wcag.bbc;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.IWidgetAccessibilityCheck;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.ui.ActionType;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

public class ManagingFocusCheck implements IWCAGCheck {
    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        if (widget.isFocusable()) {

            if (widget.isEditable()){

                //MATE.log("Focused before: " + widget.isFocused());
                WidgetAction wclick = new WidgetAction(widget, ActionType.CLICK);
                //MATE.log(" CLICK on : " + widget.getId() + " " + widget.getClazz() + " " + widget.getText());
                MATE.uiAbstractionLayer.executeAction(wclick);
                //MATE.log("AFTER CLICK");
                IScreenState nState = ScreenStateFactory.getScreenState("ActionsScreenState");
                //MATE.log("AFTER NEW STATE");
                //MATE.log("nstate widgets: " + nState.getWidgets().size());
                Widget wd = findWidget(nState, widget);
                if (wd != null) {
                    //MATE.log("Focused after: " + wd.isFocused());
                    if (!wd.isFocused())
                        return new AccessibilityViolation(AccessibilityViolationType.MANAGING_FOCUS,widget,state,"");

                    WidgetAction editAction = new WidgetAction(widget,ActionType.TYPE_TEXT);
                    MATE.uiAbstractionLayer.executeAction(editAction);
                    //MATE.log("AFTER EDIT TEXT");
                    nState = ScreenStateFactory.getScreenState("ActionsScreenState");
                    wd = findWidget(nState, widget);
                    if (wd!=null){
                        //MATE.log("Focused after: " + wd.isFocused());
                        if (!wd.isFocused())
                            return new AccessibilityViolation(AccessibilityViolationType.MANAGING_FOCUS,widget,state,"");


                        if (widget.getText().equals(widget.getHint())|| widget.getText().equals(widget.getContentDesc())){
                            //MATE.log("MUST CLEAR");
                            WidgetAction clearField = new WidgetAction(wd,ActionType.CLEAR_WIDGET);
                            MATE.uiAbstractionLayer.executeAction(clearField);
                        }
                        else{
                            //MATE.log("MUST INSERT SP TEXT");
                            WidgetAction textSpecific = new WidgetAction(wd,ActionType.TYPE_SPECIFIC_TEXT);
                            textSpecific.setExtraInfo(widget.getText());
                            MATE.uiAbstractionLayer.executeAction(textSpecific);
                        }
                    }
                }


            }

        }

        return null;
    }

    private Widget findWidget(IScreenState nState, Widget widget) {

        for (Widget w: nState.getWidgets()){
           // MATE.log(w.getId());
            if (w.getId().equals(widget.getId())){
                return w;
            }
        }
        return null;
    }
}
