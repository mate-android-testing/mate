package org.mate.exploration.genetic;

import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class GeneticAlgorithm<T> implements IGeneticAlgorithm<T> {
    protected IChromosomeFactory<T> chromosomeFactory;
    protected ISelectionFunction<T> selectionFunction;
    protected ICrossOverFunction<T> crossOverFunction;
    protected IMutationFunction<T> mutationFunction;
    protected List<IFitnessFunction<T>> fitnessFunctions;
    protected ITerminationCondition terminationCondition;

    protected int populationSize;
    protected int generationSurvivorCount;
    protected List<IChromosome<T>> population;
    protected int currentGenerationNumber;
    protected float pCrossover;
    protected float pMutate;


    public GeneticAlgorithm(IChromosomeFactory<T> chromosomeFactory, ISelectionFunction<T>
            selectionFunction, ICrossOverFunction<T> crossOverFunction, IMutationFunction<T> mutationFunction, List<IFitnessFunction<T>> fitnessFunctions, ITerminationCondition terminationCondition, int populationSize, int generationSurvivorCount, float pCrossover, float pMutate) {
        this.chromosomeFactory = chromosomeFactory;
        this.selectionFunction = selectionFunction;
        this.crossOverFunction = crossOverFunction;
        this.mutationFunction = mutationFunction;
        this.fitnessFunctions = fitnessFunctions;
        this.terminationCondition = terminationCondition;

        this.populationSize = populationSize;
        this.generationSurvivorCount = generationSurvivorCount;
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
        currentGenerationNumber++;
        for (int i = 0; i < populationSize; i++) {
            population.add(chromosomeFactory.createChromosome());
        }

    }

    @Override
    public void evolve() {
        List<IChromosome<T>> survivors = getGenerationSurvivors();
        List<IChromosome<T>> newGeneration = new ArrayList<>(survivors);

        while (newGeneration.size() < populationSize) {
            List<IChromosome<T>> parents = selectionFunction.select(survivors, fitnessFunctions);

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
                if (newGeneration.size() == populationSize)
                    break;

                newGeneration.add(chromosome);

            }
        }

        population.clear();
        population.addAll(newGeneration);
        currentGenerationNumber++;
    }

    private List<IChromosome<T>> getGenerationSurvivors() {
        List<IChromosome<T>> survivors = new ArrayList<>(population);
        Collections.sort(survivors, new Comparator<IChromosome<T>>() {
            @Override
            public int compare(IChromosome<T> o1, IChromosome<T> o2) {
                double c = fitnessFunctions.get(0).getFitness(o2) - fitnessFunctions.get(0).getFitness(o1);
                if (c > 0) {
                    return 1;
                } else if (c < 0) {
                    return -1;
                }
                return 0;
            }
        });
        return survivors.subList(0, generationSurvivorCount);
    }
}
