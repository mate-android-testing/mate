package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.utils.Coverage;

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

        logCurrentFitness();

        if (compared > 0) {
            population.remove(1);
        } else {
            population.remove(0);
        }

        currentGenerationNumber++;
    }

    @Override
    protected void logCurrentFitness() {

        for (int i = 0; i < Math.min(fitnessFunctions.size(), 5); i++) {
            MATE.log_acc("Fitness function " + (i + 1) + ":");
            IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(i);
            for (int j = 0; j < population.size(); j++) {
                IChromosome<T> chromosome = population.get(j);
                MATE.log_acc("Chromosome " + (j + 1) + " Fitness: "
                        + fitnessFunction.getFitness(chromosome));

                if (Properties.COVERAGE() != Coverage.NO_COVERAGE
                        && Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {
                    MATE.log_acc("Chromosome " + (j + 1) + " Coverage: "
                            + Registry.getEnvironmentManager().getCoverage(Properties.COVERAGE(),
                            chromosome.toString()));
                }
            }
        }

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE
                && Properties.COVERAGE() != Coverage.ACTIVITY_COVERAGE) {
            MATE.log_acc("Accumulated Coverage: "
                    + Registry.getEnvironmentManager().getCombinedCoverage(Properties.COVERAGE()));
        }
    }

}
