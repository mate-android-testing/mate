package org.mate.exploration.genetic.fitness;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.utils.FitnessUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a fitness function that aims to maximise a hand-crafted line metric. This fitness function
 * is intended to be used in the context of multi/many-objective search. The AUT needs to be manually
 * instrumented with Jacoco.
 */
public class LineCoveredPercentageFitnessFunction<T> implements IFitnessFunction<T> {

    // TODO: find better solution than static map... (i know its ugly)
    private static final Map<String, Map<IChromosome, Double>> cache = new HashMap<>();

    /**
     * The possible target lines.
     */
    private static List<String> lines = new ArrayList<>();

    /**
     * The line for which the fitness value should be evaluated.
     */
    private final String line;

    /**
     * Initialises the fitness function with the given line as target.
     *
     * @param line The target line.
     */
    public LineCoveredPercentageFitnessFunction(String line) {
        this.line = line;
        lines.add(line);
    }

    /**
     * Retrieves the line metric value for the given chromosome. Note that the value must be
     * already in the cache, i.e. a call to {@link #retrieveFitnessValues(IChromosome)} must
     * precede this call.
     *
     * @param chromosome The chromosome for which we want to retrieve its fitness value.
     * @return Returns the line metric value for the given chromosome.
     */
    @Override
    public double getFitness(IChromosome<T> chromosome) {
        if (!cache.get(line).containsKey(chromosome)) {
            throw new IllegalStateException("Fitness for chromosome " + chromosome
                    + " not in cache. Must fetch fitness previously for performance reasons");
        }
        return cache.get(line).get(chromosome);
    }

    /**
     * Returns whether this fitness function is maximising or not.
     *
     * @return Returns {@code true} since this fitness functions aims to maximise the line metric.
     */
    @Override
    public boolean isMaximizing() {
        return true;
    }

    /**
     * Returns the normalised fitness value for the given chromosome.
     *
     * @param chromosome The chromosome for which the normalised fitness should be evaluated.
     * @return Returns the normalised fitness value bounded in [0,1].
     */
    @Override
    public double getNormalizedFitness(IChromosome<T> chromosome) {
        return getFitness(chromosome);
    }

    /**
     * Retrieves the fitness value for the given chromosome.
     *
     * @param chromosome The chromosome for which the fitness should be evaluated.
     * @param <T> The type wrapped by the chromosome.
     */
    public static <T> void retrieveFitnessValues(IChromosome<T> chromosome) {

        if (lines.size() == 0) {
            return;
        }

        if (cache.size() == 0) {
            for (String line : lines) {
                cache.put(line, new HashMap<>());
            }
        }

        MATE.log_acc("retrieving fitness values for chromosome " + chromosome);
        List<Double> coveredPercentage = FitnessUtils.getFitness(chromosome, lines);
        for (int i = 0; i < coveredPercentage.size(); i++) {
            cache.get(lines.get(i)).put(chromosome, coveredPercentage.get(i));
        }
    }

    /**
     * Removes chromosomes from the cache that are no longer in use in order to avoid memory issues.
     *
     * @param chromosomes The list of active chromosomes.
     */
    public static <T> void cleanCache(List<IChromosome<T>> chromosomes) {

        if (lines.size() == 0 || cache.size() == 0) {
            return;
        }

        List<IChromosome<T>> activeChromosomes = new ArrayList<>(chromosomes);

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
        MATE.log_acc("Cleaning cache: " + count + " inactive chromosome removed.");
    }
}
