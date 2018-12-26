package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Mio<T> extends GeneticAlgorithm<T> {

    private final int populationSizeStart;
    private final long startTime;
    private float pSampleRandom;
    private final float pSampleRandomStart;
    private final float focusedSearchStart;
    private HashMap<IFitnessFunction<T>, List<IndividualFitnessTuple<T>>> archive;

    /**
     * Initializing the genetic algorithm with all necessary attributes
     *
     * @param chromosomeFactory       see {@link IChromosomeFactory}
     * @param selectionFunction       see {@link ISelectionFunction}
     * @param crossOverFunction       see {@link ICrossOverFunction}
     * @param mutationFunction        see {@link IMutationFunction}
     * @param fitnessFunctions        see {@link IFitnessFunction}
     * @param terminationCondition    see {@link ITerminationCondition}
     * @param populationSize          size of population kept by the genetic algorithm
     * @param generationSurvivorCount amount of survivors of each generation
     * @param pCrossover              probability that crossover occurs (between 0 and 1)
     * @param pMutate                 probability that mutation occurs (between 0 and 1)
     * @param pSampleRandom           probability that a random individual is sampled
     * @param focusedSearchStart      percentage as decimal value (e.g 0.8) of time, after which the focused search starts
     */
    public Mio(IChromosomeFactory<T> chromosomeFactory,
               ISelectionFunction<T> selectionFunction,
               ICrossOverFunction<T> crossOverFunction,
               IMutationFunction<T> mutationFunction,
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
        this.startTime = System.currentTimeMillis();
        this.pSampleRandomStart = pSampleRandom;
        this.populationSizeStart = populationSize;
    }


    @Override
    public void evolve() {
        IChromosome<T> individual;
        if (Randomness.getRnd().nextDouble() < pSampleRandom) {
            // Sample Random
            individual = chromosomeFactory.createChromosome();
        } else {
            // Sample individual from archive
            IFitnessFunction<T> key = Randomness.randomElement(new ArrayList<>(archive.keySet()));
            IndividualFitnessTuple<T> tuple = Randomness.randomElement(archive.get(key));
            individual = tuple.getIndividual();
            List<IChromosome<T>> mutated = mutationFunction.mutate(individual);
            individual = mutated.get(0);
        }

        for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
            if (archive.get(fitnessFunction) == null ) {
                archive.put(fitnessFunction, new LinkedList<IndividualFitnessTuple<T>>());
            }

            double fitness = fitnessFunction.getFitness(individual);
            IndividualFitnessTuple<T> tuple = new IndividualFitnessTuple<>(individual, fitness);
            if (fitness == 1) {
                // check population size
                archive.get(fitnessFunction).clear();
                archive.get(fitnessFunction).add(tuple);
            } else if (fitness > 0){
                archive.get(fitnessFunction).add(tuple);
                if (archive.get(fitnessFunction).size() > populationSize){
                    // Remove worst if we reached limit population limit
                    removeWorstTest(archive.get(fitnessFunction));
                }
            }
        }

        updateParameters();
    }

    private void updateParameters() {
        // We also need to shrink the population at this point
        long currentTime = System.currentTimeMillis();
        long expiredTime = (currentTime - startTime);
        long focusedStartAbsolute = (long) (MATE.TIME_OUT * focusedSearchStart);

        if (expiredTime >= focusedStartAbsolute) {
            pSampleRandom = 0;
            populationSize = 1;
        } else {
            float expiredTimePercent = expiredTime / (MATE.TIME_OUT / 100);
            float decreasePercent = expiredTimePercent / (focusedSearchStart * 100);
            populationSize = (int) (populationSizeStart * (1 - decreasePercent));
            pSampleRandom = (int) pSampleRandomStart * (1 - decreasePercent);
        }
   }

    private void removeWorstTest(List<IndividualFitnessTuple<T>> tuples) {
        if (tuples == null) {
            throw new IllegalArgumentException("Cannot remove worst test if list is empty");
        }

        IndividualFitnessTuple worstTuple = tuples.get(0);
        for (IndividualFitnessTuple tuple:  tuples) {
            if (worstTuple.getFitness() > tuple.getFitness()) {
                worstTuple = tuple;
            }
        }

        tuples.remove(worstTuple);
    }

    private class IndividualFitnessTuple<R> {

        private IChromosome<R> individual;
        private double fitness;

        IndividualFitnessTuple(IChromosome<R> individual, double fitness) {
            this.individual = individual;
            this.fitness = fitness;
        }

        IChromosome<R> getIndividual() {
            return individual;
        }

        double getFitness() {
            return fitness;
        }
    }
}
