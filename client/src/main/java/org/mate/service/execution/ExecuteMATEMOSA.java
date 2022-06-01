package org.mate.service.execution;

import android.content.Context;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.selection.SelectionFunction;

public class ExecuteMATEMOSA {

    public static void run(String packageName, Context context) {

        MATELog.log_acc("Starting Evolutionary Search...");
        MATELog.log_acc("MOSA algorithm");

        MATE mate = new MATE(packageName, context);

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

        int numberOfObjectives
                = Registry.getEnvironmentManager().getNumberOfObjectives(Properties.OBJECTIVE());

        // we need to associate with each objective (branch, line) a fitness function
        for (int i = 0; i < numberOfObjectives; i++) {
            builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION());
        }

        final IGeneticAlgorithm mosa = builder.build();
        mate.testApp(mosa);
    }
}
