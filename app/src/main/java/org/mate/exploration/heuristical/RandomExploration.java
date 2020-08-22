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
        // number of ants created in a full ACO run, generationAmount * generationSize
        int antAmount = 20 * 10;

        antStatsLogger.write("\"Algorithm_Type\";\"Test_Case\";\"Fitness_Value\";" +
                "\"Current_Coverage\";\"Combined_Coverage\";\"Runtime\"\n");

        // Get the target line to generate a test for and initialise the fitness function
        String targetLine = Properties.TARGET_LINE();
        IFitnessFunction<TestCase> lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);

        // Log the current target line for later identification of the test
        antStatsLogger.write("\"random\";\"-\";\"-\";\"-\";\"-\";\"" + targetLine + "\"\n");

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


            antStatsLogger.write("\"random\";\"" + (i + 1) + "\";");

            double fitnessValue = lineCoveredPercentageFitnessFunction.getFitness(chromosome);
            double coverage = Registry.getEnvironmentManager().getCoverage(chromosome);
            double combinedCoverage = Registry.getEnvironmentManager().getCombinedCoverage();

            antStatsLogger.write("\"" + fitnessValue + "\";\"" +
                    coverage + "\";\"" + combinedCoverage + "\";\"");

            logCurrentRuntime(testCaseStartTime);

            // Stop algorithm if target line was reached
            if (fitnessValue == 1) {
                MATE.log_acc("Random Exploration finished successfully");

                antStatsLogger.write("\"random\";\"-\";\"-\";\"-\";\"-\";\"");
                logCurrentRuntime(algorithmStartTime);

                antStatsLogger.write("\"random\";\"-\";\"-\";\"-\";\"-\";\"successful\"\n");

                break;
            } else if (i == (antAmount - 1)) {
                MATE.log_acc("Random Exploration finished unsuccessfully");

                antStatsLogger.write("\"random\";\"-\";\"-\";\"-\";\"-\";\"");
                logCurrentRuntime(algorithmStartTime);

                antStatsLogger.write("\"random\";\"-\";\"-\";\"-\";\"-\";\"unsuccessful\"\n");

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
        antStatsLogger.write(seconds + "\"\n");
    }
}
