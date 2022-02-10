package org.mate.service.execution;

import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.model.TestCase;

public class ExecuteMATEPrimitiveStandardGA {

    public static void run(String packageName, Context context) {
        MATELog.log_acc("Starting Evolutionary Search...");
        MATELog.log_acc("Primitive StandardGeneticAlgorithm implementation");

        MATE mate = new MATE(packageName, context);

        final IGeneticAlgorithm<TestCase> genericGA = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.STANDARD_GA)
                .withChromosomeFactory(ChromosomeFactory.PRIMITIVE_ANDROID_RANDOM_CHROMOSOME_FACTORY)
                .withSelectionFunction(Properties.SELECTION_FUNCTION())
                .withCrossoverFunction(CrossOverFunction.PRIMITIVE_TEST_CASE_MERGE_CROSS_OVER)
                .withMutationFunction(MutationFunction.PRIMITIVE_SHUFFLE_MUTATION)
                .withFitnessFunction(Properties.FITNESS_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .build();

        mate.testApp(genericGA);
    }
}
