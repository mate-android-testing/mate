package org.mate.exploration.genetic.fitness;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.utils.FitnessUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This fitness function is only supposed to work a multi/many-objective algorithm
 * like MOSA or MIO.
 */
public class LineCoveredPercentageFitnessFunction<T> implements IFitnessFunction<T> {

    //todo: find better solution than static map... (i know its ugly)
    private static final Map<String, Map<IChromosome, Double>> cache = new HashMap<>();
    private static List<String> lines = new ArrayList<>();
    private final String line;

    public LineCoveredPercentageFitnessFunction(String line) {
        this.line = line;
        lines.add(line);
    }

    @Override
    public double getFitness(IChromosome<T> chromosome) {
        if (!cache.get(line).containsKey(chromosome)) {
            throw new IllegalStateException("Fitness for chromosome " + chromosome + " not in cache. Must fetch fitness previously or performance reasons");
        }
        return cache.get(line).get(chromosome);

    }

    public static <T> void retrieveFitnessValues(IChromosome<T> chromosome) {
        if (lines.size() == 0) {
            return;
        }

        if (cache.size() == 0) {
            for (String line : lines) {
                cache.put(line, new HashMap<IChromosome, Double>());
            }
        }

        MATE.log_acc("retrieving fitness values for chromosome " + chromosome);
        List<Double> coveredPercentage = FitnessUtils.getFitness(chromosome, lines);
        for (int i = 0; i < coveredPercentage.size(); i++) {
            cache.get(lines.get(i)).put(chromosome, coveredPercentage.get(i));
        }
    }

    /**
     * remove chromosome from cache that are no longer in use. (to avoid memory issues)
     */
    public static <T> void cleanCache(List<IChromosome<T>> activeChromosomesAnon) {
        if (lines.size() == 0 || cache.size() == 0) {
            return;
        }

        List<IChromosome<T>> activeChromosomes = new ArrayList<>();
        for (IChromosome<T> chromosome: activeChromosomesAnon) {
            activeChromosomes.add((IChromosome<T>) chromosome);
        }

        int count = 0;
        for (String line : lines) {
            Map<IChromosome, Double> lineCache =  cache.get(line);
            for (IChromosome chromosome: new ArrayList<>(lineCache.keySet())) {
                if (!activeChromosomes.contains(chromosome)) {
                    lineCache.remove(chromosome);
                    count++;
                }
            }
        }
        MATE.log_acc("Cleaning cache: " + count + " inactive chromosome removed");
    }
}
