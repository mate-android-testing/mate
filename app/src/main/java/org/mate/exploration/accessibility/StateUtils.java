package org.mate.exploration.accessibility;

import android.content.Context;

import org.mate.Registry;
import org.mate.accessibility.check.wcag.perceivable.distinguishable.ContrastMinimumEnhancedCheck;
import org.mate.accessibility.check.wcag.perceivable.distinguishable.NonTextContrastCheck;
import org.mate.state.IScreenState;
import org.mate.state.executables.ActionsScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class StateUtils {

    //-> types of state difference
    //[package name=1, activity=2, hierarchy=3, widget state=4, acc properties=5]

    private static List<String> screenShots=new ArrayList<String>();


    public static void showDifference(IScreenState stateA, IScreenState stateB, String text){
        //MATE.log("Comparing " + stateA.getId() + " vs " + stateB.getId()+ ": "+ text);
    }

    public static void takeScreenShot(IScreenState state){
        if (!screenShots.contains(state.getId())) {
            String stateId = state.getId();
            if (!state.getPackageName().equals(state.getPackageName()))
                stateId+="_OUTBOUND-"+state.getPackageName();
            Registry.getEnvironmentManager().screenShot(state.getPackageName(), stateId);
        }
    }


    public static void takeScreenShotDetectProperties(IScreenState state){
        if (!screenShots.contains(state.getId())) {
            String stateId = state.getId();
            if (!state.getPackageName().equals(state.getPackageName()))
                stateId+="_OUTBOUND-"+state.getPackageName();
            Registry.getEnvironmentManager().screenShot(state.getPackageName(), stateId);
            if (!state.getId().contains("tempstate"))
                screenShots.add(state.getId());
            //DetectColours(state);
            /*
            MATE.log("colours of widgets: " + state.getId());
            for (Widget widget: state.getWidgets()){
                if (!widget.getText().equals("")){
                    MATE.log(widget.getId()  + " - " + widget.getText() + " : "+widget.getColor());
                }
            }*/
            //DetectSize(state);
            //DetectConstrast(state);
        }
    }

    private static void DetectSize(IScreenState state) {
        for (Widget widget: state.getWidgets()){
            //if (widget.isActionable() && widget.hasChildren()==false) {
                Context generalContext = getInstrumentation().getContext();
                float density = generalContext.getResources().getDisplayMetrics().density;
                float height = Math.abs(widget.getY2() - widget.getY1());
                float width = Math.abs(widget.getX2() - widget.getX1());

                float relativeHeight = height / density;
                float relativeWidth = width / density;
                String sizeInfo = relativeHeight + "x" + relativeWidth;
                widget.setSize(sizeInfo);
            //}
        }
    }

    private static void DetectConstrast(IScreenState state){
        for (Widget widget: state.getWidgets()){
            if (ContrastMinimumEnhancedCheck.needsTextContrastChecked(widget) || NonTextContrastCheck.needsTextContrastChecked(widget)) {
                double contrastRatio = Registry.getEnvironmentManager().getContrastRatio(state.getPackageName(), state.getId(), widget);
                widget.setContrast(contrastRatio);
            }
        }
    }

    private static void DetectColours(IScreenState state) {
        for (Widget widget: state.getWidgets()){

            String luminances = Registry.getEnvironmentManager().getLuminances(state.getPackageName(),state.getId(),widget);
            //MATE.log(widget.getId()+ ": " + luminances);
            if (!luminances.equals("0,0")) {
                widget.setColor(luminances);
            }
            else
                widget.setColor("");

            //MATE.log("Widget colour " + widget.getColor());
            //MATE.log("Widget lum " + widget.getMaxminLum());

        }
    }


    public static String checkStateDifferenceType(IScreenState stateA, IScreenState stateB){

        //stateB is the new state
        stateB.setId("tempstate");
        //stateB.setId("");


        //MATE.log("colours of widgets: " + stateA.getId());
        for (Widget widget: stateA.getWidgets()){
            if (!widget.getText().equals("")){
                //MATE.log(widget.getId()  + " - " + widget.getText() + " : "+widget.getColor());
            }
        }


        if (!stateA.getPackageName().equals(stateB.getPackageName())){
            return "Package";
        }

        if (!stateA.getActivityName().equals(stateB.getActivityName())){
            showDifference(stateA,stateB,"Activity name:  " + stateA.getActivityName() + "  vs  " + stateB.getActivityName());
            return "Activity";
        }

        String differences = "";

        List<String> setActThis = new ArrayList<>();
        List<String> setActOther = new ArrayList<>();

        for (Widget widget: stateA.getWidgets()){
            if (widget.getClazz().contains("Button"))
                setActThis.add(widget.getId() + "-" + widget.getText());
            else
                setActThis.add(widget.getId());
        }

        for (Widget widget: stateB.getWidgets()){
            if (widget.getClazz().contains("Button"))
                setActOther.add(widget.getId() + "-" + widget.getText());
            else
                setActOther.add(widget.getId());
        }

        if (setActThis.size()==setActOther.size()) {

            for (String strActThis : setActThis) {
                if (!setActOther.contains(strActThis)) {
                    //MATE.log_acc("Strobe 3: State "+this.id+"different from State: "+object.getId());
                    showDifference(stateA,stateB,stateB.getId() +" does not contain " + strActThis);
                    return "Hierarchy";
                }
            }
        }
        else {
            showDifference(stateA,stateB,"Widgets.size() is different: " + setActThis.size() + " vs " + setActOther.size());

            /*
            MATE.log(stateA.getId() + " widgets ");
            for (String st: setActThis)
                MATE.log (st);

            MATE.log(stateB.getId() + " widgets ");
            for (String st: setActOther)
                MATE.log (st);
*/
            return "Hierarchy";
        }



        ActionsScreenState sA = (ActionsScreenState) stateA;
        ActionsScreenState sB = (ActionsScreenState) stateB;
        Hashtable<String, Widget> editablesThis = sA.getEditableWidgets();
        Hashtable<String, Widget> editablesOther = sB.getEditableWidgets();

        for (Widget wdgThis: editablesThis.values()){

            Widget wdgOther = editablesOther.get(wdgThis.getId());
            if (wdgOther!=null) {
                //MATE.log_acc("Strobe 4 State "+this.id+"different from State: "+object.getId());
                //return "HierarchyED";


                if (wdgOther.isEmpty() != wdgThis.isEmpty()) {
                    //MATE.log_acc("Strobe 5: State "+this.id+"different from State: "+object.getId());
                    differences += "St";
                }

                if (!wdgOther.getErrorText().equals(wdgThis.getErrorText())) {
                    differences += "Er";
                }
            }
        }


        //as for the checkables it considers two GUIs equals if they have the same objects checked
        Hashtable<String,Widget> checkablesThis = sA.getCheckableWidgets();
        Hashtable<String,Widget> checkablesOther = sB.getCheckableWidgets();
        for (Widget wdgThis: checkablesThis.values()){
            Widget wdgOther = checkablesOther.get(wdgThis.getId());
            if (wdgOther!=null) {
                //return "HierarchyCK";

                if (wdgOther.isChecked() != wdgThis.isChecked())
                    differences += "St";
            }
        }

        return differences;
        /*
        //FOR STATE EXPERIMENT ONLY
        takeScreenShotDetectProperties(stateB);
        //CHECK acc properties
        for (Widget wA: stateA.getWidgets()){
            if (!wA.getText().equals("")){
                //MATE.log("within test: " );
                //MATE.log(wA.getId()  + " - " + wA.getText() + " : "+wA.getColor());

            }

            Widget wB = getWidget(stateB.getWidgets(),wA);

            if (wB!=null) {
                // return "Hierarchy";

                if (wB.getText().equals(wA.getText())) {

                    if (!wB.getColor().equals(wA.getColor())) {
                        //differences += "Cl";
                    }

                    if (!wB.getMaxminLum().equals(wA.getMaxminLum())) {
                        //showDifference(stateA,stateB," different luminance in "+wA.getId()+ ": " + wA.getMaxminLum() + " vs " + wB.getMaxminLum());
                        //differences += "Lu";
                    }

                    if (wA.getContrast() != wB.getContrast()){
                        //showDifference(stateA,stateB," different contrast in "+wA.getId()+ ": " + wA.getContrast() + " vs " + wB.getContrast());
                        differences += "Ctr";
                    }

                    if (!wB.getSize().equals(wA.getSize())) {
                        differences += "Sz"; //dimension
                        showDifference(stateA,stateB," different size in "+wA.getId()+ " txt: " + wA.getText() +" / " + wB.getText()+": " + wA.getSize() + " vs " + wB.getSize());
                    }

                }


                if (!wB.getHint().equals(wA.getHint()))
                    differences += "Ht";

                if (!wB.getContentDesc().equals(wA.getContentDesc()))
                    differences += "Cd";

                if (wB.isScreenReaderFocusable() != wA.isScreenReaderFocusable())
                    differences += "Srf";

                if (wB.isImportantForAccessibility() != wA.isImportantForAccessibility())
                    differences += "Ifa";
            }
        }

        if (differences.equals(""))
            showDifference(stateA,stateB," same state");
        else
            showDifference(stateA,stateB,differences);

        return differences;
        */

    }



    private static Widget getWidget(List<Widget> widgets, Widget widget){
        for (Widget w: widgets){
            if (w.getId().equals(widget.getId()))
                return w;
        }
        return null;
    }
}
