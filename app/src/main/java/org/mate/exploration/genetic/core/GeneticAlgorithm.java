package org.mate.exploration.genetic.core;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an abstraction for the genetic algorithm.
 *
 * @param <T> The type of the chromosomes.
 */
public abstract class GeneticAlgorithm<T> implements IGeneticAlgorithm<T> {

    /**
     * The used chromosome factory, see {@link IChromosomeFactory}.
     */
    protected IChromosomeFactory<T> chromosomeFactory;

    /**
     * The used selection function, see {@link ISelectionFunction}.
     */
    protected ISelectionFunction<T> selectionFunction;

    /**
     * The used crossover function, see {@link ICrossOverFunction}.
     */
    protected ICrossOverFunction<T> crossOverFunction;

    /**
     * The used mutation function, see {@link IMutationFunction}.
     */
    protected IMutationFunction<T> mutationFunction;

    /**
     * The used fitness functions, see {@link IFitnessFunction}.
     */
    protected List<IFitnessFunction<T>> fitnessFunctions;

    /**
     * The used termination condition, see {@link ITerminationCondition}.
     */
    protected ITerminationCondition terminationCondition;

    /**
     * The used population size.
     */
    protected int populationSize;

    /**
     * The used big population size, typically twice the size of {@link #populationSize}.
     */
    protected int bigPopulationSize;

    /**
     * The current population.
     */
    protected List<IChromosome<T>> population;

    /**
     * The current generation number.
     */
    protected int currentGenerationNumber;

    /**
     * The used probability for crossover.
     */
    protected double pCrossover;

    /**
     * The used probability for mutation.
     */
    protected double pMutate;

    /**
     * Initialises the genetic algorithm with the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param selectionFunction The used selection function.
     * @param crossOverFunction The used crossover function.
     * @param mutationFunction The used mutation function.
     * @param fitnessFunctions The list of fitness functions.
     * @param terminationCondition The used termination condition.
     * @param populationSize The population size.
     * @param bigPopulationSize The big population size.
     * @param pCrossover The probability rate for crossover.
     * @param pMutate The probability rate for mutation.
     */
    public GeneticAlgorithm(IChromosomeFactory<T> chromosomeFactory,
                            ISelectionFunction<T> selectionFunction,
                            ICrossOverFunction<T> crossOverFunction,
                            IMutationFunction<T> mutationFunction,
                            List<IFitnessFunction<T>> fitnessFunctions,
                            ITerminationCondition terminationCondition,
                            int populationSize,
                            int bigPopulationSize,
                            double pCrossover,
                            double pMutate) {

        this.chromosomeFactory = chromosomeFactory;
        this.selectionFunction = selectionFunction;
        this.crossOverFunction = crossOverFunction;
        this.mutationFunction = mutationFunction;
        this.fitnessFunctions = fitnessFunctions;
        this.terminationCondition = terminationCondition;

        this.populationSize = populationSize;
        this.bigPopulationSize = bigPopulationSize;
        this.population = new ArrayList<>();
        this.pCrossover = pCrossover;
        this.pMutate = pMutate;

        currentGenerationNumber = 0;
    }

    /**
     * This is the entry point of each genetic algorithm. It generates an initial population and
     * then evolves the population by means of selection, crossover and mutation until the termination
     * condition is met.
     */
    @Override
    public void run() {
        createInitialPopulation();
        while (!terminationCondition.isMet()) {
            evolve();
        }
    }

    /**
     * Returns the current population.
     *
     * @return Returns the current population.
     */
    @Override
    public List<IChromosome<T>> getCurrentPopulation() {
        return population;
    }

    /**
     * Creates the initial population.
     */
    @Override
    public void createInitialPopulation() {

        MATE.log_acc("Creating initial population (1st generation)");

        for (int i = 0; i < populationSize; i++) {
            population.add(chromosomeFactory.createChromosome());
        }

        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * The current population is evolved by means of selection, crossover and mutation. In particular,
     * new chromosomes are sampled from the current population until the {@link #bigPopulationSize}
     * is reached. Each of these chromosomes may undergo a crossover and mutation depending on the
     * selected probabilities for crossover and mutation respectively. Once the big population is
     * filled, the final population is formed by removing chromosomes from the big population based
     * on the implementation of {@link #getGenerationSurvivors()}.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));
        List<IChromosome<T>> newGeneration = new ArrayList<>(population);

        while (newGeneration.size() < bigPopulationSize) {
            List<IChromosome<T>> parents = selectionFunction.select(population, fitnessFunctions);

            IChromosome<T> parent;

            if (Randomness.getRnd().nextDouble() < pCrossover) {
                parent = crossOverFunction.cross(parents);
            } else {
                parent = parents.get(0);
            }

            IChromosome<T> offspring;

            if (Randomness.getRnd().nextDouble() < pMutate) {
                offspring = mutationFunction.mutate(parent);
            } else {
                offspring = parent;
            }

            newGeneration.add(offspring);
        }

        // TODO: beautify later when more time
        population.clear();
        population.addAll(newGeneration);
        List<IChromosome<T>> survivors = getGenerationSurvivors();
        population.clear();
        population.addAll(survivors);
        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Determines the survivors of the current generation. By default, the newly created offsprings
     * constitute the survivors.
     *
     * @return Returns a population of size {@link #populationSize} that is used in the next generation.
     */
    @Override
    public List<IChromosome<T>> getGenerationSurvivors() {
        return new ArrayList<>(population.subList(population.size() - populationSize, population.size()));
    }

    /**
     * Logs the fitness of the current population.
     */
    protected void logCurrentFitness() {

        if (population.size() <= 10) {
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

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {
            MATE.log_acc("Combined coverage until now: "
                    + CoverageUtils.getCombinedCoverage(Properties.COVERAGE()));
            if (population.size() <= 10) {
                MATE.log_acc("Combined coverage of current population: "
                        + CoverageUtils.getCombinedCoverage(Properties.COVERAGE(), population));
            }
        }
    }
}
