package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;

import java.util.List;

public class OnePlusOne<T> extends GeneticAlgorithm<T> {

    public OnePlusOne(IChromosomeFactory<T> chromosomeFactory,
                      ISelectionFunction<T> selectionFunction,
                      ICrossOverFunction<T> crossOverFunction,
                      IMutationFunction<T> mutationFunction,
                      List<IFitnessFunction<T>> fitnessFunctions,
                      ITerminationCondition terminationCondition) {
        super(chromosomeFactory, selectionFunction, null, mutationFunction,
                fitnessFunctions, terminationCondition, 1, 2,
                0, 1);
    }

    @Override
    public void evolve() {
        MATE.log_acc("Evolving into generation: " + (currentGenerationNumber + 1));

        // Temporarily allow two chromosomes in the population.
        populationSize++;

        // Add offspring to population.
        super.evolve();

        // Discard old chromosome if not better than new one.
        IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);
        double compared = fitnessFunction.getNormalizedFitness(population.get(0))
                - fitnessFunction.getNormalizedFitness(population.get(1));

        logCurrentFitness();

        if (fitnessFunction.isMaximizing()) {
            population.remove(compared > 0 ? 1 : 0);
        } else {
            population.remove(compared < 0 ? 1 : 0);
        }

        // Revert population size back to normal;
        populationSize--;
    }


}
