package org.mate.exploration.accessibility;

import org.mate.MATE;
import org.mate.exploration.genetic.selection.RandomSelectionFunction;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.state.IScreenState;
import org.mate.ui.ActionType;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


//execute each action only once

public class BiasedRandomFixedWeight extends AbstractRandomExploration{

    public BiasedRandomFixedWeight(UIAbstractionLayer uiAbstractionLayer){
        super(uiAbstractionLayer);
    }

    /*
    Action	States	%States
CLICK	2690	65,21%
TYPE_TEXT	448	10,86%
SWIPE_DOWN	405	9,82%
MENU	129	3,13%
BACK	125	3,03%
SWIPE_UP	117	2,84%
ENTER	68	1,65%
LONG_CLICK	49	1,19%
SWIPE_RIGHT	48	1,16%
SWIPE_LEFT	46	1,12%

     */

    //run experiment to tune weight
    private int defineWeight(WidgetAction widgetAction){
        ActionType typeOfAction = widgetAction.getActionType();

        //order
        //1,1,2,3,5,8

        switch (typeOfAction){
            case CLICK:
                return 65;
            case LONG_CLICK:
                return 1;
            case TYPE_SPECIFIC_TEXT:
                return 10;
            case TYPE_TEXT:
                return 10;
            case SWIPE_DOWN:
                return 10;
            case SWIPE_UP:
                return 10;
            case SWIPE_LEFT:
                return 1;
            case SWIPE_RIGHT:
                return 1;
            case CLEAR_WIDGET:
                return 1;
            case MENU:
                return 3;
            case BACK:
                return 3;
            default:
                return 1;
        }
    }


    private void setWeights(List<WidgetAction> executableActions) {
        for (WidgetAction widgetAction: executableActions){
            widgetAction.setPheromone(defineWeight(widgetAction));
        }
    }

    //https://aonecode.com/getArticle/210
    @Override
    public WidgetAction nextAction(IScreenState state) {
        List<WidgetAction> executableActions = state.getActions();
        setWeights(executableActions);

       /* float sum = 0;
        for (WidgetAction wa: executableActions)
            sum+= wa.getPheromone();

        float probability[] = new float[executableActions.size()];
        for (int i=0; i<probability.length; i++){
            probability[i] = executableActions.get(i).getPheromone()/sum;
        }

        Random rand = new Random();
        int randomNumber = rand.nextInt((int)sum-1);
*/
       //MATE.log("POSSIBLE ACTIONS + WEIGHT");
        RandomCollection<WidgetAction> rc = new RandomCollection<WidgetAction>();
        for (WidgetAction wa: executableActions){
            //MATE.log(wa.getActionType() + ": " + wa.getPheromone());
            rc.add(wa.getPheromone(),wa);
        }


        //MATE.log("TEST SELECTING ACTIONS");
        WidgetAction choice = rc.next();

        //int index =  rand.nextInt(executableActions.size());
        //WidgetAction action = executableActions.get(index);
        return choice;

    }


}
