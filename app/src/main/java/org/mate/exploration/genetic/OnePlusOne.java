package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.ui.UIAbstractionLayer;

import static org.mate.Properties.EVO_ITERATIONS_NUMBER;
import static org.mate.Properties.MAX_NUM_EVENTS;
import static org.mate.Properties.MAX_NUM_TCS;

public class OnePlusOne extends GeneticAlgorithm<TestCase> {
    public OnePlusOne(int populationSize, int generationSurvivorCount, float pMutate, UIAbstractionLayer uiAbstractionLayer, int maxNumEvents, int iterations) {
        super(populationSize, generationSurvivorCount, 0, pMutate);
        MATE.log_acc("Using new One Plus One");
        chromosomeFactory = new AndroidRandomChromosomeFactory(uiAbstractionLayer,maxNumEvents);
        selectionFunction = new FitnessProportionateSelectionFunction<>();
        mutationFunction = new CutPointMutationFunction(uiAbstractionLayer, maxNumEvents);
        fitnessFunction = new AndroidStateFitnessFunction();
        terminationCondition = new IterTerminationCondition(iterations);
    }

    public OnePlusOne(UIAbstractionLayer uiAbstractionLayer) {
        this(MAX_NUM_TCS, 7, 1, uiAbstractionLayer, MAX_NUM_EVENTS, EVO_ITERATIONS_NUMBER);
    }
}
