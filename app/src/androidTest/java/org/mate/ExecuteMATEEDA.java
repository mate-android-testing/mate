package org.mate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.eda.BestActionsDistributionModel;
import org.mate.exploration.eda.EstimationOfDistribution;
import org.mate.exploration.genetic.chromosome_factory.HeuristicalChromosomeFactory;
import org.mate.exploration.genetic.fitness.TargetActivityFitnessFunction;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.interaction.action.Action;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEEDA {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting EDA strategy ...");
        MATE mate = new MATE();

        EstimationOfDistribution estimationOfDistribution = new EstimationOfDistribution(
                Collections.singletonList(new TargetActivityFitnessFunction<>()),
                new FitnessSelectionFunction<>(),
                new HeuristicalChromosomeFactory(Properties.MAX_NUMBER_EVENTS()),
                new ConditionalTerminationCondition(),
                new BestActionsDistributionModel<>(new Action() {
                    @NonNull
                    @Override
                    public String toString() {
                        return "ROOT";
                    }

                    @NonNull
                    @Override
                    public String toShortString() {
                        return "ROOT";
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }

                    @Override
                    public boolean equals(@Nullable Object o) {
                        return o == this;
                    }
                }),
                Properties.POPULATION_SIZE(),
                Properties.MAX_NUMBER_EVENTS(),
                0.1);
        mate.testApp(estimationOfDistribution);
    }
}
