package org.mate.exploration.genetic.builder;

import org.mate.Registry;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.algorithm.EDA;
import org.mate.exploration.genetic.algorithm.MIO;
import org.mate.exploration.genetic.algorithm.MOSA;
import org.mate.exploration.genetic.algorithm.NSGAII;
import org.mate.exploration.genetic.algorithm.NoveltySearch;
import org.mate.exploration.genetic.algorithm.OnePlusOne;
import org.mate.exploration.genetic.algorithm.RandomSearch;
import org.mate.exploration.genetic.algorithm.RandomWalk;
import org.mate.exploration.genetic.algorithm.Sapienz;
import org.mate.exploration.genetic.algorithm.StandardGeneticAlgorithm;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.AndroidSuiteRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.BitSequenceChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.EDAChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.HeuristicalChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.IntegerSequenceChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.IntentChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.PrimitiveAndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.SapienzRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.SapienzSuiteRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.UniformIntentChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.crossover.IntegerSequencePointCrossOverFunction;
import org.mate.exploration.genetic.crossover.PrimitiveOnePointCrossOverFunction;
import org.mate.exploration.genetic.crossover.PrimitiveTestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.crossover.TestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.crossover.UniformSuiteCrossoverFunction;
import org.mate.exploration.genetic.fitness.ActivityFitnessFunction;
import org.mate.exploration.genetic.fitness.AmountCrashesFitnessFunction;
import org.mate.exploration.genetic.fitness.AndroidStateFitnessFunction;
import org.mate.exploration.genetic.fitness.BasicBlockBranchCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.BasicBlockLineCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.BasicBlockMultiObjectiveFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceMultiObjectiveFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchMultiObjectiveFitnessFunction;
import org.mate.exploration.genetic.fitness.CrashDistance;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.fitness.GenotypePhenotypeMappedFitnessFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.exploration.genetic.fitness.MethodCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.NoveltyFitnessFunction;
import org.mate.exploration.genetic.fitness.SpecificActivityCoveredFitnessFunction;
import org.mate.exploration.genetic.fitness.TestLengthFitnessFunction;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.mutation.IntegerSequenceLengthMutationFunction;
import org.mate.exploration.genetic.mutation.IntegerSequencePointMutationFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.mutation.PrimitiveTestCaseShuffleMutationFunction;
import org.mate.exploration.genetic.mutation.SapienzSuiteMutationFunction;
import org.mate.exploration.genetic.mutation.SuiteCutPointMutationFunction;
import org.mate.exploration.genetic.mutation.TestCaseShuffleMutationFunction;
import org.mate.exploration.genetic.selection.CrowdedTournamentSelectionFunction;
import org.mate.exploration.genetic.selection.FitnessProportionateSelectionFunction;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.selection.NoveltyRankSelectionFunction;
import org.mate.exploration.genetic.selection.RandomSelectionFunction;
import org.mate.exploration.genetic.selection.RankSelectionFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.selection.TournamentSelectionFunction;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.termination.IterTerminationCondition;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.exploration.genetic.util.eda.IProbabilisticModel;
import org.mate.exploration.genetic.util.eda.PIPE;
import org.mate.exploration.genetic.util.ge.AndroidListAnalogousMapping;
import org.mate.exploration.genetic.util.ge.AndroidListBasedBiasedMapping;
import org.mate.exploration.genetic.util.ge.AndroidListBasedEqualWeightedDecisionBiasedMapping;
import org.mate.exploration.genetic.util.ge.GEMappingFunction;
import org.mate.exploration.genetic.util.ge.IGenotypePhenotypeMapping;
import org.mate.model.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mate.Properties.GE_TEST_CASE_ENDING_BIAS_PER_TEN_THOUSAND;

/**
 * Provides a {@link GeneticAlgorithm} by consuming the properties specified via the
 * {@link GeneticAlgorithmBuilder} class.
 */
public class GeneticAlgorithmProvider {

    /**
     * Whether to allow default values if a property was not specified or not.
     */
    private boolean useDefaults;

    /**
     * The list of properties obtained from the {@link GeneticAlgorithmBuilder}.
     */
    private final Properties properties;

    /**
     * Constructs the genetic algorithm by consuming the given properties.
     *
     * @param properties The list of properties.
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the constructed genetic algorithm.
     */
    public static <T> GeneticAlgorithm<T> getGeneticAlgorithm(Properties properties) {
        GeneticAlgorithmProvider gaProvider = new GeneticAlgorithmProvider(properties);
        return gaProvider.getGeneticAlgorithm();
    }

    /**
     * Initialises the genetic algorithm provider with the given properties.
     *
     * @param properties The list of properties.
     */
    private GeneticAlgorithmProvider(Properties properties) {
        this.properties = properties;
        setUseDefaults();
    }

    /**
     * Determines whether default values are allowed or not.
     */
    private void setUseDefaults() {
        useDefaults = properties.getProperty(GeneticAlgorithmBuilder.USE_DEFAULTS_KEY)
                .equals(GeneticAlgorithmBuilder.TRUE_STRING);
    }

    /**
     * Constructs the genetic algorithm.
     *
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the constructed genetic algorithm.
     */
    private <T> GeneticAlgorithm<T> getGeneticAlgorithm() {

        String algorithmName = properties.getProperty(GeneticAlgorithmBuilder.ALGORITHM_KEY);
        if (algorithmName == null) {
            throw new IllegalArgumentException("No algorithm specified");
        }

        switch (Algorithm.valueOf(algorithmName)) {
            case STANDARD_GA:
                return initializeStandardGA();
            case ONE_PLUS_ONE:
                return initializeOnePlusOne();
            case NSGAII:
                return initializeNSGAII();
            case MOSA:
                return initializeMOSA();
            case MIO:
                return initializeMIO();
            case RANDOM_WALK:
                return initializeRandomWalk();
            case RANDOM_SEARCH:
                return initializeRandomSearch();
            case SAPIENZ:
                return initializeSapienz();
            case NOVELTY_SEARCH:
                return initializeNoveltySearch();
            case EDA:
                return initializeEDA();
            default:
                throw new UnsupportedOperationException("Unknown algorithm: " + algorithmName);
        }
    }

    /**
     * Initialises the standard genetic algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type of the chromosomes.
     * @return Returns an instance of the standard genetic algorithm.
     */
    private <T> StandardGeneticAlgorithm<T> initializeStandardGA() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("StandardGA requires a chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.CROSSOVER_FUNCTIONS() == null) {
            throw new IllegalStateException("StandardGA requires a crossover function. You have to " +
                    "define the property org.mate.Properties.CROSSOVER_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.SELECTION_FUNCTION() == null) {
            throw new IllegalStateException("StandardGA requires a selection function. You have to " +
                    "define the property org.mate.Properties.SELECTION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTIONS() == null) {
            throw new IllegalStateException("StandardGA requires a mutation function. You have to " +
                    "define the property org.mate.Properties.MUTATION_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("StandardGA requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("StandardGA requires a termination condition. You have to " +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        }

        return new StandardGeneticAlgorithm<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunctions(),
                this.<T>initializeMutationFunctions(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getBigPopulationSize(),
                getPCrossOver(),
                getPMutate());
    }

    /**
     * Initialises the random search algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type of the chromosomes.
     * @return Returns an instance of the random search algorithm.
     */
    private <T> RandomSearch<T> initializeRandomSearch() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("RandomSearch requires a chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("RandomSearch requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("RandomSearch requires a termination condition. You have to " +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        }

        return new RandomSearch<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeFitnessFunctions(),
                this.<T>initializeTerminationCondition());
    }

    /**
     * Initialises the NSGA-II algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type of the chromosomes.
     * @return Returns an instance of the NSGA-II algorithm.
     */
    private <T> NSGAII<T> initializeNSGAII() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("NSGA-II requires a chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.CROSSOVER_FUNCTIONS() == null) {
            throw new IllegalStateException("NSGA-II requires a crossover function. You have to " +
                    "define the property org.mate.Properties.CROSSOVER_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.SELECTION_FUNCTION() == null) {
            throw new IllegalStateException("NSGA-II requires a selection function. You have to " +
                    "define the property org.mate.Properties.SELECTION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTIONS() == null) {
            throw new IllegalStateException("NSGA-II requires a mutation function. You have to " +
                    "define the property org.mate.Properties.MUTATION_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("NSGA-II requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("NSGA-II requires a termination condition. You have to " +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        }

        return new NSGAII<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunctions(),
                this.<T>initializeMutationFunctions(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getBigPopulationSize(),
                getPCrossOver(),
                getPMutate());
    }

    /**
     * Initialises the MOSA algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type of the chromosomes.
     * @return Returns an instance of the MOSA algorithm.
     */
    private <T> MOSA<T> initializeMOSA() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("MOSA requires a chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.CROSSOVER_FUNCTIONS() == null) {
            throw new IllegalStateException("MOSA requires a crossover function. You have to " +
                    "define the property org.mate.Properties.CROSSOVER_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.SELECTION_FUNCTION() == null) {
            throw new IllegalStateException("MOSA requires a selection function. You have to " +
                    "define the property org.mate.Properties.SELECTION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTIONS() == null) {
            throw new IllegalStateException("MOSA requires a mutation function. You have to " +
                    "define the property org.mate.Properties.MUTATION_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("MOSA requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("MOSA requires a termination condition. You have to " +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        } else if (org.mate.Properties.OBJECTIVE() == null) {
            throw new IllegalStateException("MOSA requires the type of objectives. You have to " +
                    "define the property org.mate.Properties.OBJECTIVE() appropriately!");
        }

        return new MOSA<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunctions(),
                this.<T>initializeMutationFunctions(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getBigPopulationSize(),
                getPCrossOver(),
                getPMutate());
    }

    /**
     * Initialises the MIO algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type of the chromosomes.
     * @return Returns an instance of the MIO algorithm.
     */
    private <T> MIO<T> initializeMIO() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("MIO requires a chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTIONS() == null) {
            throw new IllegalStateException("MIO requires a mutation function. You have to " +
                    "define the property org.mate.Properties.MUTATION_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("MIO requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("MIO requires a termination condition. You have to " +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        } else if (org.mate.Properties.OBJECTIVE() == null) {
            throw new IllegalStateException("MIO requires the type of objectives. You have to " +
                    "define the property org.mate.Properties.OBJECTIVE() appropriately!");
        }

        return new MIO<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeMutationFunctions(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getBigPopulationSize(),
                getPCrossOver(),
                getPMutate(),
                getPSampleRandom(),
                getFocusedSearchStart(),
                getMutationRate());
    }

    /**
     * Initialises the EDA algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type of the chromosomes.
     * @return Returns an instance of the EDA algorithm.
     */
    private <T> EDA<T> initializeEDA() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() != ChromosomeFactory.EDA_CHROMOSOME_FACTORY) {
            throw new IllegalStateException("EDA requires the EDA chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("EDA requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("EDA requires a termination condition. You have to " +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        }

        final List<IFitnessFunction<T>> fitnessFunctions = this.<T>initializeFitnessFunctions();
        final IProbabilisticModel<T> probabilisticModel = getProbabilisticModel(fitnessFunctions);

        // TODO: Hand over the probabilistic model and the fitness functions via a setter within
        //  the EDA class. Then, we can initialise the chromosome factory the default way.

        final IChromosomeFactory<T> chromosomeFactory
                = (IChromosomeFactory<T>) new EDAChromosomeFactory(getNumEvents(),
                probabilisticModel, fitnessFunctions);

        return new EDA<>(
                chromosomeFactory,
                fitnessFunctions,
                initializeTerminationCondition(),
                getPopulationSize(),
                probabilisticModel);
    }

    /**
     * Initialises the OnePlusOne algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type of the chromosomes.
     * @return Returns an instance of the OnePlusOne algorithm.
     */
    private <T> OnePlusOne<T> initializeOnePlusOne() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("OnePlusOne requires a chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTIONS() == null) {
            throw new IllegalStateException("OnePlusOne requires a mutation function. You have to " +
                    "define the property org.mate.Properties.MUTATION_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("OnePlusOne requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("OnePlusOne requires a termination condition. You have to " +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        }

        return new OnePlusOne<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeMutationFunctions(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition());
    }

    /**
     * Initialises the random walk algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type of the chromosomes.
     * @return Returns an instance of the random walk algorithm.
     */
    private <T> RandomWalk<T> initializeRandomWalk() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("RandomWalk requires a chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTIONS() == null) {
            throw new IllegalStateException("RandomWalk requires a mutation function. You have to " +
                    "define the property org.mate.Properties.MUTATION_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("RandomWalk requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("RandomWalk requires a termination condition. You have to " +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        }

        return new RandomWalk<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeMutationFunctions(),
                this.<T>initializeFitnessFunctions(),
                this.initializeTerminationCondition());
    }

    /**
     * Initialises the NoveltySearch algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type o the chromosomes.
     * @return Returns an instance o the NoveltySearch algorithm.
     */
    private <T> NoveltySearch<T> initializeNoveltySearch() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("NoveltySearch requires a chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.CROSSOVER_FUNCTIONS() == null) {
            throw new IllegalStateException("NoveltySearch requires a crossover function. You have to " +
                    "define the property org.mate.Properties.CROSSOVER_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTIONS() == null) {
            throw new IllegalStateException("NoveltySearch requires a mutation function. You have to " +
                    "define the property org.mate.Properties.MUTATION_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.SELECTION_FUNCTION() == null) {
            throw new IllegalStateException("NoveltySearch requires a selection function. You have to " +
                    "define the property org.mate.Properties.SELECTION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("NoveltySearch requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("NoveltySearch requires a termination condition. You have to" +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        } else if (org.mate.Properties.OBJECTIVE() == null) {
            throw new IllegalStateException("NoveltySearch requires an objective. You have to" +
                    "define the property org.mate.Properties.OBJECTIVE() appropriately!");
        }

        return new NoveltySearch<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunctions(),
                this.<T>initializeMutationFunctions(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getBigPopulationSize(),
                getPCrossOver(),
                getPMutate(),
                getNearestNeighbours(),
                getArchiveLimit(),
                getNoveltyThreshold());
    }

    /**
     * Retrieves the novelty threshold used for novelty search.
     *
     * @return Returns the novelty threshold.
     */
    private double getNoveltyThreshold() {
        String noveltyThreshold
                = properties.getProperty(GeneticAlgorithmBuilder.NOVELTY_THRESHOLD_KEY);
        if (noveltyThreshold == null) {
            if (useDefaults) {
                return org.mate.Properties.NOVELTY_THRESHOLD();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: novelty threshold not specified");
            }
        } else {
            return Double.valueOf(noveltyThreshold);
        }
    }

    /**
     * Retrieves the archive size limit used for novelty search.
     *
     * @return Returns the archive limit.
     */
    private int getArchiveLimit() {
        String archiveLimit
                = properties.getProperty(GeneticAlgorithmBuilder.ARCHIVE_LIMIT_KEY);
        if (archiveLimit == null) {
            if (useDefaults) {
                return org.mate.Properties.ARCHIVE_LIMIT();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: archive limit not specified");
            }
        } else {
            return Integer.valueOf(archiveLimit);
        }
    }

    /**
     * Retrieves the nearest neighbours k used for novelty search.
     *
     * @return Returns the nearest neighbours k.
     */
    private int getNearestNeighbours() {
        String nearestNeighbours
                = properties.getProperty(GeneticAlgorithmBuilder.NEAREST_NEIGHBOURS_KEY);
        if (nearestNeighbours == null) {
            if (useDefaults) {
                return org.mate.Properties.NEAREST_NEIGHBOURS();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: nearest neighbours not specified");
            }
        } else {
            return Integer.valueOf(nearestNeighbours);
        }
    }

    /**
     * Initialises the Sapienz algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type of the chromosomes.
     * @return Returns an instance of the Sapienz algorithm.
     */
    private <T> Sapienz<T> initializeSapienz() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("Sapienz requires a chromosome factory. You have to " +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.CROSSOVER_FUNCTIONS() == null) {
            throw new IllegalStateException("Sapienz requires a crossover function. You have to " +
                    "define the property org.mate.Properties.CROSSOVER_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.SELECTION_FUNCTION() == null) {
            throw new IllegalStateException("Sapienz requires a selection function. You have to " +
                    "define the property org.mate.Properties.SELECTION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTIONS() == null) {
            throw new IllegalStateException("Sapienz requires a mutation function. You have to " +
                    "define the property org.mate.Properties.MUTATION_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTIONS() == null) {
            throw new IllegalStateException("Sapienz requires a fitness function. You have to " +
                    "define the property org.mate.Properties.FITNESS_FUNCTIONS() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("Sapienz requires a termination condition. You have to " +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        } else if (!org.mate.Properties.USE_PRIMITIVE_ACTIONS()) {
            throw new IllegalStateException("Sapienz can not handle widget-based actions! Turn " +
                    "the property Properties.USE_PRIMITIVE_ACTIONS() on.");
        }

        return new Sapienz<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunctions(),
                this.<T>initializeMutationFunctions(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getBigPopulationSize(),
                getPCrossOver(),
                getPMutate());
    }

    /**
     * Initialises the chromosome factory of the genetic algorithm.
     *
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the chromosome factory used by the genetic algorithm.
     */
    private <T> IChromosomeFactory<T> initializeChromosomeFactory() {

        String chromosomeFactoryId
                = properties.getProperty(GeneticAlgorithmBuilder.CHROMOSOME_FACTORY_KEY);
        if (chromosomeFactoryId == null) {
            return null;
        }

        switch (ChromosomeFactory.valueOf(chromosomeFactoryId)) {
            case ANDROID_RANDOM_CHROMOSOME_FACTORY:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new AndroidRandomChromosomeFactory(getNumEvents());
            case ANDROID_SUITE_RANDOM_CHROMOSOME_FACTORY:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new AndroidSuiteRandomChromosomeFactory(getNumTestCases(), getNumEvents());
            case HEURISTICAL_CHROMOSOME_FACTORY:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new HeuristicalChromosomeFactory(getNumEvents());
            case PRIMITIVE_ANDROID_RANDOM_CHROMOSOME_FACTORY:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new PrimitiveAndroidRandomChromosomeFactory(getNumEvents());
            case INTENT_CHROMOSOME_FACTORY:
                return (IChromosomeFactory<T>) new IntentChromosomeFactory(getNumEvents(), org.mate.Properties.RELATIVE_INTENT_AMOUNT());
            case UNIFORM_INTENT_CHROMOSOME_FACTORY:
                return (IChromosomeFactory<T>) new UniformIntentChromosomeFactory(getNumEvents(), org.mate.Properties.RELATIVE_INTENT_AMOUNT());
            case SAPIENZ_RANDOM_CHROMOSOME_FACTORY:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new SapienzRandomChromosomeFactory(getNumEvents());
            case SAPIENZ_SUITE_RANDOM_CHROMOSOME_FACTORY:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new SapienzSuiteRandomChromosomeFactory(getNumTestCases(), getNumEvents());
            case BIT_SEQUENCE_CHROMOSOME_FACTORY:
                return (IChromosomeFactory<T>) new BitSequenceChromosomeFactory(getSequenceLength());
            case INTEGER_SEQUENCE_CHROMOSOME_FACTORY:
                return (IChromosomeFactory<T>) new IntegerSequenceChromosomeFactory(getSequenceLength());
            default:
                throw new UnsupportedOperationException("Unknown chromosome factory: "
                        + chromosomeFactoryId);
        }
    }

    /**
     * Initialises the selection function of the genetic algorithm.
     *
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the selection function used by the genetic algorithm.
     */
    private <T> ISelectionFunction<T> initializeSelectionFunction() {

        String selectionFunctionId
                = properties.getProperty(GeneticAlgorithmBuilder.SELECTION_FUNCTION_KEY);
        if (selectionFunctionId == null) {
            return null;
        } else {
            switch (SelectionFunction.valueOf(selectionFunctionId)) {
                case FITNESS_SELECTION:
                    return new FitnessSelectionFunction<T>();
                case RANDOM_SELECTION:
                    return new RandomSelectionFunction<>();
                case FITNESS_PROPORTIONATE_SELECTION:
                    return new FitnessProportionateSelectionFunction<>();
                case TOURNAMENT_SELECTION:
                    return new TournamentSelectionFunction<>(getTournamentSize());
                case RANK_SELECTION:
                    return new RankSelectionFunction<>();
                case CROWDED_TOURNAMENT_SELECTION:
                    return new CrowdedTournamentSelectionFunction<>();
                case NOVELTY_RANK_SELECTION:
                    return new NoveltyRankSelectionFunction<>();
                default:
                    throw new UnsupportedOperationException("Unknown selection function: "
                            + selectionFunctionId);
            }
        }
    }

    /**
     * Initialises the crossover functions of the genetic algorithm.
     *
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the crossover functions used by the genetic algorithm.
     */
    private <T> List<ICrossOverFunction<T>> initializeCrossOverFunctions() {

        int amountCrossoverFunction = Integer.parseInt(
                properties.getProperty(GeneticAlgorithmBuilder.AMOUNT_CROSSOVER_FUNCTIONS_KEY));

        if (amountCrossoverFunction == 0) {
            return null;
        } else {
            List<ICrossOverFunction<T>> crossOverFunctions = new ArrayList<>();
            for (int i = 0; i < amountCrossoverFunction; i++) {
                crossOverFunctions.add(this.initializeCrossOverFunction(i));
            }
            return crossOverFunctions;
        }
    }

    /**
     * Initialises the crossover function of the genetic algorithm.
     *
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the crossover function used by the genetic algorithm.
     */
    private <T> ICrossOverFunction<T> initializeCrossOverFunction(int index) {

        String key = String.format(GeneticAlgorithmBuilder.FORMAT_LOCALE,
                GeneticAlgorithmBuilder.CROSSOVER_FUNCTION_KEY_FORMAT, index);
        String crossOverFunction = properties.getProperty(key);

        switch (CrossOverFunction.valueOf(crossOverFunction)) {
            case TEST_CASE_MERGE_CROSS_OVER:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (ICrossOverFunction<T>) new TestCaseMergeCrossOverFunction();
            case TEST_SUITE_UNIFORM_CROSS_OVER:
                return (ICrossOverFunction<T>) new UniformSuiteCrossoverFunction();
            case PRIMITIVE_TEST_CASE_MERGE_CROSS_OVER:
                return (ICrossOverFunction<T>) new PrimitiveTestCaseMergeCrossOverFunction();
            case INTEGER_SEQUENCE_POINT_CROSS_OVER:
                return (ICrossOverFunction<T>) new IntegerSequencePointCrossOverFunction();
            case PRIMITIVE_ONE_POINT_CROSS_OVER:
                return (ICrossOverFunction<T>) new PrimitiveOnePointCrossOverFunction();
            default:
                throw new UnsupportedOperationException("Unknown crossover function: "
                        + crossOverFunction);
        }
    }

    /**
     * Initialises the mutation functions of the genetic algorithm.
     *
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the mutation functions used by the genetic algorithm.
     */
    private <T> List<IMutationFunction<T>> initializeMutationFunctions() {

        int amountMutationFunction = Integer.parseInt(
                properties.getProperty(GeneticAlgorithmBuilder.AMOUNT_MUTATION_FUNCTIONS_KEY));

        if (amountMutationFunction == 0) {
            return null;
        } else {
            List<IMutationFunction<T>> mutationFunctions = new ArrayList<>();
            for (int i = 0; i < amountMutationFunction; i++) {
                mutationFunctions.add(this.initializeMutationFunction(i));
            }
            return mutationFunctions;
        }
    }

    /**
     * Initialises the mutation function of the genetic algorithm.
     *
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the mutation function used by the genetic algorithm.
     */
    private <T> IMutationFunction<T> initializeMutationFunction(int index) {

        String key = String.format(GeneticAlgorithmBuilder.FORMAT_LOCALE,
                GeneticAlgorithmBuilder.MUTATION_FUNCTION_KEY_FORMAT, index);
        String mutationFunctionId = properties.getProperty(key);

        switch (MutationFunction.valueOf(mutationFunctionId)) {
            case TEST_CASE_CUT_POINT_MUTATION:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IMutationFunction<T>) new CutPointMutationFunction(getNumEvents());
            case TEST_SUITE_CUT_POINT_MUTATION:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IMutationFunction<T>) new SuiteCutPointMutationFunction(getNumEvents());
            case SAPIENZ_MUTATION:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IMutationFunction<T>) new SapienzSuiteMutationFunction(getPMutate());
                //return (IMutationFunction<T>) new SapienzTest(getPMutate());
            case PRIMITIVE_SHUFFLE_MUTATION:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IMutationFunction<T>) new PrimitiveTestCaseShuffleMutationFunction();
            case TEST_CASE_SHUFFLE_MUTATION:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IMutationFunction<T>) new TestCaseShuffleMutationFunction(false);
            case INTEGER_SEQUENCE_POINT_MUTATION:
                return (IMutationFunction<T>) new IntegerSequencePointMutationFunction();
            case INTEGER_SEQUENCE_LENGTH_MUTATION:
                return (IMutationFunction<T>) new IntegerSequenceLengthMutationFunction(getMutationCount());
            default:
                throw new UnsupportedOperationException("Unknown mutation function: "
                        + mutationFunctionId);
        }
    }

    /**
     * Initialises the fitness functions of the genetic algorithm.
     *
     * @param <T> The type wrapped by the chromosomes.
     * @return Returns the fitness functions used by the genetic algorithm.
     */
    private <T> List<IFitnessFunction<T>> initializeFitnessFunctions() {

        final FitnessFunction[] rawFitnessFunctions = org.mate.Properties.FITNESS_FUNCTIONS();
        final boolean withGenoToPhenoTypeMapping = org.mate.Properties.GENO_TO_PHENO_TYPE_MAPPING();

        if (rawFitnessFunctions.length == 0) {
            return null;
        } else {

            final List<IFitnessFunction<T>> fitnessFunctions = new ArrayList<>();

            if (withGenoToPhenoTypeMapping) {

                // initialise the shared geno to pheno type mapping function
                final IGenotypePhenotypeMapping<?,T> mappingFunction = getGenoToPhenoTypeMapping();
                GenotypePhenotypeMappedFitnessFunction.setGenotypePhenotypeMapping(mappingFunction);

                for (int i = 0; i < rawFitnessFunctions.length; i++) {
                    final IFitnessFunction<T> fitnessFunction
                            = initializeGenoToPhenoFitnessFunction(rawFitnessFunctions[i], i);
                    fitnessFunctions.add(fitnessFunction);
                }
            } else {
                for (int i = 0; i < rawFitnessFunctions.length; i++) {
                    final IFitnessFunction<T> fitnessFunction
                            = initializeFitnessFunction(rawFitnessFunctions[i], i);
                    fitnessFunctions.add(fitnessFunction);
                }
            }

            return fitnessFunctions;
        }
    }

    /**
     * Initializes a single fitness function.
     *
     * @param fitnessFunction The fitness function type.
     * @param index The current index of the fitness function.
     * @param <T> The type of the chromosome used by the fitness function.
     * @return Returns the initialized fitness function.
     */
    private <T> IFitnessFunction<T> initializeFitnessFunction(final FitnessFunction fitnessFunction,
                                                                    final int index) {
        switch (fitnessFunction) {
            case NUMBER_OF_ACTIVITIES:
                return new ActivityFitnessFunction<>();
            case NUMBER_OF_CRASHES:
                return new AmountCrashesFitnessFunction<>();
            case NUMBER_OF_STATES:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new AndroidStateFitnessFunction();
            case COVERED_SPECIFIC_ACTIVITY:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>)
                        new SpecificActivityCoveredFitnessFunction(getFitnessFunctionArgument(index));
            case TEST_LENGTH:
                return new TestLengthFitnessFunction<>();
            case METHOD_COVERAGE:
                return new MethodCoverageFitnessFunction<>();
            case BRANCH_COVERAGE:
                return new BranchCoverageFitnessFunction<>();
            case BRANCH_MULTI_OBJECTIVE:
                return new BranchMultiObjectiveFitnessFunction<>(index);
            case BRANCH_DISTANCE:
                return new BranchDistanceFitnessFunction<>();
            case CRASH_DISTANCE:
                return new CrashDistance<>(Registry.getEnvironmentManager().getStackTrace());
            case BRANCH_DISTANCE_MULTI_OBJECTIVE:
                return new BranchDistanceMultiObjectiveFitnessFunction<>(index);
            case BASIC_BLOCK_MULTI_OBJECTIVE:
                return new BasicBlockMultiObjectiveFitnessFunction<>(index);
            case LINE_COVERAGE:
                return new LineCoverageFitnessFunction<>();
            case LINE_PERCENTAGE_COVERAGE:
                return new LineCoveredPercentageFitnessFunction<>(index);
            case BASIC_BLOCK_LINE_COVERAGE:
                return new BasicBlockLineCoverageFitnessFunction<>();
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return new BasicBlockBranchCoverageFitnessFunction<>();
            case NOVELTY:
                return new NoveltyFitnessFunction<>(getFitnessFunctionArgument(index));
            default:
                throw new UnsupportedOperationException("Unknown fitness function: "
                        + fitnessFunction.name());
        }
    }

    /**
     * Initializes a single fitness function with the geno to pheno type mapping.
     *
     * @param fitnessFunction The fitness function type.
     * @param index The index of the current fitness function.
     * @param <T> The type of the chromosome used by the fitness function.
     * @return Returns the initialized fitness function.
     */
    private <T> IFitnessFunction<T> initializeGenoToPhenoFitnessFunction(
            final FitnessFunction fitnessFunction, final int index) {

        final IFitnessFunction<T> phenoTypeFitnessFunction;

        switch (fitnessFunction) {
            case NUMBER_OF_ACTIVITIES:
                phenoTypeFitnessFunction = new ActivityFitnessFunction<>();
                break;
            case NUMBER_OF_CRASHES:
                phenoTypeFitnessFunction = new AmountCrashesFitnessFunction<>();
                break;
            case NUMBER_OF_STATES:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                phenoTypeFitnessFunction = (IFitnessFunction<T>) new AndroidStateFitnessFunction();
                break;
            case COVERED_SPECIFIC_ACTIVITY:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                phenoTypeFitnessFunction = (IFitnessFunction<T>)
                        new SpecificActivityCoveredFitnessFunction(getFitnessFunctionArgument(index));
                break;
            case TEST_LENGTH:
                phenoTypeFitnessFunction = new TestLengthFitnessFunction<>();
                break;
            case BASIC_BLOCK_BRANCH_COVERAGE:
                phenoTypeFitnessFunction = new BasicBlockBranchCoverageFitnessFunction<>();
                break;
            case BASIC_BLOCK_LINE_COVERAGE:
                phenoTypeFitnessFunction = new BasicBlockLineCoverageFitnessFunction<>();
                break;
            case BASIC_BLOCK_MULTI_OBJECTIVE:
                phenoTypeFitnessFunction = new BasicBlockMultiObjectiveFitnessFunction<>(index);
                break;
            case BRANCH_COVERAGE:
                phenoTypeFitnessFunction = new BranchCoverageFitnessFunction<>();
                break;
            case BRANCH_MULTI_OBJECTIVE:
                phenoTypeFitnessFunction = new BranchMultiObjectiveFitnessFunction<>(index);
                break;
            case BRANCH_DISTANCE:
                phenoTypeFitnessFunction = new BranchDistanceFitnessFunction<>();
                break;
            case BRANCH_DISTANCE_MULTI_OBJECTIVE:
                phenoTypeFitnessFunction = new BranchDistanceMultiObjectiveFitnessFunction<>(index);
                break;
            case LINE_COVERAGE:
                phenoTypeFitnessFunction = new LineCoverageFitnessFunction<>();
                break;
            case LINE_PERCENTAGE_COVERAGE:
                phenoTypeFitnessFunction = new LineCoveredPercentageFitnessFunction<>(index);
                break;
            case METHOD_COVERAGE:
                phenoTypeFitnessFunction = new MethodCoverageFitnessFunction<>();
                break;
            case NOVELTY:
                phenoTypeFitnessFunction = new NoveltyFitnessFunction<>(getFitnessFunctionArgument(index));
                break;
            case CRASH_DISTANCE:
                phenoTypeFitnessFunction = new CrashDistance<>(Registry.getEnvironmentManager().getStackTrace());
                break;
            default:
                throw new UnsupportedOperationException("GE fitness function "
                        + fitnessFunction.name() + " not yet supported!");
        }

        return new GenotypePhenotypeMappedFitnessFunction<>(phenoTypeFitnessFunction);
    }

    /**
     * Gets the fitness function argument of the i-th fitness function.
     *
     * @param index The fitness function index.
     * @return Returns the fitness function argument of the i-th fitness function.
     */
    private String getFitnessFunctionArgument(int index) {
        String key = String.format(GeneticAlgorithmBuilder.FORMAT_LOCALE, GeneticAlgorithmBuilder
                .FITNESS_FUNCTION_ARG_KEY_FORMAT, index);
        return properties.getProperty(key);
    }

    /**
     * Initialises the termination condition of the genetic algorithm.
     *
     * @return Returns the termination condition used by the genetic algorithm.
     */
    private ITerminationCondition initializeTerminationCondition() {

        String terminationConditionId
                = properties.getProperty(GeneticAlgorithmBuilder.TERMINATION_CONDITION_KEY);
        if (terminationConditionId == null) {
            return null;
        }

        switch (TerminationCondition.valueOf(terminationConditionId)) {
            case ITERATION_TERMINATION:
                return new IterTerminationCondition(getNumberIterations());
            case NEVER_TERMINATION:
                return new NeverTerminationCondition();
            case CONDITIONAL_TERMINATION:
                return new ConditionalTerminationCondition();
            default:
                throw new UnsupportedOperationException("Unknown termination condition: "
                        + terminationConditionId);
        }
    }

    /**
     * Initializes the probabilistic model for the EDA-based approach.
     *
     * @param fitnessFunctions The fitness function(s) used in EDA.
     * @param <T> The type of the chromosomes.
     * @return Returns the probabilistic model.
     */
    private <T> IProbabilisticModel<T> getProbabilisticModel(final List<IFitnessFunction<T>> fitnessFunctions) {
        // default learning rate 0.01
        // default epsilon 0.000001
        // default clr 0.1
        // default pEl 0.01
        // default pMutation 0.4
        // default mutationRate 0.4
        return (IProbabilisticModel<T>) new PIPE((IFitnessFunction<TestCase>) fitnessFunctions.get(0),
                org.mate.Properties.PIPE_LEARNING_RATE(),
                org.mate.Properties.PIPE_NEGATIVE_LEARNING_RATE(),
                org.mate.Properties.PIPE_EPSILON(),
                org.mate.Properties.PIPE_CLR(),
                org.mate.Properties.PIPE_PROB_ELITIST_LEARNING(),
                org.mate.Properties.PIPE_PROB_MUTATION(),
                org.mate.Properties.PIPE_MUTATION_RATE());
    }

    /**
     * Retrieves the number of test cases per test suite.
     *
     * @return Returns the number of test cases per test suite.
     */
    private int getNumTestCases() {

        String numTestCases = properties.getProperty(GeneticAlgorithmBuilder.NUM_TESTCASES_KEY);
        if (numTestCases == null) {
            if (useDefaults) {
                return org.mate.Properties.NUMBER_TESTCASES();
            } else {
                throw new IllegalStateException("Without using defaults: number of test cases not specified");
            }
        } else {
            return Integer.parseInt(numTestCases);
        }
    }

    /**
     * Retrieves the number of actions per test case.
     *
     * @return Returns the number of actions per test case.
     */
    private int getNumEvents() {

        String numEvents = properties.getProperty(GeneticAlgorithmBuilder.MAX_NUM_EVENTS_KEY);
        if (numEvents == null) {
            if (useDefaults) {
                return org.mate.Properties.MAX_NUMBER_EVENTS();
            } else {
                throw new IllegalStateException("Without using defaults: maximum number of events not specified");
            }
        } else {
            return Integer.parseInt(numEvents);
        }
    }

    /**
     * Retrieves the number of iterations for the {@link IterTerminationCondition}.
     *
     * @return Returns the number of iterations.
     */
    private int getNumberIterations() {

        String numberIterations = properties.getProperty(GeneticAlgorithmBuilder.NUMBER_ITERATIONS_KEY);
        if (numberIterations == null) {
            if (useDefaults) {
                return org.mate.Properties.EVO_ITERATIONS_NUMBER();
            } else {
                throw new IllegalStateException("Without using defaults: number of iterations not specified");
            }
        } else {
            return Integer.parseInt(numberIterations);
        }
    }

    /**
     * Retrieves the population size.
     *
     * @return Returns the population size.
     */
    private int getPopulationSize() {

        String populationSize = properties.getProperty(GeneticAlgorithmBuilder.POPULATION_SIZE_KEY);
        if (populationSize == null) {
            if (useDefaults) {
                return org.mate.Properties.POPULATION_SIZE();
            } else {
                throw new IllegalStateException("Without using defaults: population size not specified");
            }
        } else {
            return Integer.parseInt(populationSize);
        }
    }

    /**
     * Retrieves the big population size.
     *
     * @return Returns the big population size.
     */
    private int getBigPopulationSize() {

        String bigPopulationSize = properties.getProperty(GeneticAlgorithmBuilder.BIG_POPULATION_SIZE_KEY);
        if (bigPopulationSize == null) {
            if (useDefaults) {
                return 2 * getPopulationSize();
            } else {
                throw new IllegalStateException("Without using defaults: big population size not specified");
            }
        } else {
            return Integer.parseInt(bigPopulationSize);
        }
    }

    /**
     * Retrieves the probability for crossover.
     *
     * @return Returns the probability for crossover.
     */
    private double getPCrossOver() {

        String pCrossover = properties.getProperty(GeneticAlgorithmBuilder.P_CROSSOVER_KEY);
        if (pCrossover == null) {
            if (useDefaults) {
                return org.mate.Properties.P_CROSSOVER();
            } else {
                throw new IllegalStateException("Without using defaults: p cross over not specified");
            }
        } else {
            return Double.parseDouble(pCrossover);
        }
    }

    /**
     * Retrieves the probability for mutation.
     *
     * @return Returns the probability for mutation.
     */
    private double getPMutate() {

        String pMutate = properties.getProperty(GeneticAlgorithmBuilder.P_MUTATE_KEY);
        if (pMutate == null) {
            if (useDefaults) {
                return org.mate.Properties.P_MUTATE();
            } else {
                throw new IllegalStateException("Without using defaults: p mutate not specified");
            }
        } else {
            return Double.parseDouble(pMutate);
        }
    }

    /**
     * Retrieves the probability for sampling a random chromosome which is used by {@link MIO}.
     *
     * @return Returns the probability for random sampling.
     */
    private double getPSampleRandom() {

        String pSampleRandom = properties.getProperty(GeneticAlgorithmBuilder.P_SAMPLE_RANDOM_KEY);
        if (pSampleRandom == null) {
            if (useDefaults) {
                return org.mate.Properties.P_SAMPLE_RANDOM();
            } else {
                throw new IllegalStateException("Without using defaults: p sample random not specified");
            }
        } else {
            return Double.parseDouble(pSampleRandom);
        }
    }

    /**
     * Retrieves the percentage when focused search should start which is used by {@link MIO}.
     *
     * @return Returns the percentage when focused search should start.
     */
    private double getFocusedSearchStart() {

        String focusedSearchStart = properties.getProperty(GeneticAlgorithmBuilder.FOCUSED_SEARCH_START_KEY);
        if (focusedSearchStart == null) {
            if (useDefaults) {
                return org.mate.Properties.P_FOCUSED_SEARCH_START();
            } else {
                throw new IllegalStateException("Without using defaults: focused search start not specified");
            }
        } else {
            return Double.parseDouble(focusedSearchStart);
        }
    }

    /**
     * Retrieves the mutation rate which is used by {@link MIO}.
     *
     * @return Returns the mutation rate.
     */
    private int getMutationRate() {

        String mutationRate = properties.getProperty(GeneticAlgorithmBuilder.MUTATION_RATE_KEY);
        if (mutationRate == null) {
            if (useDefaults) {
                return org.mate.Properties.MUTATION_RATE();
            } else {
                throw new IllegalStateException("Without using defaults: mutation rate not specified");
            }
        } else {
            return Integer.parseInt(mutationRate);
        }
    }

    /**
     * Returns the tournament size used for tournament selection.
     *
     * @return Returns the tournament size.
     */
    private int getTournamentSize() {
        if (useDefaults) {
            return org.mate.Properties.TOURNAMENT_SIZE();
        } else {
            throw new IllegalStateException("Without using defaults: tournament size not specified");
        }
    }

    /**
     * Retrieves the sequence length for the {@link BitSequenceChromosomeFactory} and the
     * {@link IntegerSequenceChromosomeFactory}, respectively.
     *
     * @return Returns the sequence length.
     */
    private int getSequenceLength() {
        if (useDefaults) {
            return org.mate.Properties.GE_SEQUENCE_LENGTH();
        } else {
            throw new IllegalStateException("Without using defaults: sequence length not specified");
        }
    }

    /**
     * Retrieves the mutation count for the {@link IntegerSequenceLengthMutationFunction}.
     *
     * @return Returns the mutation count.
     */
    private int getMutationCount() {
        if (useDefaults) {
            return org.mate.Properties.GE_MUTATION_COUNT();
        } else {
            throw new IllegalStateException("Without using defaults: sequence length not specified");
        }
    }

    /**
     * Retrieves the geno to phenotype mapping function used in GE.
     *
     * @param <S> The genotype.
     * @param <T> The phenotype.
     * @return Returns the specified geno to phenotype mapping function.
     */
    private <S, T> IGenotypePhenotypeMapping<S, T> getGenoToPhenoTypeMapping() {

        String geMappingFunction = properties.getProperty(GeneticAlgorithmBuilder.GE_MAPPING_FUNCTION_KEY);

        if (geMappingFunction == null) {
            throw new IllegalStateException("GE mapping function not specified!");
        }

        switch (GEMappingFunction.valueOf(geMappingFunction)) {
            case LIST_ANALOGOUS_MAPPING:
                return (IGenotypePhenotypeMapping<S, T>) new AndroidListAnalogousMapping();
            case LIST_BASED_BIASED_MAPPING:
                return (IGenotypePhenotypeMapping<S, T>)
                        new AndroidListBasedBiasedMapping(GE_TEST_CASE_ENDING_BIAS_PER_TEN_THOUSAND());
            case LIST_BASED_EQUAL_WEIGHTED_MAPPING:
                return (IGenotypePhenotypeMapping<S, T>)
                        new AndroidListBasedEqualWeightedDecisionBiasedMapping(GE_TEST_CASE_ENDING_BIAS_PER_TEN_THOUSAND());
            default:
                throw new UnsupportedOperationException("GE mapping function "
                        + geMappingFunction + " not yet supported!");
        }
    }
}
