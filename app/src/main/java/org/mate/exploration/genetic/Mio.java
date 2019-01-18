package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.utils.Randomness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Mio<T> extends GeneticAlgorithm<T> {
    public static final String ALGORITHM_NAME = "Mio";

    private final int populationSizeStart;
    private final long startTime;
    private HashMap<IFitnessFunction<T>, List<IndividualFitnessTuple>> populations;
    private double pSampleRandom;
    private final double pSampleRandomStart;
    private final double focusedSearchStart;
    private HashMap<IFitnessFunction<T>, Integer> samplingCounters;
    private LinkedList<IChromosome<T>> archive;

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
               double pCrossover,
               double pMutate,
               double pSampleRandom,
               double focusedSearchStart) {

        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction, fitnessFunctions,
                terminationCondition, populationSize, generationSurvivorCount, pCrossover, pMutate);

        this.focusedSearchStart = focusedSearchStart;
        this.pSampleRandom = pSampleRandom;
        this.populations = new HashMap<>();
        this.samplingCounters = new HashMap<>();
        this.archive = new LinkedList<>();
        this.startTime = System.currentTimeMillis();
        this.pSampleRandomStart = pSampleRandom;
        this.populationSizeStart = populationSize;

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
            samplingCounters.put(fitnessFunction, 0);
        }
    }


    @Override
    public void evolve() {
        IChromosome<T> individual;
        if (Randomness.getRnd().nextDouble() < pSampleRandom) {
            // Sample Random
            individual = chromosomeFactory.createChromosome();
        } else {
            // Sample individual from populations
            IFitnessFunction<T> key = getBestTarget();

            IndividualFitnessTuple tuple = Randomness.randomElement(populations.get(key));
            individual = tuple.getIndividual();

            //Increase Counter
            Integer value = samplingCounters.get(key);
            samplingCounters.put(key, value + 1);

            List<IChromosome<T>> mutated = mutationFunction.mutate(individual);
            individual = mutated.get(0);
        }

        for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
            double fitness = fitnessFunction.getFitness(individual);
            IndividualFitnessTuple tuple = new IndividualFitnessTuple(individual, fitness);

            if (isTargetCovered(fitnessFunction)) {
                // Todo: We should actually check whether the new test could still be better by being
                // shorter.
                continue;
            }

            if (fitness == 1) {
                // TODO: Can we check if one of them is better?
                populations.get(fitnessFunction).clear();
                populations.get(fitnessFunction).add(tuple);
                archive.add(tuple.individual);
            } else if (fitness > 0) {

                IndividualFitnessTuple worstTest = getWorstTest(populations.get(fitnessFunction));
                populations.get(fitnessFunction).add(tuple);

                if (worstTest.fitness < tuple.getFitness()) {
                    //Reset counter
                    samplingCounters.put(fitnessFunction, 0);
                } else {
                    worstTest = tuple;
                }

                if (populations.get(fitnessFunction).size() > populationSize) {
                    // Remove worst if we reached limit population limit
                    populations.get(fitnessFunction).remove(worstTest);
                }
            }
        }

        updateParameters();

        population.clear();
        for (List<IndividualFitnessTuple> individualFitnessTuples : populations.values()) {
            for (IndividualFitnessTuple individualFitnessTuple : individualFitnessTuples) {
                population.add(individualFitnessTuple.getIndividual());
            }
        }
        currentGenerationNumber++;
        logCurrentFitness();
    }

    private boolean isTargetCovered(IFitnessFunction<T> target) {
        if (populations.get(target) == null) {
            throw new IllegalArgumentException("Cannot find population for Target");
        }

        if (populations.get(target).size() != 1) {
            //If we have more or less than one individual we cannot have this covered yet.
            //Covered targets only retain one individual
            return false;
        }

        return populations.get(target).get(0).fitness == 1;
    }

    @Override
    public void createInitialPopulation() {
        super.createInitialPopulation();
        for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
            if (populations.get(fitnessFunction) == null) {
                populations.put(fitnessFunction, new LinkedList<IndividualFitnessTuple>());
            }

            for (IChromosome<T> individual : this.population) {
                IndividualFitnessTuple tuple = new IndividualFitnessTuple(individual, fitnessFunction.getFitness(individual));
                populations.get(fitnessFunction).add(tuple);

            }
        }
    }

    private void updateParameters() {
        // We also need to shrink the population at this point
        long currentTime = System.currentTimeMillis();
        long expiredTime = (currentTime - startTime);
        long focusedStartAbsolute = (long) (MATE.TIME_OUT * focusedSearchStart);

        if (expiredTime >= focusedStartAbsolute) {
            MATE.log_acc("Starting focused search.");
            pSampleRandom = 0;
            populationSize = 1;
        } else {
            double expiredTimePercent = expiredTime / (MATE.TIME_OUT / 100);
            double decreasePercent = expiredTimePercent / (focusedSearchStart * 100);
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
        for (Map.Entry<IFitnessFunction<T>, Integer> entry : samplingCounters.entrySet()) {
            if (bestEntry == null || (bestEntry.getValue() > entry.getValue() && !isTargetCovered(entry.getKey()))) {
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
