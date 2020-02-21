package org.mate.accessibility.check.bbc.screenbased;

import android.os.Build;

import org.mate.Registry;
import org.mate.accessibility.check.bbc.AccessibilitySettings;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.check.bbc.AccessibilityViolationType;
import org.mate.accessibility.check.IScreenAccessibilityCheck;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ColourMeaningAccessibilityCheck implements IScreenAccessibilityCheck {

    private List<IScreenState> visitedStates;

    private Hashtable<String,List<String>> luminancesByType;

    public ColourMeaningAccessibilityCheck(){
        visitedStates = new ArrayList<IScreenState>();
    }

    private void detectColours(IScreenState state){

        luminancesByType = new Hashtable<String,List<String>>();
        for (Widget widget: state.getWidgets()){
            if (widget.isActionable()){
                String luminances = Registry.getEnvironmentManager().getLuminances(state.getPackageName(),state.getId(),widget);
                if (!luminances.equals("0,0")) {
                    widget.setColor(luminances);
                    //luminances.add(luminance);
                    //MATE.log("##Luminance: " + luminances);
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

                        //double low = Double.valueOf(parts[0]);
                        //double high = Double.valueOf(parts[1]);
                        //luminancesLow.add(low);
                        //luminancesHigh.add(high);
                    }

                }
            }
        }


    }

    @Override
    public AccessibilityViolation check(IScreenState state) {
        String extraInfo = "";

        IScreenState visitedState =stateHasBeenVisited(state);
        if (visitedState!=null){
            Registry.getEnvironmentManager().screenShot(state.getPackageName(),visitedState.getId()+"_");
            state.setId(visitedState.getId()+"_");
            detectColours(state);
            //MATE.log("CHECK IF COLOR CHANGED");
            //if state has been visited
            //   check whether any of their colors have changed
            if (visitedState.differentColor(state)){
                extraInfo = "Same state, different colors for some widgets!";
                return new AccessibilityViolation(AccessibilityViolationType.COLOUR_MEANING,state,extraInfo);
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

                /*
                String parts[] = lum.split(",");
                String lowparts[] = parts[0].split(":");
                String highparts[] = parts[1].split(":");

                int rh = Integer.valueOf(highparts[0]);
                int gh = Integer.valueOf(highparts[1]);
                int bh = Integer.valueOf(highparts[2]);

                int rl = Integer.valueOf(lowparts[0]);
                int gl = Integer.valueOf(lowparts[1]);
                int bl = Integer.valueOf(lowparts[2]);
*/
                //MATE.log("   # "+lum);
                if (!distinctCombinations.contains(lum))
                    distinctCombinations.add(lum);

            }
            if (distinctCombinations.size()>= AccessibilitySettings.maxColors) {
                exceedNumberColorsByType = true;
                extraInfo+=widgetType+":"+distinctCombinations.size()+" ";
            }
        }

        if (exceedNumberColorsByType){
            AccessibilityViolation violation = new AccessibilityViolation(AccessibilityViolationType.COLOUR_MEANING,state,extraInfo);
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
