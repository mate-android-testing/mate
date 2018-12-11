package org.mate.exploration.genetic;

import org.mate.MATE;

import java.util.List;

public class OnePlusOne<T> extends GeneticAlgorithm<T> {
    public static final String ALGORITHM_NAME = "OnePlusOne";

    public OnePlusOne(IChromosomeFactory<T> chromosomeFactory,
                      ISelectionFunction<T> selectionFunction,
                      ICrossOverFunction<T> crossOverFunction,
                      IMutationFunction<T> mutationFunction,
                      List<IFitnessFunction<T>> fitnessFunctions,
                      ITerminationCondition terminationCondition) {
        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction,
                fitnessFunctions, terminationCondition, 1, 1,
                0, 1);
    }

    @Override
    public void evolve() {
        MATE.log_acc("Evolving into generation: " + (currentGenerationNumber + 1));
        // Temporarily allow two chromosomes in the population.
        populationSize++;

        // Add offspring to population
        super.evolve();

        // Discard old chromosome if not better than new one.
        double compared = fitnessFunctions.get(0).getFitness(population.get(0))
                          - fitnessFunctions.get(0).getFitness(population.get(1));
        if (compared > 0) {
            population.remove(1);
        } else {
            population.remove(0);
        }

        // Revert population size back to normal;
        populationSize--;
    }
}
