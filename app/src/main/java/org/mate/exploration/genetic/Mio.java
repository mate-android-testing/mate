package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.utils.Randomness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Mio<T> extends GeneticAlgorithm<T> {

    private final int populationSizeStart;
    private final long startTime;
    private float pSampleRandom;
    private final float pSampleRandomStart;
    private final float focusedSearchStart;
    private HashMap<IFitnessFunction<T>, List<IndividualFitnessTuple>> archive;
    private HashMap<IFitnessFunction<T>, Integer> counters;

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
        this.counters = new HashMap<>();
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
            IFitnessFunction<T> key = getBestTarget();

            IndividualFitnessTuple tuple = Randomness.randomElement(archive.get(key));
            individual = tuple.getIndividual();

            //Increase Counter
            if (counters.containsKey(key)) {
                Integer value = counters.get(key);
                counters.put(key, value + 1);
            } else {
                counters.put(key, 0);
            }

            List<IChromosome<T>> mutated = mutationFunction.mutate(individual);
            individual = mutated.get(0);
        }

        for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
            if (archive.get(fitnessFunction) == null) {
                archive.put(fitnessFunction, new LinkedList<IndividualFitnessTuple>());
            }

            double fitness = fitnessFunction.getFitness(individual);
            IndividualFitnessTuple tuple = new IndividualFitnessTuple(individual, fitness);
            if (fitness == 1) {
                // check population size
                archive.get(fitnessFunction).clear();
                archive.get(fitnessFunction).add(tuple);
            } else if (fitness > 0) {

                IndividualFitnessTuple worstTest = getWorstTest(archive.get(fitnessFunction));
                archive.get(fitnessFunction).add(tuple);

                if (worstTest.fitness < tuple.getFitness()) {
                    //Reset counter
                    counters.put(fitnessFunction, 0);
                } else {
                    worstTest = tuple;
                }

                if (archive.get(fitnessFunction).size() > populationSize) {
                    // Remove worst if we reached limit population limit
                    archive.get(fitnessFunction).remove(worstTest);
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

    private IndividualFitnessTuple getWorstTest(List<IndividualFitnessTuple> tuples) {
        if (tuples == null || tuples.isEmpty()) {
            throw new IllegalArgumentException("Cannot find worst test if list is empty");
        }

        IndividualFitnessTuple worstTuple = tuples.get(0);
        for (IndividualFitnessTuple tuple : tuples) {
            if (worstTuple.getFitness() > tuple.getFitness()) {
                worstTuple = tuple;
            }
        }

        return worstTuple;
    }

    private IFitnessFunction<T> getBestTarget() {
        Map.Entry<IFitnessFunction<T>, Integer> bestEntry = null;
        for (Map.Entry<IFitnessFunction<T>, Integer> entry : counters.entrySet()) {
            if (bestEntry == null || bestEntry.getValue() > entry.getValue()) {
                bestEntry = entry;
            }
        }

        return bestEntry.getKey();
    }

    private class IndividualFitnessTuple {

        private IChromosome<T> individual;
        private double fitness;

        IndividualFitnessTuple(IChromosome<T> individual, double fitness) {
            this.individual = individual;
            this.fitness = fitness;
        }

        IChromosome<T> getIndividual() {
            return individual;
        }

        double getFitness() {
            return fitness;
        }
    }
}
