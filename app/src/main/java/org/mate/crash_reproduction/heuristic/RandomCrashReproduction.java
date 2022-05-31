package org.mate.crash_reproduction.heuristic;

import org.mate.Properties;
import org.mate.crash_reproduction.CrashReproduction;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.model.TestCase;

import java.util.Collections;
import java.util.List;

public class RandomCrashReproduction extends CrashReproduction {
    private final AndroidRandomChromosomeFactory chromosomeFactory = new AndroidRandomChromosomeFactory(Properties.MAX_NUMBER_EVENTS());

    public RandomCrashReproduction(List<IFitnessFunction<TestCase>> fitnessFunctions) {
        super(fitnessFunctions);
    }

    @Override
    protected List<IChromosome<TestCase>> initialPopulation() {
        return Collections.singletonList(chromosomeFactory.createChromosome());
    }

    @Override
    protected List<IChromosome<TestCase>> evolve(List<IChromosome<TestCase>> prevPopulation) {
        return initialPopulation();
    }
}
