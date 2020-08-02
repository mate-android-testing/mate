package org.mate.exploration.accessibility;

import org.mate.MATE;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.state.IScreenState;
import org.mate.ui.ActionType;
import org.mate.ui.WidgetAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


//execute each action only once

public class BiasedRandomNewAction extends AbstractRandomExploration{

    public BiasedRandomNewAction(UIAbstractionLayer uiAbstractionLayer){
        super(uiAbstractionLayer);
    }

    @Override
    public WidgetAction nextAction(IScreenState state) {
        List<WidgetAction> executableActions = state.getActions();
        List<WidgetAction> possibleActions  = new ArrayList<WidgetAction>();

        for (WidgetAction action: executableActions){
            if (!action.isExecuted()){
                possibleActions.add(action);
            }
        }

        if (possibleActions.size()==0){
            for (WidgetAction wa: executableActions)
                wa.setExecuted(false);
            possibleActions.addAll(executableActions);
        }


        Random rand = new Random();
        int index =  rand.nextInt(possibleActions.size());
        WidgetAction action = possibleActions.get(index);
        return action;
    }
}
