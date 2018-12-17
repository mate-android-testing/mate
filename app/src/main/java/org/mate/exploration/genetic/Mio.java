package org.mate.exploration.genetic;

import org.mate.utils.Randomness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Mio<T> extends GeneticAlgorithm {

    private final float pSampleRandom;
    private final float focusedSearchStart;
    private HashMap<IFitnessFunction, LinkedList<IChromosome>> archive;

    /**
     * Initializing the genetic algorithm with all necessary attributes
     *
     * @param chromosomeFactory       see {@link IChromosomeFactory}
     * @param selectionFunction       see {@link ISelectionFunction}
     * @param crossOverFunction       see {@link ICrossOverFunction}
     * @param mutationFunction        see {@link IMutationFunction}
     * @param fitnessFunctions                    see {@link IFitnessFunction}
     * @param terminationCondition    see {@link ITerminationCondition}
     * @param populationSize          size of population kept by the genetic algorithm
     * @param generationSurvivorCount amount of survivors of each generation
     * @param pCrossover              probability that crossover occurs (between 0 and 1)
     * @param pMutate                 probability that mutation occurs (between 0 and 1)
     * @param pSampleRandom           probability that a random individual is sampled
     */
    public Mio(IChromosomeFactory chromosomeFactory,
               ISelectionFunction selectionFunction,
               ICrossOverFunction crossOverFunction,
               IMutationFunction mutationFunction,
               List<IFitnessFunction<T>> fitnessFunctions,
               ITerminationCondition terminationCondition,
               int populationSize,
               int generationSurvivorCount,
               float pCrossover,
               float pMutate,
               float pSampleRandom,
               float focusedSearchStart) {

        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction, fitnessFunctions,
                terminationCondition, populationSize, generationSurvivorCount, pCrossover, pMutate);

        this.focusedSearchStart = focusedSearchStart;
        this.pSampleRandom = pSampleRandom;
        this.archive = new HashMap<>();
    }


    @Override
    public void evolve() {
        IChromosome individual = null;
        if (Randomness.getRnd().nextDouble() < pSampleRandom) {
            // Sample Random
            individual = chromosomeFactory.createChromosome();
        } else {
            // Sample individual from archive
            Object[] keys = archive.keySet().toArray();
            Object key = keys[Randomness.getRnd().nextInt(keys.length)];
            LinkedList<IChromosome> individuals = archive.get(key);
            individual = individuals.get(Randomness.getRnd().nextInt(individuals.size()));
            List<IChromosome> mutated = mutationFunction.mutate(individual);
            individual = mutated.get(0); //todo check
        }

        // Fixme this cast?
        for (IFitnessFunction fitnessFunction : (List<IFitnessFunction>) this.fitnessFunctions) {
            if (archive.get(fitnessFunction) == null ) {
                archive.put(fitnessFunction, new LinkedList<IChromosome>());
            }

            // Are my fitness functions the targets I need to cover?
            double fitness = fitnessFunction.getFitness(individual);
            if (fitness == 1) {
                // check population size
                archive.get(fitnessFunction).clear();
                archive.get(fitnessFunction).add(individual);
            } else if (fitness > 0){
                // Todo: Remove worst if we reached limit
                archive.get(fitnessFunction).add(individual);
                if (archive.get(fitnessFunction).size() > populationSize){
                    removeWorstTest(archive.get(fitnessFunction), fitness);
                }
            }
        }

        updateParameters(focusedSearchStart, pSampleRandom, populationSize);
    }

    private void updateParameters(float focusedSearchStart, float pSampleRandom, int populationSize) {
        // We also need to shrink the population at this point

    }

    private void removeWorstTest(List<IChromosome> iChromosomes, double fitness) {
        // In place
        // Is it better "re-measure" the fitnesses or save a tuple in the archive?
    }
}
