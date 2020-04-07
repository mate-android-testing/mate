package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;

public class RandomExploration {
    private final AndroidRandomChromosomeFactory randomChromosomeFactory;
    private final boolean alwaysReset;

    public RandomExploration(int maxNumEvents) {
        this(Properties.STORE_COVERAGE(), false, maxNumEvents);
    }

    public RandomExploration(boolean storeCoverage, boolean alwaysReset, int maxNumEvents) {
        this.alwaysReset = alwaysReset;
        randomChromosomeFactory = new AndroidRandomChromosomeFactory(storeCoverage, alwaysReset, maxNumEvents);
    }

    public void run() {
        // Get the target line to generate a test for and initialise the fitness function
        String targetLine = Properties.TARGET_LINE();
        IFitnessFunction<TestCase> lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);

        if (!alwaysReset) {
            MATE.uiAbstractionLayer.resetApp();
        }
        for (int i = 0; true; i++) {
            MATE.log_acc("Exploration #" + (i + 1));

            //TODO REMOVE after successful implementation of aborting runs with fitness = 1
            //randomChromosomeFactory.createChromosome();

            // Store the chromosome created in each step to calculate fitness value
            IChromosome<TestCase> chromosome = randomChromosomeFactory.createChromosome();

            if (!alwaysReset) {
                MATE.uiAbstractionLayer.restartApp();
            }

            // TODO REMOVE - DEBUG
            System.out.println(lineCoveredPercentageFitnessFunction.getFitness(chromosome));

            // Stop algorithm if target line was reached
            if (lineCoveredPercentageFitnessFunction.getFitness(chromosome) == 1) {
                MATE.log_acc("Random Exploration finished successfully");
                break;
            }

            if (!alwaysReset) {
                MATE.uiAbstractionLayer.restartApp();
            }
        }
    }
}
