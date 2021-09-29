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

        for (IFitnessFunction<T> fitnessFunction : fitnessFunctions) {

            // for each testing target we keep a population of up to size n
            archive.put(fitnessFunction, new LinkedList<ChromosomeFitnessTuple>());

            // initially the sampling counter for each testing target is zero
            samplingCounters.put(fitnessFunction, 0);
        }
    }

    @Override
    public void createInitialPopulation() {

        this.startTime = System.currentTimeMillis();

        MATE.log_acc("Generating initial population!");
        IChromosome<T> chromosome = chromosomeFactory.createChromosome();

        for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
            double fitness = fitnessFunction.getNormalizedFitness(chromosome);
            archive.get(fitnessFunction).add(new ChromosomeFitnessTuple(chromosome, fitness));
        }

        logCurrentFitness();
        currentGenerationNumber++;
    }

    @Override
    public void evolve() {

        MATE.log_acc("Generating population #" + (++currentGenerationNumber));

        IChromosome<T> chromosome;

        if (Randomness.getRnd().nextDouble() < pSampleRandom) {
            // sample random
            chromosome = chromosomeFactory.createChromosome();
        } else {

            // TODO: Allow up to m mutations of chromosome when we run focused search.

            // sample from archive, pick target with lowest sampling counter
            IFitnessFunction<T> target = getBestTarget();

            // take a random element from the chosen target population
            ChromosomeFitnessTuple tuple = Randomness.randomElement(archive.get(target));
            chromosome = tuple.chromosome;

            // increase sampling counter, see section 3.3
            Integer value = samplingCounters.get(target);
            samplingCounters.put(target, value + 1);

            List<IChromosome<T>> mutated = mutationFunction.mutate(chromosome);
            chromosome = mutated.get(0);
        }

        for (IFitnessFunction<T> fitnessFunction : this.fitnessFunctions) {
            double fitness = fitnessFunction.getNormalizedFitness(chromosome);
            ChromosomeFitnessTuple tuple = new ChromosomeFitnessTuple(chromosome, fitness);

            // the new sampled chromosome might be added to one or more sub populations in the archive
            modifyArchive(fitnessFunction, tuple);
        }

        logCurrentFitness();
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

    @Override
    protected void logCurrentFitness() {
        // TODO: log fitness of last accessed chromosomes
    }

    // section 3.1
    private void modifyArchive(IFitnessFunction<T> fitnessFunction, ChromosomeFitnessTuple tuple) {

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
