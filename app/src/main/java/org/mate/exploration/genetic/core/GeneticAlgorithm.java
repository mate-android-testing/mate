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
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.mate.utils.MathUtils.isEpsEq;

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

        currentGenerationNumber = 0;
    }

    @Override
    public void run() {
        createInitialPopulation();
        while (!terminationCondition.isMet()) {
            evolve();
        }
    }

    @Override
    public List<IChromosome<T>> getCurrentPopulation() {
        return population;
    }

    @Override
    public void createInitialPopulation() {
        MATE.log_acc("Creating initial population (1st generation)");
        for (int i = 0; i < populationSize; i++) {
            population.add(chromosomeFactory.createChromosome());
        }
        currentGenerationNumber++;

        logCurrentFitness();
    }

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

            }
        }

        //todo: beautify later when more time
        population.clear();
        population.addAll(newGeneration);
        logCurrentFitness();
        List<IChromosome<T>> tmp = getGenerationSurvivors();
        population.clear();
        population.addAll(tmp);
        currentGenerationNumber++;
    }

    @Override
    public List<IChromosome<T>> getGenerationSurvivors() {
        List<IChromosome<T>> survivors = new ArrayList<>(population);
        Collections.sort(survivors, new Comparator<IChromosome<T>>() {
            @Override
            public int compare(IChromosome<T> o1, IChromosome<T> o2) {
                double c = fitnessFunctions.get(0).getFitness(o2) - fitnessFunctions.get(0).getFitness(o1);
                if (isEpsEq(c)) {
                    return 0;
                }
                if (c > 0) {
                    return 1;
                }
                return -1;
            }
        });
        return survivors.subList(0, populationSize);
    }

    protected void logCurrentFitness() {
        if (population.size() <= 10 ) {
            MATE.log_acc("Fitness of generation #" + currentGenerationNumber + " :");
            for (int i = 0; i < Math.min(fitnessFunctions.size(), 5); i++) {
                MATE.log_acc("Fitness of initial population (Fitness function " + (i + 1) + "):");
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

        if (Properties.STORE_COVERAGE) {
            MATE.log_acc("Combined coverage until now: " + EnvironmentManager.getCombinedCoverage());
            if (population.size() <= 10) {
                MATE.log_acc("Combined coverage of current population: " + EnvironmentManager.getCombinedCoverage(population));
            }
        }
    }

}
