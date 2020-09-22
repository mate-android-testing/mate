package org.mate.exploration.genetic.core;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.ant.AntStatsLogger;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Coverage;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Abstract class that serves as a basis for genetic algorithms
 * @param <T> Type wrapped by the chromosome implementation
 */
public abstract class GeneticAlgorithm<T> implements IGeneticAlgorithm<T> {
    protected IChromosomeFactory<T> chromosomeFactory;
    protected ISelectionFunction<T> selectionFunction;
    protected ICrossOverFunction<T> crossOverFunction;
    protected IMutationFunction<T> mutationFunction;
    protected List<IFitnessFunction<T>> fitnessFunctions;
    protected ITerminationCondition terminationCondition;

    protected int populationSize;
    protected int bigPopulationSize;
    protected List<IChromosome<T>> population;
    protected int currentGenerationNumber;
    protected double pCrossover;
    protected double pMutate;

    private final AntStatsLogger antStatsLogger;
    private Boolean algorithmFinished = FALSE;

    /**
     * Initializing the genetic algorithm with all necessary attributes
     * @param chromosomeFactory see {@link IChromosomeFactory}
     * @param selectionFunction see {@link ISelectionFunction}
     * @param crossOverFunction see {@link ICrossOverFunction}
     * @param mutationFunction see {@link IMutationFunction}
     * @param fitnessFunctions see {@link IFitnessFunction}
     * @param terminationCondition see {@link ITerminationCondition}
     * @param populationSize size of population kept by the genetic algorithm
     * @param bigPopulationSize size which population will temporarily be after creating offspring
     * @param pCrossover probability that crossover occurs (between 0 and 1)
     * @param pMutate probability that mutation occurs (between 0 and 1)
     */
    public GeneticAlgorithm(IChromosomeFactory<T> chromosomeFactory, ISelectionFunction<T>
            selectionFunction, ICrossOverFunction<T> crossOverFunction, IMutationFunction<T> mutationFunction, List<IFitnessFunction<T>> fitnessFunctions, ITerminationCondition terminationCondition, int populationSize, int bigPopulationSize, double pCrossover, double pMutate) {
        this.chromosomeFactory = chromosomeFactory;
        this.selectionFunction = selectionFunction;
        this.crossOverFunction = crossOverFunction;
        this.mutationFunction = mutationFunction;
        this.fitnessFunctions = fitnessFunctions;
        this.terminationCondition = terminationCondition;

        this.populationSize = populationSize;
        this.bigPopulationSize = bigPopulationSize;
        population = new ArrayList<>();
        this.pCrossover = pCrossover;
        this.pMutate = pMutate;

        // Create a logger to store collected data during the run
        antStatsLogger = new AntStatsLogger();

        currentGenerationNumber = 0;
    }

    @Override
    public void run() {
        // Store the start time of the algorithm for later runtime calculation
        long algorithmStartTime = System.currentTimeMillis();

        // Add the column headlines to the log file
        antStatsLogger.write("\"Algorithm_Type\";\"Generation_Number\";\"Population_Number\"" +
                ";\"Fitness_Value\";\"Current_Coverage\";\"Combined_Coverage\";\"Runtime\"\n");

        createInitialPopulation();

        boolean successful = TRUE;
        int iterations = 1;

        // Run the algorithm unit the max amount of test cases was executed
        if(!algorithmFinished) {
            while (!terminationCondition.isMet()) {
                if (iterations < 20) {
                    evolve();

                    // Exit the loop if a successful test case was found
                    if(algorithmFinished) {
                        break;
                    }
                    iterations++;
                } else {
                    successful = FALSE;
                    break;
                }
            }
        }

        // Log the runtime of the algorithm
        antStatsLogger.write("\"genetic\";\"-\";\"-\";\"-\";\"-\";\"-\";\"");
        logCurrentRuntime(algorithmStartTime);

        // Log the final result
        if(successful) {
            antStatsLogger.write("\"genetic\";\"-\";\"-\";\"-\";\"-\";\"-\";\"successful\"");
        } else {
            antStatsLogger.write("\"genetic\";\"-\";\"-\";\"-\";\"-\";\"-\";\"unsuccessful\"");
        }

        // Close the logger
        antStatsLogger.close();
    }

    @Override
    public List<IChromosome<T>> getCurrentPopulation() {
        return population;
    }

    @Override
    public void createInitialPopulation() {
        // Store the start time of the generation for later runtime calculation
        long generationStartTime = System.currentTimeMillis();
        MATE.log_acc("Creating initial population (1st generation)");

        // Get the target line to generate a test for and initialise the fitness function
        String targetLine = Properties.TARGET_LINE();
        IFitnessFunction<TestCase> lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);

        // Log the current target line for later identification of the test
        antStatsLogger.write("\"genetic\";\"-\";\"-\";\"-\";\"-\";\"-\";\"" + targetLine + "\"\n");

        for (int i = 0; i < populationSize; i++) {
            // Store the start time of the population for later runtime calculation
            long populationStartTime = System.currentTimeMillis();

            IChromosome<T> chromosomeT = chromosomeFactory.createChromosome();
            IChromosome<TestCase> chromosome = (IChromosome<TestCase>) chromosomeT;

            LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);

            // Retrieve the relevant test case data
            double fitnessValue = lineCoveredPercentageFitnessFunction.getFitness(chromosome);
            double coverage = Registry.getEnvironmentManager().getCoverage(chromosome);
            double combinedCoverage = Registry.getEnvironmentManager().getCombinedCoverage();

            population.add(chromosomeT);

            MATE.log_acc("Fitness Value: " + fitnessValue);

            // Log the relevant test case data
            antStatsLogger.write("\"genetic\";\"" + (currentGenerationNumber + 1) + "\";\""
                    + (i + 1) + "\";\"" + fitnessValue + "\";\"" + coverage + "\";\""
                    + combinedCoverage + "\";\"");
            logCurrentRuntime(populationStartTime);

            // Check if the current generated test case was successful
            if(fitnessValue == 1.0) {
                algorithmFinished = TRUE;
                break;
            }

            MATE.log_acc("Algorithm Finished: " + algorithmFinished);
        }

        // Log the runtime for the current generation
        antStatsLogger.write("\"genetic\";\"" + (currentGenerationNumber + 1) + "\";\"-\";\"-\";" +
                "\"-\";\"-\";\"");
        logCurrentRuntime(generationStartTime);

        logCurrentFitness();
        currentGenerationNumber++;
    }

    @Override
    public void evolve() {
        // Store the start time of the generation for later runtime calculation
        long generationStartTime = System.currentTimeMillis();

        // Initialise a variable to keep track of the current test case number
        int testCaseCount = 0;

        // Get the target line to generate a test for and initialise the fitness function
        String targetLine = Properties.TARGET_LINE();
        IFitnessFunction<TestCase> lineCoveredPercentageFitnessFunction
                = new LineCoveredPercentageFitnessFunction(targetLine);

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        outerLoop: while (newGeneration.size() < bigPopulationSize) {
            // Store the start time for the current test case
            long testCaseStartTime = System.currentTimeMillis();

            List<IChromosome<T>> parents = selectionFunction.select(population, fitnessFunctions);

            IChromosome<T> parent;

            if (Randomness.getRnd().nextDouble() < pCrossover) {
                parent = crossOverFunction.cross(parents);
            } else {
                parent = parents.get(0);
            }

            List<IChromosome<T>> offspring = new ArrayList<>();
            offspring.add(parent);
            if (Randomness.getRnd().nextDouble() < pMutate) {
                offspring = mutationFunction.mutate(parent);
            }

            for (IChromosome<T> chromosome : offspring) {
                if (newGeneration.size() == bigPopulationSize) {
                    break;
                }

                newGeneration.add(chromosome);

                testCaseCount++;

                IChromosome<TestCase> iChromosome = (IChromosome<TestCase>) chromosome;

                // Retrieve the relevant test case data
                double fitnessValue = lineCoveredPercentageFitnessFunction.getFitness(iChromosome);
                double coverage = Registry.getEnvironmentManager().getCoverage(iChromosome);
                double combinedCoverage = Registry.getEnvironmentManager().getCombinedCoverage();

                // Log the relevant test case data
                antStatsLogger.write("\"genetic\";\"" + (currentGenerationNumber + 1) + "\";\""
                        + testCaseCount + "\";\"" + fitnessValue + "\";\"" + coverage + "\";\""
                        + combinedCoverage + "\";\"");
                logCurrentRuntime(testCaseStartTime);

                // Check if the current generated test case was successful
                if(fitnessValue == 1.0) {
                    algorithmFinished = TRUE;
                    break outerLoop;
                }
            }
        }

        //todo: beautify later when more time
        population.clear();
        population.addAll(newGeneration);
        List<IChromosome<T>> tmp = getGenerationSurvivors();
        population.clear();
        population.addAll(tmp);
        // TODO log new generation infos

        // Log the runtime for the current generation
        antStatsLogger.write("\"genetic\";\"" + (currentGenerationNumber + 1) + "\";\"-\";" +
                "\"-\";\"-\";\"-\";\"");
        logCurrentRuntime(generationStartTime);

        logCurrentFitness();
        currentGenerationNumber++;
    }

    @Override
    public List<IChromosome<T>> getGenerationSurvivors() {
        return new ArrayList<>(population.subList(population.size() - populationSize, population.size()));
    }

    protected void logCurrentFitness() {
        if (population.size() <= 10 ) {
            MATE.log_acc("Fitness of generation #" + (currentGenerationNumber + 1) + " :");
            for (int i = 0; i < Math.min(fitnessFunctions.size(), 5); i++) {
                MATE.log_acc("Fitness function " + (i + 1) + ":");
                IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(i);
                for (int j = 0; j < population.size(); j++) {
                    IChromosome<T> chromosome = population.get(j);
                    MATE.log_acc("Chromosome " + (j + 1) + ": "
                            + fitnessFunction.getFitness(chromosome));
                }
            }
            if (fitnessFunctions.size() > 5) {
                MATE.log_acc("Omitted other fitness function because there are to many (" + fitnessFunctions.size() + ")");
            }
        }

        if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {
            MATE.log_acc("Total Coverage: " + Registry.getEnvironmentManager().getBranchCoverage());
        } else if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
            MATE.log_acc("Combined coverage until now: " + Registry.getEnvironmentManager().getCombinedCoverage());
            if (population.size() <= 10) {
                MATE.log_acc("Combined coverage of current population: " + Registry.getEnvironmentManager().getCombinedCoverage(population));
            }
        }
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