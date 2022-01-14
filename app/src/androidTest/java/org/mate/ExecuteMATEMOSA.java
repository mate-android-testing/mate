package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.selection.SelectionFunction;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEMOSA {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("MOSA algorithm");

        MATE mate = new MATE();

        GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.MOSA)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withCrossoverFunction(Properties.CROSSOVER_FUNCTION())
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withSelectionFunction(SelectionFunction.CROWDED_TOURNAMENT_SELECTION)
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER());

        List<String> objectives = Registry.getEnvironmentManager()
                .getObjectives(Properties.OBJECTIVE());

        // we need to associate with each objective (branch, line) a fitness function
        for (String objective : objectives) {
            builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION(), objective);
        }

        final IGeneticAlgorithm mosa = builder.build();
        mate.testApp(mosa);
    }
}
