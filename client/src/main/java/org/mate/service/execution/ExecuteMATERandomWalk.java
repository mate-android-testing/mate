package org.mate.service.execution;

import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

public class ExecuteMATERandomWalk {

    public static void run(String packageName, Context context) {

        MATELog.log_acc("Starting Random Walk ....");
        MATE mate = new MATE(packageName, context);

        final GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.RANDOM_WALK)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withFitnessFunction(Properties.FITNESS_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION());

        final IGeneticAlgorithm randomWalk = builder.build();
        mate.testApp(randomWalk);
    }
}
