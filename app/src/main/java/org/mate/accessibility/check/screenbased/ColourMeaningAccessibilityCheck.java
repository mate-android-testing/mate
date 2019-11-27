package org.mate.accessibility.check.screenbased;

import android.os.Build;

import org.mate.MATE;
import org.mate.accessibility.AccessibilitySettings;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ColourMeaningAccessibilityCheck implements IScreenAccessibilityCheck {

    private List<Double> luminancesLow;
    private List<Double> luminancesHigh;

    private Hashtable<String,List<String>> luminancesByType;

    public ColourMeaningAccessibilityCheck(){
        luminancesLow= null;
        luminancesHigh = null;
    }

    private void detectColours(IScreenState state){

        luminancesByType = new Hashtable<String,List<String>>();

        luminancesLow = new ArrayList<Double>();
        luminancesHigh = new ArrayList<Double>();
        for (Widget widget: state.getWidgets()){
            if (!widget.getText().equals("")){
                String luminances = EnvironmentManager.getLuminances(state.getPackageName(),state.getId(),widget);
                if (!luminances.equals("0,0")) {
                    //luminances.add(luminance);
                    MATE.log("##Luminance: " + luminances);
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
        detectColours(state);
        String extraInfo = "";

        boolean exceedNumberColorsByType = false;

        for (String widgetType: luminancesByType.keySet()){
            List<String> luminances = luminancesByType.get(widgetType);
            MATE.log("LL " + widgetType);
            List<String> distinctCombinations = new ArrayList<String>();
            for (String lum: luminances){
                String parts[] = lum.split(",");

                String lowparts[] = parts[0].split(":");
                String highparts[] = parts[1].split(":");

                int rh = Integer.valueOf(highparts[0]);
                int gh = Integer.valueOf(highparts[1]);
                int bh = Integer.valueOf(highparts[2]);

                int rl = Integer.valueOf(lowparts[0]);
                int gl = Integer.valueOf(lowparts[1]);
                int bl = Integer.valueOf(lowparts[2]);

                MATE.log("   # "+lum);
                if (!distinctCombinations.contains(lum))
                    distinctCombinations.add(lum);

            }
            if (distinctCombinations.size()>= AccessibilitySettings.maxColors) {
                exceedNumberColorsByType = true;
                extraInfo+=widgetType+":"+distinctCombinations.size()+" ";
            }
        }

        if (exceedNumberColorsByType){
            return new AccessibilityViolation(AccessibilityViolationTypes.COLOUR_MEANING,state,extraInfo);
        }
        return null;


        //IDEA
        //COMPARE
        //   A- THE COLOUR OF THE TEXT ASSOCIATED WITH AN OBJECT
        //   WITH
        //   B - THE COLOUR OF THE TEXT ASSOCIATED WITH OBJECTS OF THE SAME TYPE (CHECK PARENT/CONTAINER/BUTTON)


    }
}
