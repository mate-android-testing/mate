package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.ant.AntStatsLogger;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.model.TestCase;

public class RandomExploration {
    private final AndroidRandomChromosomeFactory randomChromosomeFactory;
    private final AntStatsLogger antStatsLogger;
    private final boolean alwaysReset;

    public RandomExploration(int maxNumEvents) {
        this(Properties.STORE_COVERAGE(), false, maxNumEvents);
    }

    public RandomExploration(boolean storeCoverage, boolean alwaysReset, int maxNumEvents) {
        this.alwaysReset = alwaysReset;
        randomChromosomeFactory = new AndroidRandomChromosomeFactory(storeCoverage, alwaysReset, maxNumEvents);

        antStatsLogger = new AntStatsLogger();
    }

    public void run() {
        long algorithmStartTime = System.currentTimeMillis();

        antStatsLogger.write("Algorithm Type; Test Case #; Fitness Value; Runtime\n");

        // Get the target line to generate a test for and initialise the fitness function
        String targetLine = Properties.TARGET_LINE();
        IFitnessFunction<TestCase> lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);

        if (!alwaysReset) {
            MATE.uiAbstractionLayer.resetApp();
        }

        long testCaseStartTime;

        for (int i = 0; true; i++) {
            MATE.log_acc("Exploration #" + (i + 1));

            //TODO REMOVE after successful implementation of aborting runs with fitness = 1
            //randomChromosomeFactory.createChromosome();

            testCaseStartTime = System.currentTimeMillis();

            // Store the chromosome created in each step to calculate fitness value
            IChromosome<TestCase> chromosome = randomChromosomeFactory.createChromosome();

            if (!alwaysReset) {
                MATE.uiAbstractionLayer.restartApp();
            }

            // TODO REMOVE - DEBUG
            System.out.println(lineCoveredPercentageFitnessFunction.getFitness(chromosome));


            antStatsLogger.write("random; " + (i + 1) + "; ");

            double fitnessValue = lineCoveredPercentageFitnessFunction.getFitness(chromosome);

            antStatsLogger.write(fitnessValue + "; ");

            logCurrentRuntime(testCaseStartTime);

            // Stop algorithm if target line was reached
            if (fitnessValue == 1) {
                MATE.log_acc("Random Exploration finished successfully");

                antStatsLogger.write("random; -; -; ");
                logCurrentRuntime(algorithmStartTime);

                break;
            }

            if (!alwaysReset) {
                MATE.uiAbstractionLayer.restartApp();
            }
        }
    }

    private void logCurrentRuntime (long startTime) {
        long currentTime = System.currentTimeMillis();
        currentTime = currentTime - startTime;
        long seconds = (currentTime/(1000));
        antStatsLogger.write(seconds + "\n");
    }
}
