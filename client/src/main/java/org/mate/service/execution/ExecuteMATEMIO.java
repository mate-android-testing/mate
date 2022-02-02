package org.mate.service.execution;

import android.content.Context;

import org.mate.commons.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

import java.util.List;

public class ExecuteMATEMIO {


    public static void run(String packageName, IRepresentationLayerInterface representationLayer, Context context) {
        MATELog.log_acc("Starting Evolutionary Search...");
        MATELog.log_acc("MIO algorithm");

        MATE mate = new MATE(packageName, representationLayer, context);

        GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.MIO)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withMutationFunction(Properties.MUTATION_FUNCTION())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .withPSampleRandom(Properties.P_SAMPLE_RANDOM())
                .withFocusedSearchStart(Properties.P_FOCUSED_SEARCH_START())
                .withMutationRate(Properties.MUTATION_RATE());

        List<String> objectives = Registry.getEnvironmentManager().getObjectives(Properties.OBJECTIVE());

        for (String objective : objectives) {
            builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION(), objective);
        }

        final IGeneticAlgorithm mio = builder.build();
        mate.testApp(mio);
    }
}

