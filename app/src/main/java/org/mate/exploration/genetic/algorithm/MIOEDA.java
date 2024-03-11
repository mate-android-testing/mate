package org.mate.exploration.genetic.algorithm;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.MIOEDAChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.fitness.ActionFitnessFunctionWrapper;
import org.mate.exploration.genetic.fitness.GenotypePhenotypeMappedFitnessFunction;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;
import org.mate.model.TestCase;
import org.mate.utils.FitnessFunctionState;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageDTO;
import org.mate.utils.coverage.CoverageUtils;

import java.util.*;

/**
 * Combines the traditional MIO algorithm with the benefits of an EDA. In particular, the original
 * archive of MIO is replaced with a probabilistic model per target from which new chromosomes are
 * sampled. Each probabilistic model is optimized towards one specific target, e.g., a branch, and
 * sampling from a probabilistic model has the benefit that no dedicated mutation operator is required.
 *
 * @param <T> The type wrapped by the chromosomes.
 */
public class MIOEDA<T> extends GeneticAlgorithm<T> {

    /**
     * The underlying probabilistic model.
     */
    private final IProbabilisticModel<T> probabilisticModel;

    /**
     * The archive maintains for each target, e.g., branch, a current state.
     */
    private final List<FitnessFunctionState> archive;

    /**
     * The hardcoded chromosome factory used by MIOEDA to sample new chromosomes.
     */
    private final MIOEDAChromosomeFactory chromosomeFactory;

    /**
     * Represents the current probability P_r for sampling a random chromosome.
     */
    private double pSampleRandom;

    /**
     * The sampling probability P_r during focused search.
     */
    private final double pSampleRandomFocusedSearch = 0.0;

    /**
     * Represents the initial probability P_r for sampling a random chromosome.
     */
    private final double pSampleRandomStart;

    /**
     * Represents the percentage F that defines after which time the focused search should start.
     * For example, F = 0.5 means that the focused search should start when 50% of the search
     * budget is exhausted.
     */
    private final double focusedSearchStart;

    /**
     * Maintains for each target k a sampling counter c_k that determines how often from each target
     * a new chromosome was already sampled.
     */
    private final int[] samplingCounters;

    /**
     * Maintains a set that tracks the targets for which the probabilistic model was initialized and therefore should be updated.
     */
    private final Set<Integer> initializedTargets = new HashSet<>();

    /**
     * Tracks the start point of the search.
     */
    private final long startTime;

    /**
     * Determines whether we are already in the focused search phase.
     */
    private boolean startedFocusedSearch = false;

    /**
     * Keeps track whether all branches have been covered, this not only includes the connected
     * branches (targets) in the underlying control flow graph but really all branches.
     */
    private boolean coveredAllBranches = false;

    /**
     * Initializes MIOEDA with the relevant attributes.
     *
     * @param chromosomeFactory    The used chromosome factory, see {@link IChromosomeFactory}.
     * @param terminationCondition The used termination condition, see {@link ITerminationCondition}.
     * @param probabilisticModel   The probabilistic model.
     * @param pSampleRandom        The sampling probability P_r.
     * @param focusedSearchStart   The start point of the focused search.
     */
    public MIOEDA(IChromosomeFactory<T> chromosomeFactory,
                  ITerminationCondition terminationCondition,
                  IProbabilisticModel<T> probabilisticModel,
                  double pSampleRandom,
                  double focusedSearchStart) {

        super(chromosomeFactory,
                null,
                null,
                null,
                null,
                terminationCondition,
                1,
                1,
                0,
                0);

        this.archive = new ArrayList<>(probabilisticModel.getTargets().size()); // (k -> T_k)
        this.samplingCounters = new int[probabilisticModel.getTargets().size()]; // (k -> c_k)
        this.pSampleRandom = pSampleRandom; // P_r
        this.focusedSearchStart = focusedSearchStart; // F
        this.pSampleRandomStart = pSampleRandom;
        this.chromosomeFactory = (MIOEDAChromosomeFactory) chromosomeFactory;
        this.probabilisticModel = probabilisticModel;

        // Initialise the archive and the sampling counter for each target.
        for (final ActionFitnessFunctionWrapper fitnessFunction : probabilisticModel.getTargets()) {
            archive.add(new FitnessFunctionState(fitnessFunction));
        }

        // Initially the sampling counter c_k for each testing target k is zero.
        Arrays.fill(samplingCounters, 0);

        this.startTime = System.currentTimeMillis();
    }

    /**
     * Creates the initial population consisting of a single random chromosome and then updates the
     * probabilistic models accordingly.
     */
    @Override
    public void createInitialPopulation() {

        MATE.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");

        final IChromosome<T> chromosome = (IChromosome<T>) chromosomeFactory.createChromosome();
        population.add(chromosome);
        evaluatePopulation(population);

        logCurrentFitness();
        currentGenerationNumber++;
        FitnessUtils.cleanCache(population);
        if (coveredAllBranches) {
            ConditionalTerminationCondition.satisfiedCondition();
        }
    }

    /**
     * Forms a new generation consisting of a single chromosome that is either sampled randomly with
     * a probability P_r or otherwise samples the chromosome from the archive, more precisely from
     * one of the probabilistic models.
     */
    @Override
    public void evolve() {
        MATE.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");
        population.clear();

        if (Randomness.getRnd().nextDouble() < pSampleRandom || coveredAllTargets()) {
            // sample random chromosome with probability P_r or in case all targets have been covered
            chromosomeFactory.setSampleRandom(true);
            final IChromosome<T> chromosome = (IChromosome<T>) chromosomeFactory.createChromosome();
            population.add(chromosome);
        } else {
            /*
             * Sample a chromosome from the archive with probability (1 - P_r) from the target k with
             * the lowest sampling counter c_k.
             */
            final ActionFitnessFunctionWrapper target = getBestTarget().getFitnessFunction();

            // increase sampling counter c_k, see section 3.3
            samplingCounters[target.getIndex()] += 1;
            initializedTargets.add(target.getIndex());

            chromosomeFactory.setSampleRandom(false);
            probabilisticModel.setCurrentTarget(target);
            final IChromosome<T> chromosome = (IChromosome<T>) chromosomeFactory.createChromosome();
            population.add(chromosome);
        }

        /*
         * The parameters P_r, n and m linearly increase/decrease over time until the focused
         * search is started.
         */
        if (!startedFocusedSearch) {
            updateParameters();
        }

        // Evaluates the fitness of the current population and updates the archive.
        evaluatePopulation(population);
        logCurrentFitness();
        currentGenerationNumber++;
        FitnessUtils.cleanCache(population);
        if (coveredAllBranches) {
            ConditionalTerminationCondition.satisfiedCondition();
        }
    }

    /**
     * Checks whether all targets, e.g., branches, have been covered.
     *
     * @return Returns {@code true} if all targets have been covered, otherwise {@code false}.
     */
    private boolean coveredAllTargets() {
        return archive.parallelStream().filter(Objects::nonNull).allMatch(FitnessFunctionState::isCovered);
    }

    /**
     * Evaluates the fitness of the given population and then updates the probabilistic model.
     *
     * @param population The population which should be evaluated.
     */
    private void evaluatePopulation(final List<IChromosome<T>> population) {

        final Set<ActionFitnessFunctionWrapper> coveredTargets = new LinkedHashSet<>();

        for (final IChromosome<T> chromosome : population) {
            for (final FitnessFunctionState fitnessFunctionState : archive) {

                if (fitnessFunctionState == null) { // covered target, skip
                    continue;
                }

                // We only need to update probabilistic models that haven't been covered yet.
                if (!fitnessFunctionState.isCovered()) {

                    // We only need to evaluate the fitness for not yet covered targets.
                    final ActionFitnessFunctionWrapper target = fitnessFunctionState.getFitnessFunction();
                    final double fitness = target.getNormalizedFitness((IChromosome<TestCase>) chromosome);
                    if (fitnessFunctionState.updateFitness(fitness)) // the target might be now covered
                        samplingCounters[target.getIndex()] = 0;

                    if (fitnessFunctionState.isCovered()) {
                        coveredTargets.add(target); // mark for removal
                    } else {
                        if (fitness == 0.5f) { // covered the if statement but not the respective branch (target)
                            /*
                            * To keep the number of updates reasonable, we only update the action
                            * probabilities of targets that are close to be covered. It doesn't make
                            * sense to even initialise action probabilities for targets that are
                            * dependent on other targets which haven't been covered yet. In terms of
                            * the control flow graph (CFG), the closest not yet covered targets are
                            * those where the respective if or switch statement has been covered,
                            * which corresponds to an approach level of '1'. Since we deal with
                            * normalised fitness values, this is equal to an approach level of '0.5'
                            * under the assumption that we use x / x + 1 for the normalisation.
                             */
                            probabilisticModel.setCurrentTarget(target);
                            probabilisticModel.update(population);
                        } else if (initializedTargets.contains(target.getIndex())) {
                            /*
                            * We update the action probabilities for those targets where the action
                            * probabilities have been already initialised. This means that we must
                            * have sampled from the target at least once, which can be directly
                            * observed from the sampling counter.
                             */
                            probabilisticModel.setCurrentTarget(target);
                            probabilisticModel.update(population);
                        }
                    }
                }
            }
        }

        if (!coveredTargets.isEmpty()) { // Remove the action probabilities for covered targets.
            probabilisticModel.removeTargets(coveredTargets);

            for (ActionFitnessFunctionWrapper target : coveredTargets) {
                archive.set(target.getIndex(), null);
            }
        }
    }

    /**
     * Retrieves the best target, i.e., the target with the lowest sampling counter. If multiple
     * targets have the same lowest sampling counter, a random selection is performed among them.
     *
     * @return Returns the target associated with the lowest sampling counter.
     */
    private FitnessFunctionState getBestTarget() {

        final List<FitnessFunctionState> possibleTargets = new ArrayList<>();

        // We only need to consider targets that haven't been covered yet but that are likely coverable.
        for (final FitnessFunctionState fitnessFunctionState : archive) {

            if (fitnessFunctionState == null) {
                continue;
            }

            final ActionFitnessFunctionWrapper target = fitnessFunctionState.getFitnessFunction();
            final double fitness = fitnessFunctionState.getBestFitness();
            if (!fitnessFunctionState.isCovered()) {
                if (target.isMaximizing()) {
                    if (fitness > 0.0) {
                        possibleTargets.add(fitnessFunctionState);
                    }
                } else {
                    if (fitness < 1.0) {
                        possibleTargets.add(fitnessFunctionState);
                    }
                }
            }
        }

        MATE.log_acc("We have " + possibleTargets.size() + " possible targets.");

        // If none of the targets is likely coverable, we need to consider all targets again.
        if (possibleTargets.isEmpty()) {
            MATE.log_warn("No uncovered targets found where fitness is better than worst possible fitness value!");
            for (final FitnessFunctionState fitnessFunctionState : archive) {

                if (fitnessFunctionState == null) {
                    continue;
                }

                if (!fitnessFunctionState.isCovered()) {
                    possibleTargets.add(fitnessFunctionState);
                }
            }
        }

        // NOTE: This should never happen since we check whether all targets have been covered after
        // each population which always consists of a single chromosome.
        if (possibleTargets.isEmpty()) {
            throw new IllegalStateException("No further uncovered targets present!");
        }

        // Randomly select a target from the possible candidates with the lowest sampling counter.
        int lowestSamplingCounter = Integer.MAX_VALUE;
        final List<FitnessFunctionState> lowestSamplingCountTargets = new ArrayList<>();

        for (FitnessFunctionState fitnessFunctionState : possibleTargets) {
            final int samplingCounter
                    = samplingCounters[(fitnessFunctionState.getFitnessFunction().getIndex())];

            // Keep track of all targets with the same currently lowest sampling counter.
            if (samplingCounter <= lowestSamplingCounter) {
                // Only update/reset if we have found a smaller sampling counter.
                if (samplingCounter < lowestSamplingCounter) {
                    lowestSamplingCounter = samplingCounter;
                    lowestSamplingCountTargets.clear();
                }
                lowestSamplingCountTargets.add(fitnessFunctionState);
            }
        }

        return lowestSamplingCountTargets.get(Randomness.getRandom(0, lowestSamplingCountTargets.size()));
    }

    /**
     * Updates the parameter P_r. It linearly decreases with the passing of time until the focused
     * search is started.
     */
    private void updateParameters() {

        MATE.log_acc("Updating Parameters...");

        long currentTime = System.currentTimeMillis();
        long expiredTime = currentTime - startTime;
        long focusedSearchStartTime = (long) (Registry.getTimeout() * focusedSearchStart);

        if (expiredTime >= focusedSearchStartTime) {
            MATE.log_acc("Starting focused search...");
            startedFocusedSearch = true;
            pSampleRandom = pSampleRandomFocusedSearch;
        } else {
            float focusedSearchStartProgress = (float) expiredTime / focusedSearchStartTime;
            pSampleRandom = pSampleRandomStart
                    + (pSampleRandomFocusedSearch - pSampleRandomStart) * focusedSearchStartProgress;
        }

        MATE.log_acc("New random sampling rate P_r: " + pSampleRandom);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <S> void logCurrentFitness() {

        MATE.log_acc("Fitness of generation #" + (currentGenerationNumber + 1) + ":");

        // TODO: Use chromosome id in logs instead of natural index + find a better solution for
        //  multi-objective algorithms + a fix for MIO/MOSA/NSGA-II used in combination with GE.

        for (int i = 0; i < Math.min(probabilisticModel.getTargets().size(), 5); i++) {
            MATE.log_acc("Fitness function " + (i + 1) + ":");
            // We can use the cached fitness values here and avoid an unnecessary re-computation.
            final ActionFitnessFunctionWrapper fitnessFunction = probabilisticModel.getTargets().get(i);
            for (int j = 0; j < population.size(); j++) {
                IChromosome<TestCase> chromosome = (IChromosome<TestCase>) population.get(j);
                MATE.log_acc("Chromosome " + (j + 1) + ": " + fitnessFunction.getFitness(chromosome));
            }
        }

        if (probabilisticModel.getTargets().size() > 5) {
            MATE.log_acc("Omitted other fitness function because there are too many ("
                    + probabilisticModel.getTargets().size() + ")");
        }

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {

            /*
             * We only want to terminate the search once we have covered really all branches not only
             * those selected as targets. The initial set of targets refers to connected branches in the
             * underlying control flow graph but there are actually more in most cases, e.g., certain
             * branches couldn't be modelled correctly in the graph. Thus, we check the branch coverage
             * which relies upon all branches not only the connected ones.
             */
            final CoverageDTO coverage = CoverageUtils.getCombinedCoverage(Properties.COVERAGE());
            coveredAllBranches = coverage.getBranchCoverage() == 100.0d;

            MATE.log_acc("Combined coverage until now: " + coverage);

            if (Properties.GENO_TO_PHENO_TYPE_MAPPING()) {

                final List<IChromosome<T>> phenotypePopulation = new ArrayList<>();

                for (IChromosome<T> chromosome : population) {
                    // TODO: Fix this mismatch between the type variables!
                    phenotypePopulation.add(
                            GenotypePhenotypeMappedFitnessFunction
                                    .getPhenoType((IChromosome<S>) chromosome));
                }

                MATE.log_acc("Combined coverage of current population: "
                        + CoverageUtils.getCombinedCoverage(Properties.COVERAGE(), phenotypePopulation));
            } else {
                MATE.log_acc("Combined coverage of current population: "
                        + CoverageUtils.getCombinedCoverage(Properties.COVERAGE(), population));
            }
        }
    }
}
