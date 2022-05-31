package org.mate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.crash_reproduction.eda.EstimationOfDistribution;
import org.mate.exploration.genetic.chromosome_factory.HeuristicalChromosomeFactory;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.fitness.CallTreeDistance;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.interaction.action.ui.UIAction;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEEDA {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting EDA strategy ...");
        MATE mate = new MATE();

        UIAction dummyRoot = new UIAction(null, "ROOT", Collections.EMPTY_LIST) {
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
        };

        EstimationOfDistribution estimationOfDistribution = new EstimationOfDistribution(
                Collections.singletonList(new CallTreeDistance<>()),
                new FitnessSelectionFunction<>(),
                new HeuristicalChromosomeFactory(Properties.MAX_NUMBER_EVENTS()),
                Properties.DISTRIBUTION_MODEL().get(dummyRoot),
                Properties.POPULATION_SIZE(),
                Properties.MAX_NUMBER_EVENTS(),
                0.3);
        mate.testApp(estimationOfDistribution);
    }
}
