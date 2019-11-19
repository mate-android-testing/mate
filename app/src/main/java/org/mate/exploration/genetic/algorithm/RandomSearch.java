package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.selection.IdSelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;

import java.util.ArrayList;
import java.util.List;

public class RandomSearch<T> extends GeneticAlgorithm<T> {

    public static final String ALGORITHM_NAME = "RandomSearch";

    public RandomSearch(IChromosomeFactory<T> chromosomeFactory, List<IFitnessFunction<T>> fitnessFunctions, ITerminationCondition terminationCondition) {
        super(chromosomeFactory, null, null,null, fitnessFunctions, terminationCondition, 1, 2, 0, 0);
    }

    @Override
    public void evolve() {

        MATE.log_acc("Creating population #" + (currentGenerationNumber + 1));

        // add temporary a second random chromosome
        population.add(chromosomeFactory.createChromosome());

        // Discard old chromosome if not better than new one.
        double compared = fitnessFunctions.get(0).getFitness(population.get(0))
                - fitnessFunctions.get(0).getFitness(population.get(1));
        if (compared > 0) {
            population.remove(1);
        } else {
            population.remove(0);
        }
    }

}
