package org.mate.accessibility.check.widgetbased;

import android.os.Build;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class VisibleFocusCheck implements IWidgetAccessibilityCheck {

    private List<IScreenState> visitedStates;
    private List<String> sameStatesID;

    public VisibleFocusCheck(){
        visitedStates = new ArrayList<IScreenState>();
        sameStatesID = new ArrayList<String>();
    }

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        //only checks focused widgets
        if (!widget.isFocused())
            return null;

        String extraInfo = "";

        IScreenState visitedState =stateHasBeenVisited(state);
        if (visitedState!=null){

            String newID = visitedState.getId()+"_foc";
            if (!sameStatesID.contains(newID)){
                sameStatesID.add(newID);
                state.setId(newID);
                Registry.getEnvironmentManager().screenShot(state.getPackageName(),visitedState.getId());
            }

            String luminances = Registry.getEnvironmentManager().getLuminances(state.getPackageName(),state.getId(),widget);
            if (!luminances.equals("0,0")) {
                widget.setColor(luminances);
            }

            //find equivalent widget
            Widget wOther = null;
            for (Widget w: visitedState.getWidgets()){
                if (widget.getId().equals(w.getId()) && widget.getText().equals(w.getText()))
                    wOther = w;
            }

            if (wOther==null){
                for (Widget w: visitedState.getWidgets()){
                    if (widget.getText().equals(w.getText()))
                        wOther = w;
                }
            }

            if (wOther==null)
                return null;
            else{
                if (!wOther.isFocused()){
                    //checks difference between colours
                    if (widget.getColor().equals(wOther.getColor()) && widget.getMaxminLum().equals(wOther.getMaxminLum())){
                        return new AccessibilityViolation(AccessibilityViolationTypes.VISIBLE_FOCUS,widget,state,widget.getId());
                    }
                }
            }

        }
        else{
            MATE.log("ADD STATE TO LIST");
            visitedStates.add(state);
        }

        return null;
    }


    private IScreenState stateHasBeenVisited(IScreenState currentScreenState) {
        List<IScreenState> recordedScreenStates = visitedStates;
        for (IScreenState recordedScreenState : recordedScreenStates) {
            if (recordedScreenState.equals(currentScreenState)) {
                return recordedScreenState;
            }
        }
        return null;
    }
}
