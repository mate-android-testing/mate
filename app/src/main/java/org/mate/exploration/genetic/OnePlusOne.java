package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.ui.UIAbstractionLayer;

import java.util.LinkedList;

import static org.mate.Properties.EVO_ITERATIONS_NUMBER;
import static org.mate.Properties.MAX_NUM_EVENTS;

public class OnePlusOne extends GeneticAlgorithm<TestCase> {
    public OnePlusOne(UIAbstractionLayer uiAbstractionLayer, int maxNumEvents, int iterations) {
        super(1, 1, 0, 1);
        MATE.log_acc("Starting new One Plus One");
        chromosomeFactory = new AndroidRandomChromosomeFactory(uiAbstractionLayer, maxNumEvents);
        selectionFunction = new FitnessSelectionFunction<>();
        mutationFunction = new CutPointMutationFunction(uiAbstractionLayer, maxNumEvents);
        fitnessFunctions = new LinkedList<>();
        fitnessFunctions.add(new AndroidStateFitnessFunction());
        terminationCondition = new IterTerminationCondition(iterations);
    }

    public OnePlusOne(UIAbstractionLayer uiAbstractionLayer) {
        this(uiAbstractionLayer, MAX_NUM_EVENTS, EVO_ITERATIONS_NUMBER);
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
        if (!maximizeFitness) {
            compared = -compared;
        }
        if (compared > 0) {
            population.remove(1);
        } else {
            population.remove(0);
        }

        // Revert population size back to normal;
        populationSize--;
    }
}
