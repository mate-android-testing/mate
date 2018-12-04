package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.ui.UIAbstractionLayer;

import java.util.LinkedList;
import java.util.List;

import static org.mate.Properties.EVO_ITERATIONS_NUMBER;
import static org.mate.Properties.MAX_NUM_EVENTS;

public class OnePlusOne extends GeneticAlgorithm<TestCase> {
    public OnePlusOne(IChromosomeFactory<TestCase> chromosomeFactory,
                      ISelectionFunction<TestCase> selectionFunction,
                      ICrossOverFunction<TestCase> crossOverFunction,
                      IMutationFunction<TestCase> mutationFunction,
                      List<IFitnessFunction<TestCase>> fitnessFunctions,
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
