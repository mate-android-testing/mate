package org.mate.exploration.deprecated.Fitness;

import org.mate.exploration.deprecated.aco.Ant;
import org.mate.model.deprecated.graph.EventEdge;
import org.mate.state.IScreenState;

/**
 * Created by geyan on 03/07/2017.
 */

@Deprecated
public class ActivityCoverage {

    public void updateAntFitness(IScreenState state, Ant ant) {
        if (!ant.getCoveredActivity().contains(state.getActivityName())){
            ant.getCoveredActivity().add(state.getActivityName());
            EventEdge eventEdge = ant.getCurrentEventEdge();
            eventEdge.setFitness(eventEdge.getFitness()+1);
            ant.getBenefitForFitnessEventEdge().add(eventEdge);
        }
    }
}
