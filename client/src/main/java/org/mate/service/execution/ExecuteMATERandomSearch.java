package org.mate.service.execution;

import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

public class ExecuteMATERandomSearch {

    public static void run(String packageName, Context context) {
        MATELog.log_acc("Starting Random Search GA ....");

        MATE mate = new MATE(packageName, context);

        final IGeneticAlgorithm randomSearch = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.RANDOM_SEARCH)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withFitnessFunction(Properties.FITNESS_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .build();

        mate.testApp(randomSearch);
    }
}
