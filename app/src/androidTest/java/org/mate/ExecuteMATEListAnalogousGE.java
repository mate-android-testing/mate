package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.util.ge.GEMappingFunction;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEListAnalogousGE {
    @Test
    public void useAppContext() {
        MATE.log_acc("Starting List Analogous GE Algorithm...");

        MATE mate = new MATE();

        FitnessFunction[] singleFitnessFunction = Properties.FITNESS_FUNCTIONS();

        if (singleFitnessFunction.length > 1) {
            throw new IllegalArgumentException("List Analogous Genetic Algorithm takes only one "
                    + "fitness function.");
        }

        final IGeneticAlgorithm<List<Integer>> listAnalogousGE = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.STANDARD_GA)
                .withChromosomeFactory(ChromosomeFactory.INTEGER_SEQUENCE_CHROMOSOME_FACTORY)
                .withSelectionFunction(Properties.SELECTION_FUNCTION())
                .withGEMappingFunction(GEMappingFunction.LIST_ANALOGOUS_MAPPING)
                .withGenoToPhenoType()
                .withFitnessFunctions(singleFitnessFunction)
                .withCrossoverFunction(CrossOverFunction.INTEGER_SEQUENCE_POINT_CROSS_OVER)
                .withMutationFunction(MutationFunction.INTEGER_SEQUENCE_LENGTH_MUTATION)
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .build();

        mate.testApp(listAnalogousGE);
    }
}
