package org.mate.exploration.genetic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GeneticAlgorithm<T> implements IGeneticAlgorithm<T> {
    protected int populationSize;
    protected int generationSurvivorCount;
    protected Map<String, IChromosome<T>> population;
    protected int currentGenerationNumber;
    protected float pCrossover;
    protected float pMutate;

    public GeneticAlgorithm(int populationSize, int generationSurvivorCount, float pCrossover, float pMutate) {
        if (generationSurvivorCount >= populationSize) {
            throw new IllegalArgumentException("The survival count of a generation must be smaller than the population size. Otherwise the population will become stagnant");
        }
        if (generationSurvivorCount < 2) {
            throw new IllegalArgumentException("Generation survivor count has be greater or equal to 2");
        }

        this.populationSize = populationSize;
        this.generationSurvivorCount = generationSurvivorCount;
        this.pCrossover = pCrossover;
        this.pMutate = pMutate;

        currentGenerationNumber = 1;
    }

    @Override
    public abstract void run();

    @Override
    public Map<String, IChromosome<T>> getCurrentPopulation() {
        return population;
    }

    @Override
    public void createInitialPopulation() {
        population = new HashMap<>();

        for (int i = 0; i < populationSize; i++) {
            IChromosome<T> newChromosome = createInitialRandomChromosome();
            population.put(newChromosome.getId(), newChromosome);
        }

    }

    @Override
    public abstract void evolve();

    protected abstract List<IChromosome<T>> mutate(IChromosome<T> chromosome);

    protected abstract IChromosome<T> createInitialRandomChromosome();
}
