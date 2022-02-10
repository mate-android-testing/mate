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
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.util.ge.GEMappingFunction;

import java.util.List;

public class ExecuteMATEEqualWeightedGE {

    public static void run(String packageName, Context context) {
        MATELog.log_acc("Starting Equal Weighted GE Algorithm...");

        MATE mate = new MATE(packageName, context);

        final IGeneticAlgorithm<List<Integer>> equalWeightedGE = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.STANDARD_GA)
                .withChromosomeFactory(ChromosomeFactory.INTEGER_SEQUENCE_CHROMOSOME_FACTORY)
                .withSelectionFunction(Properties.SELECTION_FUNCTION())
                .withGEMappingFunction(GEMappingFunction.LIST_BASED_EQUAL_WEIGHTED_MAPPING)
                .withFitnessFunction(FitnessFunction.GENO_TO_PHENO_TYPE)
                .withCrossoverFunction(CrossOverFunction.INTEGER_SEQUENCE_POINT_CROSS_OVER)
                .withMutationFunction(MutationFunction.INTEGER_SEQUENCE_POINT_MUTATION)
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .build();

        mate.testApp(equalWeightedGE);
    }
}
