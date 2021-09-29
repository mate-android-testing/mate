package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of the Many Independent Objective (MIO) Algorithm according to the paper
 * "Many Independent Objective (MIO) Algorithm for Test Suite Generation", see
 * https://arxiv.org/abs/1901.01541 for more details.
 *
 * @param <T> Refers to the type of chromosome. Traditionally, MIO uses {@link TestCase}s as chromosomes.
 */
public class Mio2<T> extends GeneticAlgorithm<T> {

    private Map<IFitnessFunction<T>, List<ChromosomeFitnessTuple>> archive;

    // see paper, variable PR
    private double pSampleRandom;

    // see paper, variable F
    private double focusedSearchStart;

    // maintain the start time to increase/decrease linearly PR and n
    private long startTime;

    // track whether focused search has begun
    private boolean startedFocusedSearch = false;

    // keep the initial values of n and PR
    private final int populationSizeStart;
    private final double pSampleRandomStart;

    // how many mutations should be performed until the next sampling, see variable m
    private int mutations;

    // see variable ck
    private final Map<IFitnessFunction<T>, Integer> samplingCounters;

    /**
     * Initializing the genetic algorithm with all necessary attributes
     *
     * @param chromosomeFactory    see {@link IChromosomeFactory}
     * @param selectionFunction    see {@link ISelectionFunction}
     * @param crossOverFunction    see {@link ICrossOverFunction}
     * @param mutationFunction     see {@link IMutationFunction}
     * @param iFitnessFunctions    see {@link IFitnessFunction}
     * @param terminationCondition see {@link ITerminationCondition}
     * @param populationSize       size of population kept by the genetic algorithm
     * @param bigPopulationSize    size which population will temporarily be after creating offspring
     * @param pCrossover           probability that crossover occurs (between 0 and 1)
     * @param pMutate              probability that mutation occurs (between 0 and 1)
     */
    public Mio2(IChromosomeFactory<T> chromosomeFactory,
                ISelectionFunction<T> selectionFunction,
                ICrossOverFunction<T> crossOverFunction,
                IMutationFunction<T> mutationFunction,
                List<IFitnessFunction<T>> iFitnessFunctions,
                ITerminationCondition terminationCondition,
                int populationSize,
                int bigPopulationSize,
                double pCrossover,
                double pMutate,
                double pSampleRandom,
                double focusedSearchStart) {

        super(chromosomeFactory, selectionFunction, crossOverFunction, mutationFunction,
                iFitnessFunctions, terminationCondition, populationSize, bigPopulationSize,
                pCrossover, pMutate);

        this.pSampleRandom = pSampleRandom;
        this.focusedSearchStart = focusedSearchStart;
        this.pSampleRandomStart = pSampleRandom;
        this.populationSizeStart = populationSize;
        this.samplingCounters = new HashMap<>();
        this.mutations = 1;

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {

            // for each testing target we keep a population of up to size n
            archive.put(fitnessFunction, new LinkedList<ChromosomeFitnessTuple>());

            // initially the sampling counter for each testing target is zero
            samplingCounters.put(fitnessFunction, 0);
        }
    }

    /**
     * MIO initially samples a random chromosome, evaluates its fitness and updates the
     * archive accordingly.
     */
    @Override
    public void createInitialPopulation() {

        this.startTime = System.currentTimeMillis();

        MATE.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");
        IChromosome<T> chromosome = chromosomeFactory.createChromosome();
        population.add(chromosome);

        for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
            double fitness = fitnessFunction.getNormalizedFitness(chromosome);
            addToArchive(fitnessFunction, new ChromosomeFitnessTuple(chromosome, fitness));
        }

        logCurrentFitness();
        currentGenerationNumber++;
    }

    /**
     * Section 3.1: Core Algorithm
     *
     * From the second iteration on, MIO either samples a random chromosome with probability P_R
     * or samples a chromosome from the archive with probability (1 - P_R) and mutates it. Then,
     * the fitness of the chromosome is evaluated (h_k) and the archive is updated.
     *
     * For each target k, the chromosome is saved in the population T_k with |T_k| < n, if either:
     *
     *  - If h_k = 0 (i.e. target unreachable), then the chromosome is not added to any population
     *    regardless of the following conditions.
     *  - If h_k = 1 (i.e. target covered), the chromosome is added to the population and this
     *    population is shrunk to one chromosome and will never expand again. If the population
     *    already contains a chromosome that covers the target, then the new chromosome will only
     *    replace the present chromosome if it is shorter or in case it has the same size, only if
     *    the sum of fitness values over every target other than k is better.
     *  - If the population is not full, i.e. |T_k| < n, then the chromosome is added.
     *  - If the population is full, i.e. |T_k| = n, the chromosome might replace the worst
     *    chromosome in the population, but only if it is not worse in terms of fitness or in case
     *    of equal fitness, only if it is shorter than the worst chromosome.
     *
     * When MIO needs to sample a chromosome from the archive, it picks the chromosome based on
     * the following procedure:
     *
     *  1. It chooses the target k with the lowest sampling counter c_k, see section 3.3, where
     *    T_k is not empty and k is not covered. If all non-empty populations are covered, then
     *    k is chosen randomly among them.
     *  2. It chooses a chromosome from T_k randomly.
     *
     *
     * Section 3.2: Exploration/Exploitation Control
     *
     * MIO makes use of a focused search once a certain amount of the search budget is exhausted.
     * The percentage F defines the time after which the focused search should start. This means
     * that the values of the parameters P_R and n are dynamically adapted: initially they are set
     * to some user-defined value and with the passing of time they decrease linearly. Once the
     * focused search starts P_R is set to 0 and n is set to 1. For example, if P_R = 0.5 and
     * F = 0.5 (i.e., the focused search starts after 50% of the search budget is used), then after
     * 30% of the search, the value P_R is decreased from 0.5 to 0.2.
     *
     * MIO also introduces another parameter m that controls how many mutants should be generated
     * from the same chromosome. This parameter also varies over time, e.g. it starts from 1 and
     * increases up to 10 when the focused search begins.
     *
     *
     * Section 3.3: Feedback-Directed Sampling
     *
     * Every target will be assigned a sampling counter c_k. Initially this counter is 0 for every
     * target. When a chromosome is sampled from the population T_k, c_k is increased by one. If
     * a chromosome is added to T_k or replaces a chromosome in it, c_k is reset to 0. When we
     * sample from the archive, we choose the k with the lowest counter c_k from a population T_k
     * that is not empty nor covered yet. If all populations T_k are covered, we choose k
     * randomly from those covered populations.
     */
    @Override
    public void evolve() {

        MATE.log_acc("Generating population #" + (currentGenerationNumber + 1));
        population.clear();

        if (Randomness.getRnd().nextDouble() < pSampleRandom) {
            // sample random
            population.add(chromosomeFactory.createChromosome());
        } else {
            // sample from archive, pick target with lowest sampling counter
            IFitnessFunction<T> target = getBestTarget();

            // take a random element from the chosen target population
            ChromosomeFitnessTuple tuple = Randomness.randomElement(archive.get(target));
            IChromosome<T> chromosome = tuple.chromosome;

            // increase sampling counter, see section 3.3
            Integer value = samplingCounters.get(target);
            samplingCounters.put(target, value + 1);

            for (int i = 0; i < mutations; i++) {
                List<IChromosome<T>> mutants = mutationFunction.mutate(chromosome);
                population.add(mutants.get(0));
            }
        }

        for (IChromosome<T> chromosome : population) {
            for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {

                // evaluate fitness and update archive
                double fitness = fitnessFunction.getNormalizedFitness(chromosome);
                ChromosomeFitnessTuple tuple = new ChromosomeFitnessTuple(chromosome, fitness);
                updateArchive(fitnessFunction, tuple);
            }
        }

        logCurrentFitness();
        currentGenerationNumber++;

        updateParameters();

        // TODO: Remove! This just tries to keep the cache size reasonable by dropping unused chromosomes.
        List<IChromosome<T>> activeChromosomes = new ArrayList<>();
        for (List<ChromosomeFitnessTuple> tuples : archive.values()) {
            for (ChromosomeFitnessTuple tuple : tuples) {
                activeChromosomes.add(tuple.chromosome);
            }
        }
        FitnessUtils.cleanCache(activeChromosomes);
    }

    // section 3.1
    private void updateArchive(IFitnessFunction<T> fitnessFunction, ChromosomeFitnessTuple tuple) {

        if (isTargetNotReachable(fitnessFunction, tuple)) {
            // h = 0, the chromosome is not added to the archive at all
        } else if (isTargetCovered(fitnessFunction, tuple)) {
            // h = 1, add to archive + shrink
            if (archive.get(fitnessFunction).size() != 1) {
                // no chromosome in the archive that fulfills the testing target, otherwise size == 1
                archive.get(fitnessFunction).clear();
                addToArchive(fitnessFunction, tuple);
            } else {
                ChromosomeFitnessTuple chromosomeFitnessTuple = archive.get(fitnessFunction).get(0);
                if (!isTargetCovered(fitnessFunction, chromosomeFitnessTuple)) {
                    // replace the chromosome
                    archive.get(fitnessFunction).clear();
                    addToArchive(fitnessFunction, tuple);
                } else {
                    // replace only if better
                    if (compareSizeAndOtherTargets(fitnessFunction, tuple, chromosomeFitnessTuple) > 0) {
                        archive.get(fitnessFunction).clear();
                        addToArchive(fitnessFunction, tuple);
                    }
                }
            }
        } else if (!isPopulationFull(fitnessFunction)) {
            addToArchive(fitnessFunction, tuple);
        } else if (isPopulationFull(fitnessFunction)) {
            // replace worst but only if better
            ChromosomeFitnessTuple worst = getWorstInPopulation(fitnessFunction);
            if (compareFitnessAndSize(fitnessFunction, tuple, worst) > 0) {
                archive.get(fitnessFunction).clear();
                addToArchive(fitnessFunction, tuple);
            }
        }
    }

    /**
     * Picks the testing target where the sampling counter has the lowest value. A testing
     * target is only considered if the population size > 0. If all testing targets are covered,
     * we randomly select a target.
     *
     * @return Returns the testing target having the lowest sampling counter for a non empty
     *          population. Picks randomly if all testing targets are covered.
     */
    private IFitnessFunction<T> getBestTarget() {

        Map.Entry<IFitnessFunction<T>, Integer> bestEntry = null;

        for (Map.Entry<IFitnessFunction<T>, Integer> entry : samplingCounters.entrySet()) {

            if (archive.get(entry.getKey()).isEmpty() || isTargetCovered(entry.getKey())) {
                // we can't pick a testing target that has an empty population or is already covered
                continue;
            }

            if (bestEntry == null || bestEntry.getValue() < entry.getValue()) {
                bestEntry = entry;
            }
        }

        if (bestEntry == null) {
            // all non empty testing targets are covered, thus pick one randomly among them
            List<IFitnessFunction<T>> coveredTargets = new ArrayList<>();

            for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
                if (isTargetCovered(fitnessFunction)) {
                    coveredTargets.add(fitnessFunction);
                }
            }

            return Randomness.randomElement(coveredTargets);
        } else {
            return bestEntry.getKey();
        }
    }

    private int compareFitnessAndSize(final IFitnessFunction<T> fitnessFunction,
                                      ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {
        int cmp = compareFitness(fitnessFunction, first, second);
        return cmp != 0 ? cmp : compareSize(first, second);
    }

    private int compareSizeAndOtherTargets(final IFitnessFunction<T> fitnessFunction,
                                           ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {
        int cmp = compareSize(first, second);
        return cmp != 0 ? cmp : compareOtherTargets(fitnessFunction, first, second);
    }

    private int compareOtherTargets(final IFitnessFunction<T> fitnessFunction,
                                    ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {

        double fitnessValueSumFst = 0.0;
        double fitnessValueSumSnd = 0.0;

        for (IFitnessFunction<T> target : fitnessFunctions) {
            if (!target.equals(fitnessFunction)) {
                fitnessValueSumFst += target.getNormalizedFitness(first.chromosome);
                fitnessValueSumSnd += target.getNormalizedFitness(second.chromosome);
            }
        }

        return fitnessFunction.isMaximizing()
                ? Double.compare(fitnessValueSumFst, fitnessValueSumSnd)
                : Double.compare(fitnessValueSumSnd, fitnessValueSumFst);
    }

    private int compareFitness(final IFitnessFunction<T> fitnessFunction,
                        ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {

        Comparator<ChromosomeFitnessTuple> comparator = new Comparator<ChromosomeFitnessTuple>() {
            @Override
            public int compare(ChromosomeFitnessTuple o1, ChromosomeFitnessTuple o2) {
                boolean isMaximising = fitnessFunction.isMaximizing();

                return isMaximising
                        ? Double.compare(o1.fitness, o2.fitness)
                        : Double.compare(o2.fitness, o1.fitness);
            }
        };

        return comparator.compare(first, second);
    }

    private int compareSize(ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {

        Comparator<ChromosomeFitnessTuple> comparator = new Comparator<ChromosomeFitnessTuple>() {
            @Override
            public int compare(ChromosomeFitnessTuple o1, ChromosomeFitnessTuple o2) {

                IChromosome<T> fst = o1.chromosome;
                IChromosome<T> snd = o2.chromosome;

                if (fst.getValue() instanceof TestCase) {
                    return ((TestCase) fst.getValue()).getEventSequence().size()
                            - ((TestCase) snd.getValue()).getEventSequence().size();
                } else if (fst.getValue() instanceof TestSuite) {
                    return ((TestSuite) fst.getValue()).getTestCases().size()
                            - ((TestSuite) snd.getValue()).getTestCases().size();
                } else {
                    throw new IllegalStateException("Chromosome type " + fst.getValue().getClass() + "not yet supported!");
                }
            }
        };

        return comparator.compare(first, second);
    }

    private boolean isTargetCovered(IFitnessFunction<T> fitnessFunction) {
        if (archive.get(fitnessFunction).size() != 1) {
            // only if the population size is 1, the target might be covered
            return false;
        } else {
            ChromosomeFitnessTuple tuple = archive.get(fitnessFunction).get(0);
            return isTargetCovered(fitnessFunction, tuple);
        }
    }

    private boolean isTargetCovered(IFitnessFunction<T> fitnessFunction, ChromosomeFitnessTuple tuple) {
        boolean isMaximising = fitnessFunction.isMaximizing();
        return isMaximising ? tuple.fitness == 1 : tuple.fitness == 0;
    }

    private boolean isTargetNotReachable(IFitnessFunction<T> fitnessFunction, ChromosomeFitnessTuple tuple) {
        boolean isMaximising = fitnessFunction.isMaximizing();
        return isMaximising ? tuple.fitness == 0 : tuple.fitness == 1;
    }

    private boolean isPopulationFull(IFitnessFunction<T> fitnessFunction) {
        return archive.get(fitnessFunction).size() == populationSize;
    }

    private void addToArchive(IFitnessFunction<T> fitnessFunction, ChromosomeFitnessTuple tuple) {

        if (archive.get(fitnessFunction).size() >= populationSize) {
            throw new IllegalStateException("Archive is overcrowded!");
        }

        archive.get(fitnessFunction).add(tuple);

        // reset the sampling counter
        samplingCounters.put(fitnessFunction, 0);
    }

    private ChromosomeFitnessTuple getWorstInPopulation(final IFitnessFunction<T> fitnessFunction) {

        if (archive.get(fitnessFunction).isEmpty()) {
            throw new IllegalStateException("Can't retrieve worst chromosome from empty archive!");
        }

        List<ChromosomeFitnessTuple> population = archive.get(fitnessFunction);
        Collections.sort(population, new Comparator<ChromosomeFitnessTuple>() {
            @Override
            public int compare(ChromosomeFitnessTuple o1, ChromosomeFitnessTuple o2) {
                int cmp = compareFitness(fitnessFunction, o1, o2);
                return cmp != 0 ? cmp : compareSize(o1, o2);
            }
        });

        return population.get(0);
    }

    private void shrinkArchive() {

        for (final IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
            List<ChromosomeFitnessTuple> tuples = archive.get(fitnessFunction);
            if (tuples.size() > populationSize) {

                // we need to discard the worst chromosomes
                int discardAmount = tuples.size() - populationSize;

                Collections.sort(tuples, new Comparator<ChromosomeFitnessTuple>() {
                    @Override
                    public int compare(ChromosomeFitnessTuple o1, ChromosomeFitnessTuple o2) {
                        int cmp = compareFitness(fitnessFunction, o1, o2);
                        return cmp != 0 ? cmp : compareSize(o1, o2);
                    }
                });

                tuples.subList(0, discardAmount).clear();
            }
        }
    }

    private void updateParameters() {

        long currentTime = System.currentTimeMillis();
        long expiredTime = (currentTime - startTime);
        long focusedStartAbsolute = (long) (Registry.getTimeout() * focusedSearchStart);

        if (expiredTime >= focusedStartAbsolute && !startedFocusedSearch) {
            MATE.log_acc("Starting focused search.");
            startedFocusedSearch = true;
            pSampleRandom = 0.0;
            populationSize = 1;
            // TODO: the mutation rate also linearly increases over time
            mutations = 10;
        } else {
            double expiredTimePercent = expiredTime / (Registry.getTimeout() / 100);
            double decreasePercent = expiredTimePercent / (focusedSearchStart * 100);
            populationSize = (int) (populationSizeStart * (1 - decreasePercent));
            pSampleRandom = pSampleRandomStart * (1 - decreasePercent);
        }

        // the population size is decreasing with time, thus we need to discard the worst chromosomes
        shrinkArchive();
    }

    private class ChromosomeFitnessTuple {

        private final IChromosome<T> chromosome;
        private final double fitness;

        public ChromosomeFitnessTuple(IChromosome<T> chromosome, double fitness) {
            this.chromosome = chromosome;
            this.fitness = fitness;
        }

        public IChromosome<T> getChromosome() {
            return chromosome;
        }

        public double getFitness() {
            return fitness;
        }
    }
}
