package org.mate.exploration.accessibility;

import org.mate.MATE;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.state.IScreenState;
import org.mate.ui.ActionType;
import org.mate.ui.Widget;
import org.mate.ui.WidgetAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


//execute each action only once

public class NonRepetitiveRandom extends AbstractRandomExploration{

    public NonRepetitiveRandom(UIAbstractionLayer uiAbstractionLayer){
        super(uiAbstractionLayer);
    }

    @Override
    public WidgetAction nextAction(IScreenState state) {
        List<WidgetAction> executableActions = state.getActions();
        List<WidgetAction> possibleActions  = new ArrayList<WidgetAction>();

        //MATE.log("All actions of that state: " + executableActions.size());
        for (WidgetAction action: executableActions){
            if (!action.isExecuted()){
                possibleActions.add(action);
            }
        }
        //MATE.log("All possible actions: " + possibleActions.size());

        if (possibleActions.size()>0){
            Random rand = new Random();
            int index =  rand.nextInt(possibleActions.size());
            WidgetAction action = possibleActions.get(index);
            return action;
        }
        else{
            //back
            return new WidgetAction(ActionType.BACK);
        }

    }
}
