package org.mate.accessibility.check.wcag.perceivable.distinguishable;

import android.os.Build;

import org.mate.Registry;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.AccessibilityViolationType;
import org.mate.accessibility.check.bbc.AccessibilitySettings;
import org.mate.accessibility.check.wcag.IWCAGCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

//https://www.w3.org/TR/WCAG21/#use-of-color
//https://www.w3.org/WAI/WCAG21/Understanding/use-of-color.html


public class UseOfColorCheck implements IWCAGCheck {

    private List<IScreenState> visitedStates;

    private Hashtable<String,List<String>> luminancesByType;

    public UseOfColorCheck(){
        visitedStates = new ArrayList<IScreenState>();
    }

    private void detectColours(IScreenState state){

        luminancesByType = new Hashtable<String,List<String>>();
        for (Widget widget: state.getWidgets()){
            if (widget.isActionable() && !widget.getText().contains("")){
                String luminances = Registry.getEnvironmentManager().getLuminances(state.getPackageName(),state.getId(),widget);
                if (!luminances.equals("0,0")) {
                    widget.setColor(luminances);

                    String parts[] = luminances.split(",");
                    if (parts.length>1) {

                        List<String> lumList = luminancesByType.get(widget.getClazz());
                        if (lumList == null){
                            lumList = new ArrayList<String>();
                            lumList.add(luminances);
                            luminancesByType.put(widget.getClazz(),lumList);
                        }
                        else{
                            lumList.add(luminances);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                luminancesByType.replace(widget.getClazz(),lumList);
                            }
                            else{
                                luminancesByType.put(widget.getClazz(),lumList);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {
        String extraInfo = "";

        //check if the same state exists but with different colours


        IScreenState visitedState =stateHasBeenVisited(state);
        if (visitedState!=null){
            detectColours(state);
            //MATE.log("CHECK IF COLOR CHANGED");
            //if state has been visited
            //   check whether any of their colors have changed
            //todo: implementes colour comparison outside the ActionScreenState class
            if (visitedState.differentColor(state)){
                extraInfo = "Same state - different colors for some widgets!";
                return new AccessibilityViolation(AccessibilityViolationType.USE_OF_COLOR,state,extraInfo);
                //MATE.log("COR DIFERENTE");
            }
        }
        else{
            detectColours(state);
            //MATE.log("ADD STATE TO LIST");
            visitedStates.add(state);
        }


        boolean exceedNumberColorsByType = false;

        for (String widgetType: luminancesByType.keySet()){
            List<String> luminances = luminancesByType.get(widgetType);
            //MATE.log("LL " + widgetType);
            List<String> distinctCombinations = new ArrayList<String>();
            for (String lum: luminances){
                if (!distinctCombinations.contains(lum))
                    distinctCombinations.add(lum);

            }
            if (distinctCombinations.size()>= AccessibilitySettings.maxColors) {
                exceedNumberColorsByType = true;
                extraInfo+="WARNING: " +widgetType+" use "+distinctCombinations.size()+" colour combinations";
            }
        }

        if (exceedNumberColorsByType){
            AccessibilityViolation violation = new AccessibilityViolation(AccessibilityViolationType.USE_OF_COLOR,state,extraInfo);
            violation.setWarning(true);
            return violation;
        }
        return null;


        //IDEA
        //COMPARE
        //   A- THE COLOUR OF THE TEXT ASSOCIATED WITH AN OBJECT
        //   WITH
        //   B - THE COLOUR OF THE TEXT ASSOCIATED WITH OBJECTS OF THE SAME TYPE (CHECK PARENT/CONTAINER/BUTTON)
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
