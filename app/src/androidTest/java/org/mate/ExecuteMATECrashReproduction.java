package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATECrashReproduction {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting EDA strategy ...");
        MATE mate = new MATE();

        GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.EDA)
                .withChromosomeFactory(ChromosomeFactory.EDA_CHROMOSOME_FACTORY)
                .withFitnessFunction(FitnessFunction.CRASH_DISTANCE)
                .withTerminationCondition(TerminationCondition.CONDITIONAL_TERMINATION)
                .withPopulationSize(Properties.POPULATION_SIZE());

        final IGeneticAlgorithm eda = builder.build();
        mate.testApp(eda);
    }
}
