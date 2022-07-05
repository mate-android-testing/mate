package org.mate.crash_reproduction.fitness;

import android.util.Pair;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BarrierFitnessFunction<T> implements IFitnessFunction<T> {
    private final List<Pair<Predicate<T>, String>> barriers;

    public BarrierFitnessFunction(List<Pair<Predicate<T>, String>> barriers) {
        this.barriers = barriers;
    }

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        return getNormalizedFitness(chromosome);
    }

    @Override
    public boolean isMaximizing() {
        return false;
    }

    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        double total = barriers.size();
        List<Pair<?, String>> passedPairs = barriers.stream().filter(p -> p.first.test(chromosome.getValue())).collect(Collectors.toList());
        MATE.log("Testcase fitness: [" + passedPairs.stream().map(p -> p.second).collect(Collectors.joining(", ")) + "]");
        double passed = passedPairs.size();
        double missing = total - passed;
        return missing / total;
    }
}
