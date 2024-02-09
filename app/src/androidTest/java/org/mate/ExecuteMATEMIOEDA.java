package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.termination.TerminationCondition;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEMIOEDA {


    @Test
    public void useAppContext() {
        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("MIO-EDA algorithm");

        MATE mate = new MATE();

        GeneticAlgorithmBuilder builder = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.MIOEDA)
                .withChromosomeFactory(ChromosomeFactory.MIO_EDA_CHROMOSOME_FACTORY)
                .withTerminationCondition(TerminationCondition.CONDITIONAL_TERMINATION)
                .withFocusedSearchStart(Properties.P_FOCUSED_SEARCH_START())
                .withPSampleRandom(Properties.P_SAMPLE_RANDOM());

        int numberOfObjectives
                = Registry.getEnvironmentManager().getNumberOfObjectives(Properties.OBJECTIVE());

        // we need to associate with each objective (branch, line) a fitness function
        builder = builder.withFitnessFunctions(Properties.FITNESS_FUNCTION(), numberOfObjectives);

        final IGeneticAlgorithm mioeda = builder.build();
        mate.testApp(mioeda);
    }
}

