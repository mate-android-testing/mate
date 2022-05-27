package org.mate.exploration.genetic.algorithm;

import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Randomness;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;
import org.mate.utils.FitnessUtils;

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
public class MIO<T> extends GeneticAlgorithm<T> {

    /**
     * The sampling probability P_r during focused search.
     */
    private final double pSampleRandomFocusedSearch = 0.0;

    /**
     * The population size n during focused search.
     */
    private final int populationSizeFocusedSearch = 1;

    /**
     * The mutation rate m during focused search.
     */
    private final int mutationRateFocusedSearch = 10;

    /**
     * The archive maintains for each target k a population T_k of size up to n.
     */
    private final Map<IFitnessFunction<T>, List<ChromosomeFitnessTuple>> archive;

    /**
     * Represents the initial probability P_r for sampling a random chromosome.
     */
    private final double pSampleRandomStart;

    /**
     * Represents the initial population size n.
     */
    private final int populationSizeStart;

    /**
     * Represents the initial mutation rate m.
     */
    private final int mutationRateStart;

    /**
     * Represents the current probability P_r for sampling a random chromosome.
     */
    private double pSampleRandom;

    /**
     * Represents the current population size n.
     */
    private int populationSize;

    /**
     * Represents the current mutation rate m.
     */
    private int mutationRate;

    /**
     * Represents the percentage F that defines after which time the focused search should start.
     * For example, F = 0.5 means that the focused search should start after 50% of the search
     * budget is exhausted.
     */
    private final double focusedSearchStart;

    // represents the variable c_k for each target k
    private final Map<IFitnessFunction<T>, Integer> samplingCounters;

    // tracks the start point of the search to measure when the focused search should start
    private long startTime;

    // whether the focused search phase has been started yet
    private boolean startedFocusedSearch = false;

    /**
     * Initializes MIO with the relevant attributes.
     *
     * @param chromosomeFactory The used chromosome factory, see {@link IChromosomeFactory}.
     * @param mutationFunction The used mutation function, see {@link IMutationFunction}.
     * @param fitnessFunctions The used fitness function, see {@link IFitnessFunction}.
     * @param terminationCondition The used termination condition, see {@link ITerminationCondition}.
     * @param populationSize The population size n.
     * @param bigPopulationSize The big population size, unused here.
     * @param pCrossover The probability for crossover, unused here.
     * @param pMutate The probability for mutation.
     * @param mutationRate The mutation rate m.
     * @param focusedSearchStart The percentage F.
     * @param pSampleRandom The sampling probability P_r.
     */
    public MIO(IChromosomeFactory<T> chromosomeFactory,
               IMutationFunction<T> mutationFunction,
               List<IFitnessFunction<T>> fitnessFunctions,
               ITerminationCondition terminationCondition,
               int populationSize,
               int bigPopulationSize,
               double pCrossover,
               double pMutate,
               double pSampleRandom,
               double focusedSearchStart,
               int mutationRate) {

        super(chromosomeFactory,
                null,
                null,
                mutationFunction,
                fitnessFunctions,
                terminationCondition,
                populationSize,
                bigPopulationSize,
                pCrossover,
                pMutate);

        this.archive = new HashMap<>(); // (k -> T_k)
        this.samplingCounters = new HashMap<>(); // (k -> c_k)

        this.pSampleRandom = pSampleRandom; // P_r
        this.populationSize = populationSize; // n
        this.mutationRate = mutationRate; // m
        this.focusedSearchStart = focusedSearchStart; // F

        // the start values for P_r, n and m
        this.pSampleRandomStart = pSampleRandom;
        this.populationSizeStart = populationSize;
        this.mutationRateStart = mutationRate;

        MATELog.log_acc("Initial Parameters: ");
        MATELog.log_acc("Random sampling probability P_r: " + pSampleRandomStart);
        MATELog.log_acc("Population size n: " + populationSizeStart);
        MATELog.log_acc("Mutation rate m: " + mutationRateStart);

        for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {

            // for each testing target k we keep a population T_k of up to size n
            archive.put(fitnessFunction, new LinkedList<>());

            // initially the sampling counter c_k for each testing target k is zero
            samplingCounters.put(fitnessFunction, 0);
        }
    }

    /**
     * MIO initially samples a random chromosome, evaluates its fitness and updates the archive
     * accordingly.
     */
    @Override
    public void createInitialPopulation() {

        this.startTime = System.currentTimeMillis();

        MATELog.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");
        IChromosome<T> chromosome = chromosomeFactory.createChromosome();
        population.add(chromosome);

        for (IFitnessFunction<T> target : this.fitnessFunctions) {
            double fitness = target.getNormalizedFitness(chromosome);
            addToArchive(target, new ChromosomeFitnessTuple(chromosome, fitness));
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

        MATELog.log_acc("Generating population #" + (currentGenerationNumber + 1));
        population.clear();

        if (Randomness.getRnd().nextDouble() < pSampleRandom) {
            // sample random chromosome with probability P_r
            IChromosome<T> chromosome = chromosomeFactory.createChromosome();
            population.add(chromosome);
            MATELog.log_acc("Sampled random chromosome " + chromosome + "!");
        } else {
            /*
            * Sample a chromosome from the archive with probability (1 - P_r). Pick a target k with
            * the lowest sampling counter c_k. Then select randomly a chromosome from the
            * population T_k in the archive.
             */
            IFitnessFunction<T> target = getBestTarget();
            ChromosomeFitnessTuple tuple = Randomness.randomElement(archive.get(target));
            IChromosome<T> chromosome = tuple.chromosome;
            MATELog.log_acc("Sampled chromosome " + chromosome + " from archive!");

            // increase sampling counter c_k, see section 3.3
            Integer value = samplingCounters.get(target);
            samplingCounters.put(target, value + 1);

            // sample up to m mutants from the same base chromosome
            for (int i = 0; i < mutationRate; i++) {
                IChromosome<T> mutant = mutationFunction.mutate(chromosome);
                population.add(mutant);
                MATELog.log_acc("Sampled mutant " + mutant + "!");
            }
        }

        MATELog.log_acc("Updating Archive...");

        // evaluate fitness and update archive
        for (IChromosome<T> chromosome : population) {
            for (IFitnessFunction<T> target : this.fitnessFunctions) {
                double fitness = target.getNormalizedFitness(chromosome);
                updateArchive(target, new ChromosomeFitnessTuple(chromosome, fitness));
            }
        }

        logCurrentFitness();
        currentGenerationNumber++;

        /*
        * The parameters P_r, n and m linearly increase/decrease over time until the focused
        * search is started.
         */
        if (!startedFocusedSearch) {
            updateParameters();
        }

        // clean the cache from time to time, otherwise we end up with an OOM error for large apps
        List<IChromosome<T>> activeChromosomes = new ArrayList<>();
        for (List<ChromosomeFitnessTuple> tuples : archive.values()) {
            for (ChromosomeFitnessTuple tuple : tuples) {
                activeChromosomes.add(tuple.chromosome);
            }
        }
        FitnessUtils.cleanCache(activeChromosomes);
    }

    /**
     * Updates the archive according to the rules described in section 3.1.
     *
     * @param target The target k.
     * @param chromosome The new chromosome that might be added to the archive or that might replace
     *              another chromosome in the archive.
     */
    private void updateArchive(IFitnessFunction<T> target, ChromosomeFitnessTuple chromosome) {

        if (isTargetNotReachable(target, chromosome)) {
            // the target is unreachable for the chromosome, ignore it
        } else if (isTargetCovered(target, chromosome)) {
            // the chromosome covers the target, insert it if better
            if (archive.get(target).isEmpty() || archive.get(target).size() > 1) {
                /*
                * No chromosome in the current population T_k covers the target, thus replace
                * the current population with the new chromosome. Note, the population T_k will
                * never expand again, i.e. |T_k| = 1 from now on.
                 */
                replaceAllInArchive(target, chromosome);
            } else {
                /*
                * Check whether the single chromosome in the population T_k covers the target k.
                * If this is not the case, we can simply replace the chromosome with the new one.
                * Otherwise, we only replace the chromosome if the new chromosome is better in terms
                * of size (shorter). If the size is identical, the chromosomes are compared on the
                * remaining targets other than k.
                 */
                ChromosomeFitnessTuple oldChromosome = archive.get(target).get(0);
                if (!isTargetCovered(target, oldChromosome)) {
                    replaceInArchive(target, chromosome, oldChromosome);
                } else {
                    if (compareSizeAndOtherTargets(target, chromosome, oldChromosome) > 0) {
                        replaceInArchive(target, chromosome, oldChromosome);
                    }
                }
            }
        } else if (!isTargetPopulationFull(target)) {
            // as long as the target population T_k is not full, i.e. |T_k| < n, we add it
            addToArchive(target, chromosome);
        } else if (isTargetPopulationFull(target)) {
            // replace with worst chromosome in T_k but only if better in terms of fitness and size
            ChromosomeFitnessTuple worstChromosome = getWorstChromosomeOfTargetPopulation(target);
            if (compareFitnessAndSize(target, chromosome, worstChromosome) > 0) {
                replaceInArchive(target, chromosome, worstChromosome);
            }
        }
    }

    /**
     * Checks whether the given target is covered, i.e. there exits a single chromosome in the
     * target population T_k that fulfills the target k.
     *
     * @param target The target k.
     * @return Returns {@code true} if the target k is covered, otherwise {@code false} is returned.
     */
    private boolean isTargetCovered(IFitnessFunction<T> target) {
        if (archive.get(target).size() != 1) {
            /*
            * By construction, the target k is only covered if the population T_k contains
            * a single chromosome, since the population never expands once a chromosome covers
            * the target.
             */
            return false;
        } else {
            // check whether the single chromosome covers the target k
            ChromosomeFitnessTuple tuple = archive.get(target).get(0);
            return isTargetCovered(target, tuple);
        }
    }

    /**
     * Checks whether the given chromosome covers the given target.
     *
     * @param target The target k.
     * @param chromosome The chromosome to be checked.
     * @return Returns {@code true} if the given chromosome covers the given target k,
     *          otherwise {@code false} is returned.
     */
    private boolean isTargetCovered(IFitnessFunction<T> target, ChromosomeFitnessTuple chromosome) {
        boolean isMaximising = target.isMaximizing();
        return isMaximising ? chromosome.fitness == 1 : chromosome.fitness == 0;
    }

    /**
     * Checks whether the given chromosome can't cover the given target k.
     *
     * @param target The target k.
     * @param chromosome The chromosome to be checked.
     * @return Returns {@code true} if the given chromosome does not cover the given target k,
     *          otherwise {@code false} is returned.
     */
    private boolean isTargetNotReachable(IFitnessFunction<T> target, ChromosomeFitnessTuple chromosome) {
        boolean isMaximising = target.isMaximizing();
        return isMaximising ? chromosome.fitness == 0 : chromosome.fitness == 1;
    }

    /**
     * Checks whether the target population T_k is full, i.e. |T_k| >= n.
     *
     * @param target The target k.
     * @return Returns {@code true} if the target population T_k is full.
     */
    private boolean isTargetPopulationFull(IFitnessFunction<T> target) {
        return archive.get(target).size() >= populationSize;
    }

    /**
     * Adds the given chromosome to the given target population T_k.
     *
     * @param target The target k.
     * @param chromosome The chromosome to be added.
     */
    private void addToArchive(IFitnessFunction<T> target, ChromosomeFitnessTuple chromosome) {

        if (archive.get(target).size() >= populationSize) {
            throw new IllegalStateException("Population T_k of is full, can't store chromosome!");
        }

        archive.get(target).add(chromosome);

        // reset the sampling counter c_k for target k
        samplingCounters.put(target, 0);
    }

    /**
     * Replaces the old chromosome with the new chromosome in the target population T_k.
     *
     * @param target The target k.
     * @param newChromosome The new chromosome.
     * @param oldChromosome The old chromosome.
     */
    private void replaceInArchive(IFitnessFunction<T> target, ChromosomeFitnessTuple newChromosome,
                                  ChromosomeFitnessTuple oldChromosome) {
        archive.get(target).remove(oldChromosome);
        addToArchive(target, newChromosome);
    }

    /**
     * Replaces all chromosomes in the target population T_k with the given chromosome.
     *
     * @param target The target k.
     * @param chromosome The new chromosome.
     */
    private void replaceAllInArchive(IFitnessFunction<T> target, ChromosomeFitnessTuple chromosome) {
        archive.get(target).clear();
        addToArchive(target, chromosome);
    }

    /**
     * Retrieves the worst chromosome from the target population T_k, i.e. the chromosome with
     * the worst fitness value and the worst size.
     *
     * @param target The target k.
     * @return Returns the worst chromosome of the target population T_k.
     */
    private ChromosomeFitnessTuple getWorstChromosomeOfTargetPopulation(final IFitnessFunction<T> target) {

        if (archive.get(target).isEmpty()) {
            throw new IllegalStateException("Can't retrieve worst chromosome from empty archive!");
        }

        List<ChromosomeFitnessTuple> population = archive.get(target);
        Collections.sort(population, (o1, o2) -> {
            int cmp = compareFitness(target, o1, o2);
            return cmp != 0 ? cmp : compareSize(o1, o2);
        });

        // the worst chromosome comes first
        return population.get(0);
    }

    /**
     * Shrinks the archive. This is necessary once the parameters are updated, in particular the
     * parameter n. It could happen that certain populations T_k violate the rule |T_k| <= n
     * since n is linearly decreasing with the passing of time.
     */
    private void shrinkArchive() {

        MATELog.log_acc("Shrinking Archive...");

        for (final IFitnessFunction<T> target : fitnessFunctions) {
            List<ChromosomeFitnessTuple> population = archive.get(target);
            if (population.size() > populationSize) {

                // we need to discard the worst chromosomes
                int discardAmount = population.size() - populationSize;

                Collections.sort(population, (o1, o2) -> {
                    int cmp = compareFitness(target, o1, o2);
                    return cmp != 0 ? cmp : compareSize(o1, o2);
                });

                population.subList(0, discardAmount).clear();
            }
        }
    }

    /**
     * Updates the parameters P_r, n and m. Those parameters linearly increase/decrease with the
     * passing of time until the focused search is started. See section 3.2. for more details.
     */
    private void updateParameters() {

        MATELog.log_acc("Updating Parameters...");

        long currentTime = System.currentTimeMillis();
        long expiredTime = currentTime - startTime;
        long focusedSearchStartTime = (long) (Registry.getTimeout() * focusedSearchStart);

        if (expiredTime >= focusedSearchStartTime) {
            MATELog.log_acc("Starting focused search...");
            startedFocusedSearch = true;
            pSampleRandom = pSampleRandomFocusedSearch;
            populationSize = populationSizeFocusedSearch;
            mutationRate = mutationRateFocusedSearch;
        } else {
            float focusedSearchStartProgress = (float) expiredTime / focusedSearchStartTime;

            populationSize = populationSizeStart
                    + Math.round((populationSizeFocusedSearch - populationSizeStart) * focusedSearchStartProgress);

            pSampleRandom = pSampleRandomStart
                    + (pSampleRandomFocusedSearch - pSampleRandomStart) * focusedSearchStartProgress;

            mutationRate = mutationRateStart
                    + Math.round((mutationRateFocusedSearch - mutationRateStart) * focusedSearchStartProgress);
        }

        MATELog.log_acc("New population size n: " + populationSize);
        MATELog.log_acc("New random sampling rate P_r: " + pSampleRandom);
        MATELog.log_acc("New mutation rate m: " + mutationRate);

        // the population size is decreasing with time, thus we need to discard the worst chromosomes
        shrinkArchive();
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
            MATELog.log_acc("All testing targets covered, picking random target k.");
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

    /**
     * Compares the two chromosomes based on its fitness and size.
     *
     * @param target The target k.
     * @param first The first chromosome.
     * @param second The second chromosome.
     * @return Returns a comparison value that indicates the 'ordering' of the two chromosomes.
     */
    private int compareFitnessAndSize(final IFitnessFunction<T> target,
                                      ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {
        int cmp = compareFitness(target, first, second);
        return cmp != 0 ? cmp : compareSize(first, second);
    }

    /**
     * Compares the two chromosomes based on its size and the targets other than k.
     *
     * @param target The target k.
     * @param first The first chromosome.
     * @param second The second chromosome.
     * @return Returns a comparison value that indicates the 'ordering' of the two chromosomes.
     */
    private int compareSizeAndOtherTargets(final IFitnessFunction<T> target,
                                           ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {
        int cmp = compareSize(first, second);
        return cmp != 0 ? cmp : compareOtherTargets(target, first, second);
    }

    /**
     * Compares the two chromosomes based on the fitness values of every target other than k.
     *
     * @param target The target k.
     * @param first The first chromosome.
     * @param second The second chromosome.
     * @return Returns a comparison value that indicates the 'ordering' of the two chromosomes.
     */
    private int compareOtherTargets(final IFitnessFunction<T> target,
                                    ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {

        double fitnessValueSumFst = 0.0;
        double fitnessValueSumSnd = 0.0;

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {
            if (!fitnessFunction.equals(target)) {
                fitnessValueSumFst += target.getNormalizedFitness(first.chromosome);
                fitnessValueSumSnd += target.getNormalizedFitness(second.chromosome);
            }
        }

        return target.isMaximizing()
                // a higher fitness value is better
                ? Double.compare(fitnessValueSumFst, fitnessValueSumSnd)
                // a lower fitness value is better
                : Double.compare(fitnessValueSumSnd, fitnessValueSumFst);
    }

    /**
     * Compares the two chromosomes based on its fitness. Regards whether the fitness function
     * is maximising or minimising.
     *
     * @param target The target k.
     * @param first The first chromosome.
     * @param second The second chromosome.
     * @return Returns a comparison value that indicates the 'ordering' of the two chromosomes.
     */
    private int compareFitness(final IFitnessFunction<T> target,
                        ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {

        Comparator<ChromosomeFitnessTuple> comparator = (o1, o2) -> {
            boolean isMaximising = target.isMaximizing();

            return isMaximising
                    ? Double.compare(o1.fitness, o2.fitness)
                    : Double.compare(o2.fitness, o1.fitness);
        };

        return comparator.compare(first, second);
    }

    /**
     * Compares the two chromosomes based on its size. A shorter chromosome is considered better.
     * This is in fact the reversed natural ordering of integers.
     *
     * @param first The first chromosome.
     * @param second The second chromosome.
     * @return Returns a comparison value that indicates the 'ordering' of the two chromosomes.
     */
    private int compareSize(ChromosomeFitnessTuple first, ChromosomeFitnessTuple second) {

        Comparator<ChromosomeFitnessTuple> comparator = (o1, o2) -> {

            IChromosome<T> fst = o1.chromosome;
            IChromosome<T> snd = o2.chromosome;

            if (fst.getValue() instanceof TestCase) {
                return ((TestCase) snd.getValue()).getActionSequence().size()
                        - ((TestCase) fst.getValue()).getActionSequence().size();
            } else if (fst.getValue() instanceof TestSuite) {
                return ((TestSuite) snd.getValue()).getTestCases().size()
                        - ((TestSuite) fst.getValue()).getTestCases().size();
            } else {
                throw new IllegalStateException("Chromosome type " + fst.getValue().getClass()
                        + "not yet supported!");
            }
        };

        return comparator.compare(first, second);
    }

    /**
     * Pairs a chromosome with its (normalised) fitness value.
     */
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

        @Override
        public String toString() {

            int size = -1;

            if (chromosome.getValue() instanceof TestCase) {
                size = ((TestCase) chromosome.getValue()).getActionSequence().size();
            } else if (chromosome.getValue() instanceof TestSuite) {
                size = ((TestSuite) chromosome.getValue()).getTestCases().size();
            }

            return "ChromosomeFitnessTuple{" +
                    "chromosome=" + chromosome +
                    ", fitness=" + fitness +
                    ", size=" + size +
                    '}';
        }
    }

    @SuppressWarnings("debug")
    private void debugArchive() {

        MATELog.log_debug("Archive: ");
        int i = 0;
        for (List<ChromosomeFitnessTuple> population: archive.values()) {
            if (!population.isEmpty()) {
                MATELog.log_debug("Population: " + i);
                for (ChromosomeFitnessTuple tuple : population) {
                    MATELog.log_debug(tuple.toString());
                }
            }
            i++;
        }
    }

    @SuppressWarnings("debug")
    private void debugPopulation(List<ChromosomeFitnessTuple> population) {

        MATELog.log_debug("Population: ");
        for (ChromosomeFitnessTuple tuple : population) {
            MATELog.log_debug(tuple.toString());
        }
    }
}
