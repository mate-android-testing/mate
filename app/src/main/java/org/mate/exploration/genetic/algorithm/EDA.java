package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.ActionFitnessFunctionWrapper;
import org.mate.exploration.genetic.fitness.GenotypePhenotypeMappedFitnessFunction;
import org.mate.exploration.genetic.fitness.IActionFitnessFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;
import org.mate.model.TestCase;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic exploration using an estimation of distribution algorithm (EDA).
 *
 * @param <T> Refers to the type of chromosome.
 */
public class EDA<T> extends GeneticAlgorithm<T> {

    /**
     * The probabilistic model encoding information about the current population.
     */
    private final IProbabilisticModel<T> probabilisticModel;

    /**
     * Wraps the underlying fitness functions such that we can store and retrieve the fitness after
     * individual actions.
     */
    private final List<ActionFitnessFunctionWrapper> fitnessFunctions = new ArrayList<>();

    /**
     * Initialises the estimation of distribution algorithm with the necessary attributes.
     *
     * @param chromosomeFactory The used chromosome factory.
     * @param fitnessFunctions The list of fitness functions.
     * @param terminationCondition The used termination condition.
     * @param populationSize The population size.
     */
    public EDA(final IChromosomeFactory<T> chromosomeFactory,
               final List<IFitnessFunction<T>> fitnessFunctions,
               final ITerminationCondition terminationCondition,
               final int populationSize,
               final IProbabilisticModel<T> probabilisticModel) {
        super(chromosomeFactory, null, null, null,
                fitnessFunctions, terminationCondition, populationSize,
                populationSize, 0.0, 0.0);
        this.probabilisticModel = probabilisticModel;
        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
            this.fitnessFunctions.add(new ActionFitnessFunctionWrapper(
                    (IActionFitnessFunction<TestCase>) fitnessFunction));
        }
    }

    /**
     * Creates the initial population and updates the probabilistic model.
     */
    @Override
    public void createInitialPopulation() {

        MATE.log_acc("Creating initial population (1st generation)");

        for (int i = 0; i < populationSize; i++) {
            population.add(chromosomeFactory.createChromosome());
        }

        probabilisticModel.update(population);
        logCurrentFitness();
        Registry.getEnvironmentManager().invalidateTracesCache();
        currentGenerationNumber++;
    }

    /**
     * Creates a new population and updates the probabilistic model.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));

        population.clear();

        for (int i = 0; i < populationSize; i++) {
            population.add(chromosomeFactory.createChromosome());
        }

        probabilisticModel.update(population);
        logCurrentFitness();
        Registry.getEnvironmentManager().invalidateTracesCache();
        currentGenerationNumber++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <S> void logCurrentFitness() {

        MATE.log_acc("Fitness of generation #" + (currentGenerationNumber + 1) + ":");

        // TODO: Use chromosome id in logs instead of natural index + find a better solution for
        //  multi-objective algorithms + a fix for MIO/MOSA/NSGA-II used in combination with GE.

        for (int i = 0; i < this.fitnessFunctions.size(); i++) {
            MATE.log_acc("Fitness function " + (i + 1) + ":");
            // We can use the cached fitness values here and avoid an unnecessary re-computation.
            final ActionFitnessFunctionWrapper fitnessFunction = this.fitnessFunctions.get(i);
            for (int j = 0; j < population.size(); j++) {
                IChromosome<TestCase> chromosome = (IChromosome<TestCase>) population.get(j);
                MATE.log_acc("Chromosome " + (j + 1) + ": " + fitnessFunction.getFitness(chromosome));
            }
        }

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {

            MATE.log_acc("Combined coverage until now: "
                    + CoverageUtils.getCombinedCoverage(Properties.COVERAGE()));

            if (Properties.GENO_TO_PHENO_TYPE_MAPPING()) {

                final List<IChromosome<T>> phenotypePopulation = new ArrayList<>();

                for (IChromosome<T> chromosome : population) {
                    // TODO: Fix this mismatch between the type variables!
                    phenotypePopulation.add(
                            GenotypePhenotypeMappedFitnessFunction
                                    .getPhenoType((IChromosome<S>) chromosome));
                }

                MATE.log_acc("Combined coverage of current population: "
                        + CoverageUtils.getCombinedCoverage(Properties.COVERAGE(), phenotypePopulation));
            } else {
                MATE.log_acc("Combined coverage of current population: "
                        + CoverageUtils.getCombinedCoverage(Properties.COVERAGE(), population));
            }
        }
    }
}
