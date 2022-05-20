package org.mate.accessibility.check.bbc.screenbased;

import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.accessibility.check.IScreenAccessibilityCheck;
import org.mate.commons.utils.MATELog;
import org.mate.state.IScreenState;
import org.mate.commons.interaction.action.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class ErrorMessageScreenCheck implements IScreenAccessibilityCheck {

    private List<IScreenState> visitedStates;

    public ErrorMessageScreenCheck(){
        visitedStates = new ArrayList<IScreenState>();
    }


    @Override
    public AccessibilityViolation check(IScreenState state) {

        IScreenState visitedState = stateHasBeenVisited(state);

        if (visitedState!=null){
            MATELog.log("same state");
            if (differentContext(state,visitedState)){
                AccessibilityViolation violation = new AccessibilityViolation(AccessibilityViolationType.ERROR_MESSAGE,state,"Content descriptions/hints do not changed after showing error message");
                violation.setWarning(true);
                return violation;
            }
        }
        else{
            MATELog.log("different state");
            visitedStates.add(state);
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
                if (wThis.getId().contains("imageview_search)")){
                    MATELog.log("this: "+wThis.getText()+ " " + wThis.getContentDesc() + " " + wThis.getHint()+" " + wThis.getErrorText());
                    MATELog.log("other: "+wOther.getText()+ " " + wOther.getContentDesc() + " " + wOther.getHint()+" " + wOther.getErrorText());
                }
                if (wThis.getId().equals(wOther.getId())&&
                        wOther.getContentDesc().equals(wThis.getContentDesc()) &&
                        wOther.getHint().equals(wThis.getHint())) {
                    return true;
                }
            }
        }
        return false;
    }

    private IScreenState stateHasBeenVisited(IScreenState currentScreenState) {

        int index = visitedStates.indexOf(currentScreenState);
        if (index>=0){
            //stored state needs to be retrived to be compared to a different state
            return visitedStates.get(index);
        }
        return null;
    }
}
