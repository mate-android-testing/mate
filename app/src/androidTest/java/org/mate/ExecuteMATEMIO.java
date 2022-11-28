package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEMIO {


    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("MIO algorithm");

        MATE mate = new MATE();

        GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.MIO)
                .withChromosomeFactory(Properties.CHROMOSOME_FACTORY())
                .withMutationFunctions(Properties.MUTATION_FUNCTIONS())
                .withTerminationCondition(Properties.TERMINATION_CONDITION())
                .withPopulationSize(Properties.POPULATION_SIZE())
                .withBigPopulationSize(Properties.BIG_POPULATION_SIZE())
                .withPMutate(Properties.P_MUTATE())
                .withPCrossover(Properties.P_CROSSOVER())
                .withPSampleRandom(Properties.P_SAMPLE_RANDOM())
                .withFocusedSearchStart(Properties.P_FOCUSED_SEARCH_START())
                .withMutationRate(Properties.MUTATION_RATE());

        int numberOfObjectives
                = Registry.getEnvironmentManager().getNumberOfObjectives(Properties.OBJECTIVE());

        // we need to associate with each objective (branch, line) a fitness function
        for (int i = 0; i < numberOfObjectives; i++) {
            builder = builder.withFitnessFunction(Properties.FITNESS_FUNCTION());
        for (String objective : objectives) {
            builder = builder.withFitnessFunctions(Properties.FITNESS_FUNCTIONS(), objective);
        }

        final IGeneticAlgorithm mio = builder.build();
        mate.testApp(mio);
    }
}

