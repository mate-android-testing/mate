package org.mate.accessibility;

import org.mate.MATE;
import org.mate.exploration.random.UniformRandomForAccessibility;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;



import java.util.Vector;

/**
 * Created by marceloeler on 26/07/17.
 */

public class MultipleContentDescCheck implements IWidgetAccessibilityCheck {

    private Vector<String> allDescsAndHints;

    public Vector<String> getAllDescsAndHits(){
        return allDescsAndHints;
    }

    public MultipleContentDescCheck(IScreenState state){
        allDescsAndHints = new Vector<String>();
        for (Widget widget: state.getWidgets()){
            //MATE.log("check " + widget.getId()+ " ("+widget.getContentDesc()+"/"+widget.getHint());

            String activityName = UniformRandomForAccessibility.currentActivityName;
            String widgetIdentifier = activityName + widget.getId()+"-"+widget.getText().isEmpty()+"-"+widget.getClazz()+ ":DUPCONTENT";
            //if (!MATE.checkedWidgets.contains(widgetIdentifier)) {
              //  MATE.checkedWidgets.add(widgetIdentifier);

                if (!widget.getContentDesc().equals(""))
                    allDescsAndHints.add(widget.getContentDesc());

                if (!widget.getHint().equals("")) {
                    if (!widget.getContentDesc().equals(widget.getHint()))
                        allDescsAndHints.add(widget.getHint());
                }
        }

        //MATE.log("SIZE: " + allDescsAndHints.size());
//        for (int i=0; i<allDescsAndHints.size(); i++){
//            MATE.log(i+ ": " + allDescsAndHints.get(i));
//        }
//        for (String decs: allDescsAndHints){
//            MATE.log(decs);
//        }

    }

    private int count(String text){

        int total = 0;
        for (int i=0; i<allDescsAndHints.size(); i++)
            if (allDescsAndHints.get(i).equals(text))
                total++;
        //MATE.log("  count " + text + ": " + total);
        return total;
    }

    @Override
    public boolean check(Widget widget) {
        boolean ok = false;
        if (widget.getContentDesc().equals("") && widget.getHint().equals(""))
            return true;

        if (!widget.getContentDesc().equals("")) {
            if (count(widget.getContentDesc()) > 1)
                return false;
        }

        if (!widget.getHint().equals("")){
            if(count(widget.getHint())>1)
                return false;
        }

        return true;
    }
}
