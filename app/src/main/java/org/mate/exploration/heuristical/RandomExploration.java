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

        // Create a logger to store collected data during the run
        antStatsLogger = new AntStatsLogger();
    }

    public void run() {
        // Store the start time of the algorithm for later runtime calculation
        long algorithmStartTime = System.currentTimeMillis();

        // Maximum amount of test cases equal to ants in aco, generationAmount * generationSize
        int antAmount = 40 * 10;

        // Add the column headlines to the log file
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

        // Declare a variable for the storing of the start time for each test case
        long testCaseStartTime;

        // Loop to create the individual test cases and log collected information during them
        for (int i = 0; true; i++) {
            MATE.log_acc("Exploration #" + (i + 1));

            // Store the start time for the current test case
            testCaseStartTime = System.currentTimeMillis();

            // Store the chromosome created in each step to calculate fitness value
            IChromosome<TestCase> chromosome = randomChromosomeFactory.createChromosome();

            if (!alwaysReset) {
                MATE.uiAbstractionLayer.restartApp();
            }

            // Retrieve the relevant test case data
            double fitnessValue = lineCoveredPercentageFitnessFunction.getFitness(chromosome);
            double coverage = Registry.getEnvironmentManager().getCoverage(chromosome);
            double combinedCoverage = Registry.getEnvironmentManager().getCombinedCoverage();

            // Log the relevant test case data
            antStatsLogger.write("\"random\";\"" + (i + 1) + "\";\"" + fitnessValue + "\";\"" +
                    coverage + "\";\"" + combinedCoverage + "\";\"");
            logCurrentRuntime(testCaseStartTime);

            // Stop algorithm if the target line or if the max amount of test cases is reached
            if (fitnessValue == 1) {
                MATE.log_acc("Random Exploration finished successfully");

                // Log algorithm runtime and results into the file
                antStatsLogger.write("\"random\";\"-\";\"-\";\"-\";\"-\";\"");
                logCurrentRuntime(algorithmStartTime);
                antStatsLogger.write("\"random\";\"-\";\"-\";\"-\";\"-\";\"successful\"\n");

                break;
            } else if (i == (antAmount - 1)) {
                MATE.log_acc("Random Exploration finished unsuccessfully");

                // Log algorithm runtime and results into the file
                antStatsLogger.write("\"random\";\"-\";\"-\";\"-\";\"-\";\"");
                logCurrentRuntime(algorithmStartTime);
                antStatsLogger.write("\"random\";\"-\";\"-\";\"-\";\"-\";\"unsuccessful\"\n");

                break;
            }

            if (!alwaysReset) {
                MATE.uiAbstractionLayer.restartApp();
            }
        }

        // Close the logger
        antStatsLogger.close();
    }

    /**
     * Log the time past from a certain point in time until now
     * @param startTime the start point to calculate the time difference from
     */
    private void logCurrentRuntime (long startTime) {
        // Get the current time
        long currentTime = System.currentTimeMillis();

        // Calculate the time difference in seconds
        long secondsPast = (currentTime - startTime)/(1000);

        // Log the calculated time in the file
        antStatsLogger.write(secondsPast + "\"\n");
    }
}
