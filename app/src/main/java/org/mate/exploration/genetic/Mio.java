package org.mate.exploration.genetic;

import org.mate.utils.Randomness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Mio<T> extends GeneticAlgorithm {

    private final float pSampleRandom;
    private final float focusedSearchStart;
    private HashMap<IFitnessFunction, List<IndividualFitnessTuple>> archive;

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
        IChromosome individual;
        if (Randomness.getRnd().nextDouble() < pSampleRandom) {
            // Sample Random
            individual = chromosomeFactory.createChromosome();
        } else {
            // Sample individual from archive
            Object[] keys = archive.keySet().toArray();
            Object key = keys[Randomness.getRnd().nextInt(keys.length)];
            List<IndividualFitnessTuple> individuals = archive.get(key);
            IndividualFitnessTuple tuple = individuals.get(Randomness.getRnd().nextInt(individuals.size()));
            individual = tuple.getIndividual();
            List<IChromosome> mutated = mutationFunction.mutate(individual);
            individual = mutated.get(0); //todo check
        }

        // Fixme this cast?
        for (IFitnessFunction fitnessFunction : (List<IFitnessFunction>) this.fitnessFunctions) {
            if (archive.get(fitnessFunction) == null ) {
                archive.put(fitnessFunction, new LinkedList<IndividualFitnessTuple>());
            }

            // Are my fitness functions the targets I need to cover?
            double fitness = fitnessFunction.getFitness(individual);
            if (fitness == 1) {
                // check population size
                IndividualFitnessTuple tuple = new IndividualFitnessTuple(individual, fitness);

                archive.get(fitnessFunction).clear();
                archive.get(fitnessFunction).add(tuple);
            } else if (fitness > 0){
                // Todo: Remove worst if we reached limit
                IndividualFitnessTuple tuple = new IndividualFitnessTuple(individual, fitness);
                archive.get(fitnessFunction).add(tuple);
                if (archive.get(fitnessFunction).size() > populationSize){
                    List<IndividualFitnessTuple> tuples = removeWorstTest(archive.get(fitnessFunction));
                    archive.put(fitnessFunction, tuples);
                }
            }
        }

        updateParameters(focusedSearchStart, pSampleRandom, populationSize);
    }

    private void updateParameters(float focusedSearchStart, float pSampleRandom, int populationSize) {
        // We also need to shrink the population at this point

    }

    private List<IndividualFitnessTuple> removeWorstTest(List<IndividualFitnessTuple> tuples) {
        // In place
        // Is it better "re-measure" the fitnesses or save a tuple in the archive?
        if (tuples == null) {
            return null;
        }

        IndividualFitnessTuple worstTuple = null;
        for (IndividualFitnessTuple tuple:  tuples) {
            if (worstTuple == null || worstTuple.getFitness() > tuple.getFitness()) {
                worstTuple = tuple;
            }
        }

        tuples.remove(worstTuple);
        return tuples;
    }

    private class IndividualFitnessTuple {

        private IChromosome individual;
        private double fitness;

        public IndividualFitnessTuple(IChromosome individual, double fitness) {
            this.individual = individual;
            this.fitness = fitness;
        }

        public IChromosome getIndividual() {
            return individual;
        }

        public double getFitness() {
            return fitness;
        }
    }
}
