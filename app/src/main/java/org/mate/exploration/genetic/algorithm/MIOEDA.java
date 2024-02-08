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
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;
import org.mate.exploration.genetic.util.eda.ProbabilisticModelState;
import org.mate.model.TestCase;
import org.mate.utils.FitnessUtils;
import org.mate.utils.Randomness;
import org.mate.utils.coverage.Coverage;
import org.mate.utils.coverage.CoverageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * The archive maintains for each target, e.g., branch, a probabilistic model.
     */
    private final Map<ActionFitnessFunctionWrapper, ProbabilisticModelState<T>> archive;

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
     * Wraps the underlying fitness functions such that we can store and retrieve the fitness after
     * individual actions.
     */
    private final List<ActionFitnessFunctionWrapper> fitnessFunctions = new ArrayList<>();

    // represents the variable c_k for each target k
    private final Map<ActionFitnessFunctionWrapper, Integer> samplingCounters;

    // tracks the start point of the search to measure when the focused search should start
    private long startTime;

    // whether the focused search phase has been started yet
    private boolean startedFocusedSearch = false;

    /**
     * Initializes MIOEDA with the relevant attributes.
     *
     * @param chromosomeFactory    The used chromosome factory, see {@link IChromosomeFactory}.
     * @param terminationCondition The used termination condition, see {@link ITerminationCondition}.
     * @param probabilisticModels  The probabilistic models associated each with a fitness function.
     * @param pSampleRandom        The sampling probability P_r.
     * @param focusedSearchStart   The start point of the focused search.
     */
    public MIOEDA(IChromosomeFactory<T> chromosomeFactory,
                  ITerminationCondition terminationCondition,
                  Map<IFitnessFunction<T>, IProbabilisticModel<T>> probabilisticModels,
                  double pSampleRandom,
                  double focusedSearchStart) {

        super(chromosomeFactory,
                null,
                null,
                null,
                new ArrayList<>(probabilisticModels.keySet()),
                terminationCondition,
                1,
                1,
                0,
                0);

        this.archive = new HashMap<>(); // (k -> T_k)
        this.samplingCounters = new HashMap<>(); // (k -> c_k)
        this.pSampleRandom = pSampleRandom; // P_r
        this.focusedSearchStart = focusedSearchStart; // F
        this.pSampleRandomStart = pSampleRandom;

        // Initialise the archive with a probabilistic model for each testing target.
        for (Map.Entry<IFitnessFunction<T>, IProbabilisticModel<T>> entry : probabilisticModels.entrySet()) {
            final ActionFitnessFunctionWrapper fitnessFunction = (ActionFitnessFunctionWrapper) entry.getKey();
            final IProbabilisticModel<T> probabilisticModel = entry.getValue();
            final ProbabilisticModelState<T> probabilisticModelState
                    = new ProbabilisticModelState<T>(fitnessFunction, probabilisticModel);
            this.fitnessFunctions.add(fitnessFunction);
            archive.put(fitnessFunction, probabilisticModelState);

            // initially the sampling counter c_k for each testing target k is zero
            samplingCounters.put(fitnessFunction, 0);
        }

        // The chromosome factory needs to update the probabilistic models when sampling a new chromosome.
        List<ProbabilisticModelState<TestCase>> probabilisticModelStates = new ArrayList(archive.values());
        ((MIOEDAChromosomeFactory) chromosomeFactory).setProbabilisticModelStates(probabilisticModelStates);
    }

    /**
     * Creates the initial population consisting of a single random chromosome and then updates the
     * probabilistic models accordingly.
     */
    @Override
    public void createInitialPopulation() {
        this.startTime = System.currentTimeMillis();

        MATE.log_acc("Generating population # " + (currentGenerationNumber + 1) + "!");

        final IChromosome<T> chromosome = chromosomeFactory.createChromosome();
        population.add(chromosome);
        evaluatePopulation(population);

        logCurrentFitness();
        currentGenerationNumber++;
        FitnessUtils.cleanCache(population);
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

        if (Randomness.getRnd().nextDouble() < pSampleRandom) {
            // sample random chromosome with probability P_r
            ((MIOEDAChromosomeFactory) chromosomeFactory).setProbabilisticModel(null);
            final IChromosome<T> chromosome = chromosomeFactory.createChromosome();
            population.add(chromosome);
        } else {
            /*
             * Sample a chromosome from the archive with probability (1 - P_r) from the target k with
             * the lowest sampling counter c_k.
             */
            final ActionFitnessFunctionWrapper target = getBestTarget().getFitnessFunction();

            // increase sampling counter c_k, see section 3.3
            samplingCounters.put(target, samplingCounters.get(target) + 1);

            final IProbabilisticModel<TestCase> probabilisticModel
                    = (IProbabilisticModel<TestCase>) archive.get(target).getProbabilisticModel();
            ((MIOEDAChromosomeFactory) chromosomeFactory).setProbabilisticModel(probabilisticModel);
            final IChromosome<T> chromosome = chromosomeFactory.createChromosome();
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
    }

    /**
     * Evaluates the fitness of the given population and then updates the probabilistic models.
     *
     * @param population The population which should be evaluated.
     */
    private void evaluatePopulation(final List<IChromosome<T>> population) {
        for (IChromosome<T> chromosome : population) {
            for (ActionFitnessFunctionWrapper target : this.fitnessFunctions) {
                final ProbabilisticModelState<T> probabilisticModelState = archive.get(target);

                // We only need to update probabilistic models that haven't been covered yet.
                if (!probabilisticModelState.isCovered()) {

                    final double fitness = target.getNormalizedFitness((IChromosome<TestCase>) chromosome);

                    // Adjusting the probabilities only makes sense if the model hasn't been covered yet.
                    if (target.isMaximizing()) {
                        if (fitness != 1.0) {
                            probabilisticModelState.getProbabilisticModel().update(population);
                        }
                    } else {
                        if (fitness != 0.0) {
                            probabilisticModelState.getProbabilisticModel().update(population);
                        }
                    }

                    probabilisticModelState.updateFitness(fitness);
                }
            }
        }
    }

    /**
     * Retrieves the probabilistic model associated with the best target, i.e., the target with the
     * lowest sampling counter. If multiple targets have the same lowest sampling counter, a random
     * selection is performed among them.
     *
     * @return Returns the probabilistic model associated with the lowest sampling counter.
     */
    private ProbabilisticModelState<T> getBestTarget() {

        final List<ProbabilisticModelState<T>> possibleTargets = new ArrayList<>();

        // We only need to consider targets that haven't been covered yet but that are likely coverable.
        for (ActionFitnessFunctionWrapper target : fitnessFunctions) {
            final ProbabilisticModelState<T> probabilisticModelState = archive.get(target);
            final double fitness = probabilisticModelState.getBestFitness();
            if (!probabilisticModelState.isCovered()) {
                if (target.isMaximizing()) {
                    if (fitness > 0.0) {
                        possibleTargets.add(probabilisticModelState);
                    }
                } else {
                    if (fitness < 1.0) {
                        possibleTargets.add(probabilisticModelState);
                    }
                }
            }
        }

        MATE.log_acc("We have " + possibleTargets.size() + " possible targets.");

        // If none of the targets is likely coverable, we need to consider all targets again.
        if (possibleTargets.isEmpty()) {
            MATE.log_warn("No uncovered targets found where fitness is better than worst possible fitness value!");
            for (ActionFitnessFunctionWrapper target : fitnessFunctions) {
                final ProbabilisticModelState<T> probabilisticModelState = archive.get(target);
                if (!probabilisticModelState.isCovered()) {
                    possibleTargets.add(probabilisticModelState);
                }
            }
        }

        // Randomly select a target from the possible candidates with the lowest sampling counter.
        int lowestSamplingCounter = Integer.MAX_VALUE;
        final List<ProbabilisticModelState<T>> lowestSamplingCountTargets = new ArrayList<>();

        for (ProbabilisticModelState<T> probabilisticModelState : possibleTargets) {
            final int samplingCounter = samplingCounters.get(probabilisticModelState.getFitnessFunction());

            // Keep track of all targets with the same currently lowest sampling counter.
            if (samplingCounter <= lowestSamplingCounter) {
                // Only update/reset if we have found a smaller sampling counter.
                if (samplingCounter < lowestSamplingCounter) {
                    lowestSamplingCounter = samplingCounter;
                    lowestSamplingCountTargets.clear();
                }
                lowestSamplingCountTargets.add(probabilisticModelState);
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

        for (int i = 0; i < Math.min(this.fitnessFunctions.size(), 5); i++) {
            MATE.log_acc("Fitness function " + (i + 1) + ":");
            // We can use the cached fitness values here and avoid an unnecessary re-computation.
            final ActionFitnessFunctionWrapper fitnessFunction = this.fitnessFunctions.get(i);
            for (int j = 0; j < population.size(); j++) {
                IChromosome<TestCase> chromosome = (IChromosome<TestCase>) population.get(j);
                MATE.log_acc("Chromosome " + (j + 1) + ": " + fitnessFunction.getFitness(chromosome));
            }
        }

        if (this.fitnessFunctions.size() > 5) {
            MATE.log_acc("Omitted other fitness function because there are too many ("
                    + this.fitnessFunctions.size() + ")");
        }

        if (Properties.COVERAGE() != Coverage.NO_COVERAGE) {

            MATE.log_acc("Combined coverage until now: "
                    + CoverageUtils.getCombinedCoverage(Properties.COVERAGE()));

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
