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

    //run experiment to tune weight
    private int defineWeight(WidgetAction widgetAction){
        ActionType typeOfAction = widgetAction.getActionType();

        switch (typeOfAction){
            case CLICK:
                return 10;
            case LONG_CLICK:
                return 10;
            case TYPE_SPECIFIC_TEXT:
                return 8;
            case TYPE_TEXT:
                return 8;
            case SWIPE_DOWN:
                return 3;
            case SWIPE_UP:
                return 3;
            case SWIPE_LEFT:
                return 3;
            case SWIPE_RIGHT:
                return 3;
            case CLEAR_WIDGET:
                return 1;
            case BACK:
                return 2;
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
