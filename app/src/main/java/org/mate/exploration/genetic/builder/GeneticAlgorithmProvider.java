package org.mate.exploration.genetic.builder;

import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.algorithm.MIO;
import org.mate.exploration.genetic.algorithm.MOSA;
import org.mate.exploration.genetic.algorithm.NSGAII;
import org.mate.exploration.genetic.algorithm.OnePlusOne;
import org.mate.exploration.genetic.algorithm.RandomSearch;
import org.mate.exploration.genetic.algorithm.RandomWalk;
import org.mate.exploration.genetic.algorithm.Sapienz;
import org.mate.exploration.genetic.algorithm.StandardGeneticAlgorithm;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.AndroidSuiteRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.HeuristicalChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.PrimitiveAndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.SapienzRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.SapienzSuiteRandomChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.CrossOverFunction;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
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
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.exploration.genetic.fitness.MethodCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.SpecificActivityCoveredFitnessFunction;
import org.mate.exploration.genetic.fitness.SuiteActivityFitnessFunction;
import org.mate.exploration.genetic.fitness.TestLengthFitnessFunction;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.mutation.PrimitiveTestCaseShuffleMutationFunction;
import org.mate.exploration.genetic.mutation.SapienzSuiteMutationFunction;
import org.mate.exploration.genetic.mutation.SuiteCutPointMutationFunction;
import org.mate.exploration.genetic.mutation.TestCaseShuffleMutationFunction;
import org.mate.exploration.genetic.selection.CrowdedTournamentSelectionFunction;
import org.mate.exploration.genetic.selection.FitnessProportionateSelectionFunction;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.selection.RandomSelectionFunction;
import org.mate.exploration.genetic.selection.RankSelectionFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.selection.TournamentSelectionFunction;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.termination.IterTerminationCondition;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.exploration.intent.IntentChromosomeFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GeneticAlgorithmProvider {
    private boolean useDefaults;
    private Properties properties;

    public static <T> GeneticAlgorithm<T> getGeneticAlgorithm(Properties properties) {
        GeneticAlgorithmProvider gaProvider = new GeneticAlgorithmProvider(properties);
        return gaProvider.getGeneticAlgorithm();
    }

    private GeneticAlgorithmProvider(Properties properties) {
        this.properties = properties;
        setUseDefaults();
    }

    private void setUseDefaults() {
        useDefaults = properties.getProperty(GeneticAlgorithmBuilder.USE_DEFAULTS_KEY)
                .equals(GeneticAlgorithmBuilder.TRUE_STRING);
    }

    private <T> GeneticAlgorithm<T> getGeneticAlgorithm() {
        String algorithmName = properties.getProperty(GeneticAlgorithmBuilder.ALGORITHM_KEY);
        if (algorithmName == null) {
            throw new IllegalArgumentException("No algorithm specified");
        }
        switch (Algorithm.valueOf(algorithmName)) {
            case STANDARD_GA:
                return initializeGenericGeneticAlgorithm();
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
            default:
                throw new UnsupportedOperationException("Unknown algorithm: " + algorithmName);
        }

    }

    private <T> StandardGeneticAlgorithm<T> initializeGenericGeneticAlgorithm() {
        return new StandardGeneticAlgorithm<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
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
     * @param <T> The type o the chromosomes.
     * @return Returns an instance o the random search algorithm.
     */
    private <T> RandomSearch<T> initializeRandomSearch() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("RandomSearch requires a chromosome factory. You have to" +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTION() == null) {
            throw new IllegalStateException("RandomSearch requires a fitness function. You have to" +
                    "define the property org.mate.Properties.FITNESS_FUNCTION() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("RandomSearch requires a termination condition. You have to" +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        }

        return new RandomSearch<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeFitnessFunctions(),
                this.<T>initializeTerminationCondition());
    }

    private <T> NSGAII<T> initializeNSGAII() {
        return new NSGAII<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
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
     * @param <T> The type o the chromosomes.
     * @return Returns an instance o the MOSA algorithm.
     */
    private <T> MOSA<T> initializeMOSA() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("MOSA requires a chromosome factory. You have to" +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.CROSSOVER_FUNCTION() == null) {
            throw new IllegalStateException("MOSA requires a crossover function. You have to" +
                    "define the property org.mate.Properties.CROSSOVER_FUNCTION() appropriately!");
        } else if (org.mate.Properties.SELECTION_FUNCTION() == null) {
            throw new IllegalStateException("MOSA requires a selection function. You have to" +
                    "define the property org.mate.Properties.SELECTION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTION() == null) {
            throw new IllegalStateException("MOSA requires a mutation function. You have to" +
                    "define the property org.mate.Properties.MUTATION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTION() == null) {
            throw new IllegalStateException("MOSA requires a fitness function. You have to" +
                    "define the property org.mate.Properties.FITNESS_FUNCTION() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("MOSA requires a termination condition. You have to" +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        } else if (org.mate.Properties.OBJECTIVE() == null) {
            throw new IllegalStateException("MOSA requires the type of objectives. You have to" +
                    "define the property org.mate.Properties.OBJECTIVE() appropriately!");
        }

        return new MOSA<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
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
     * @param <T> The type o the chromosomes.
     * @return Returns an instance o the MIO algorithm.
     */
    private <T> MIO<T> initializeMIO() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("MIO requires a chromosome factory. You have to" +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTION() == null) {
            throw new IllegalStateException("MIO requires a mutation function. You have to" +
                    "define the property org.mate.Properties.MUTATION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTION() == null) {
            throw new IllegalStateException("MIO requires a fitness function. You have to" +
                    "define the property org.mate.Properties.FITNESS_FUNCTION() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("MIO requires a termination condition. You have to" +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        } else if (org.mate.Properties.OBJECTIVE() == null) {
            throw new IllegalStateException("MIO requires the type of objectives. You have to" +
                    "define the property org.mate.Properties.OBJECTIVE() appropriately!");
        }

        return new MIO<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeMutationFunction(),
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
     * Initialises the OnePlusOne algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type o the chromosomes.
     * @return Returns an instance o the OnePlusOne algorithm.
     */
    private <T> OnePlusOne<T> initializeOnePlusOne() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("OnePlusOne requires a chromosome factory. You have to" +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTION() == null) {
            throw new IllegalStateException("OnePlusOne requires a mutation function. You have to" +
                    "define the property org.mate.Properties.MUTATION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTION() == null) {
            throw new IllegalStateException("OnePlusOne requires a fitness function. You have to" +
                    "define the property org.mate.Properties.FITNESS_FUNCTION() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("OnePlusOne requires a termination condition. You have to" +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        }

        return new OnePlusOne<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition());
    }

    /**
     * Initialises the random walk algorithm. Ensures that the mandatory properties are defined.
     *
     * @param <T> The type o the chromosomes.
     * @return Returns an instance o the random walk algorithm.
     */
    private <T> RandomWalk<T> initializeRandomWalk() {

        if (org.mate.Properties.CHROMOSOME_FACTORY() == null) {
            throw new IllegalStateException("RandomWalk requires a chromosome factory. You have to" +
                    "define the property org.mate.Properties.CHROMOSOME_FACTORY() appropriately!");
        } else if (org.mate.Properties.MUTATION_FUNCTION() == null) {
            throw new IllegalStateException("RandomWalk requires a mutation function. You have to" +
                    "define the property org.mate.Properties.MUTATION_FUNCTION() appropriately!");
        } else if (org.mate.Properties.FITNESS_FUNCTION() == null) {
            throw new IllegalStateException("RandomWalk requires a fitness function. You have to" +
                    "define the property org.mate.Properties.FITNESS_FUNCTION() appropriately!");
        } else if (org.mate.Properties.TERMINATION_CONDITION() == null) {
            throw new IllegalStateException("RandomWalk requires a termination condition. You have to" +
                    "define the property org.mate.Properties.TERMINATION_CONDITION() appropriately!");
        }

        return new RandomWalk<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                this.initializeTerminationCondition());
    }

    private <T> Sapienz<T> initializeSapienz() {

        if (org.mate.Properties.WIDGET_BASED_ACTIONS()) {
            throw new IllegalStateException("Sapienz can not handle widget-based actions! Turn" +
                    "the property Properties.WIDGET_BASED_ACTIONS() off.");
        }

        return new Sapienz<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getBigPopulationSize(),
                getPCrossOver(),
                getPMutate());
    }

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
            case INTENT_ANDROID_RANDOM_CHROMOSOME_FACTORY:
                return (IChromosomeFactory<T>) new IntentChromosomeFactory(getNumEvents(), org.mate.Properties.RELATIVE_INTENT_AMOUNT());
            case SAPIENZ_RANDOM_CHROMOSOME_FACTORY:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new SapienzRandomChromosomeFactory(getNumEvents());
            case SAPIENZ_SUITE_RANDOM_CHROMOSOME_FACTORY:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new SapienzSuiteRandomChromosomeFactory(getNumTestCases(), getNumEvents());
            default:
                throw new UnsupportedOperationException("Unknown chromosome factory: "
                        + chromosomeFactoryId);
        }
    }

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
                default:
                    throw new UnsupportedOperationException("Unknown selection function: "
                            + selectionFunctionId);
            }
        }
    }

    private <T> ICrossOverFunction<T> initializeCrossOverFunction() {
        String crossOverFunctionId
                = properties.getProperty(GeneticAlgorithmBuilder.CROSSOVER_FUNCTION_KEY);
        if (crossOverFunctionId == null) {
            return null;
        } else {
            switch (CrossOverFunction.valueOf(crossOverFunctionId)) {
                case TEST_CASE_MERGE_CROSS_OVER:
                    // Force cast. Only works if T is TestCase. This fails if other properties expect a
                    // different T for their chromosomes
                    return (ICrossOverFunction<T>) new TestCaseMergeCrossOverFunction();
                case TEST_SUITE_UNIFORM_CROSS_OVER:
                    return (ICrossOverFunction<T>) new UniformSuiteCrossoverFunction();
                case PRIMITIVE_TEST_CASE_MERGE_CROSS_OVER:
                    return (ICrossOverFunction<T>) new PrimitiveTestCaseMergeCrossOverFunction();
                default:
                    throw new UnsupportedOperationException("Unknown crossover function: "
                            + crossOverFunctionId);
            }
        }
    }

    private <T> IMutationFunction<T> initializeMutationFunction() {
        String mutationFunctionId
                = properties.getProperty(GeneticAlgorithmBuilder.MUTATION_FUNCTION_KEY);
        if (mutationFunctionId == null) {
            return null;
        } else {
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
                case PRIMITIVE_SHUFFLE_MUTATION:
                    // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                    // different T for their chromosomes
                    return (IMutationFunction<T>) new PrimitiveTestCaseShuffleMutationFunction();
                case SHUFFLE_MUTATION:
                    // Force cast. Only works if T is TestCase. This fails if other properties expect a
                    // different T for their chromosomes
                    return (IMutationFunction<T>) new TestCaseShuffleMutationFunction(false);
                default:
                    throw new UnsupportedOperationException("Unknown mutation function: "
                            + mutationFunctionId);
            }
        }
    }

    private <T> List<IFitnessFunction<T>> initializeFitnessFunctions() {
        int amountFitnessFunctions = Integer.valueOf(properties.getProperty
                (GeneticAlgorithmBuilder.AMOUNT_FITNESS_FUNCTIONS_KEY));
        if (amountFitnessFunctions == 0) {
            return null;
        } else {
            List<IFitnessFunction<T>> fitnessFunctions = new ArrayList<>();
            for (int i = 0; i < amountFitnessFunctions; i++) {
                fitnessFunctions.add(this.<T>initializeFitnessFunction(i));
            }
            return fitnessFunctions;
        }
    }

    private <T> IFitnessFunction<T> initializeFitnessFunction(int index) {

        String key = String.format(GeneticAlgorithmBuilder.FORMAT_LOCALE, GeneticAlgorithmBuilder
                .FITNESS_FUNCTION_KEY_FORMAT, index);
        String fitnessFunctionId = properties.getProperty(key);

        switch (FitnessFunction.valueOf(fitnessFunctionId)) {
            case NUMBER_OF_ACTIVITIES:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new ActivityFitnessFunction();
            case NUMBER_OF_ACTIVITIES_TEST_SUITES:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new SuiteActivityFitnessFunction();
            case NUMBER_OF_CRASHES:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new AmountCrashesFitnessFunction();
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
                return (IFitnessFunction<T>) new TestLengthFitnessFunction<>();
            case METHOD_COVERAGE:
                return (IFitnessFunction<T>) new MethodCoverageFitnessFunction<>();
            case BRANCH_COVERAGE:
                return (IFitnessFunction<T>) new BranchCoverageFitnessFunction<>();
            case BRANCH_MULTI_OBJECTIVE:
                return (IFitnessFunction<T>) new BranchMultiObjectiveFitnessFunction(getFitnessFunctionArgument(index));
            case BRANCH_DISTANCE:
                return (IFitnessFunction<T>) new BranchDistanceFitnessFunction();
            case BRANCH_DISTANCE_MULTI_OBJECTIVE:
                return (IFitnessFunction<T>) new BranchDistanceMultiObjectiveFitnessFunction(getFitnessFunctionArgument(index));
            case BASIC_BLOCK_MULTI_OBJECTIVE:
                return (IFitnessFunction<T>) new BasicBlockMultiObjectiveFitnessFunction(getFitnessFunctionArgument(index));
            case LINE_COVERAGE:
                return new LineCoverageFitnessFunction<>();
            case LINE_PERCENTAGE_COVERAGE:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new LineCoveredPercentageFitnessFunction(getFitnessFunctionArgument(index));
            case BASIC_BLOCK_LINE_COVERAGE:
                return (IFitnessFunction<T>) new BasicBlockLineCoverageFitnessFunction<>();
            case BASIC_BLOCK_BRANCH_COVERAGE:
                return (IFitnessFunction<T>) new BasicBlockBranchCoverageFitnessFunction<>();
            default:
                throw new UnsupportedOperationException("Unknown fitness function: "
                        + fitnessFunctionId);
        }
    }

    private String getFitnessFunctionArgument(int index) {
        String key = String.format(GeneticAlgorithmBuilder.FORMAT_LOCALE, GeneticAlgorithmBuilder
                .FITNESS_FUNCTION_ARG_KEY_FORMAT, index);
        return properties.getProperty(key);
    }

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

    private int getNumTestCases() {
        String numTestCases = properties.getProperty(GeneticAlgorithmBuilder.NUM_TESTCASES_KEY);
        if (numTestCases == null) {
            if (useDefaults) {
                return org.mate.Properties.NUMBER_TESTCASES();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: number of test cases not specified");
            }
        } else {
            return Integer.valueOf(numTestCases);
        }
    }

    private int getNumEvents() {
        String numEvents = properties.getProperty(GeneticAlgorithmBuilder.MAX_NUM_EVENTS_KEY);
        if (numEvents == null) {
            if (useDefaults) {
                return org.mate.Properties.MAX_NUMBER_EVENTS();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: maximum number of events not specified");
            }
        } else {
            return Integer.valueOf(numEvents);
        }
    }

    private int getNumberIterations() {
        String numberIterations
                = properties.getProperty(GeneticAlgorithmBuilder.NUMBER_ITERATIONS_KEY);
        if (numberIterations == null) {
            if (useDefaults) {
                return org.mate.Properties.EVO_ITERATIONS_NUMBER();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: number of iterations not specified");
            }
        } else {
            return Integer.valueOf(numberIterations);
        }
    }

    private int getPopulationSize() {
        String populationSize
                = properties.getProperty(GeneticAlgorithmBuilder.POPULATION_SIZE_KEY);
        if (populationSize == null) {
            if (useDefaults) {
                return org.mate.Properties.POPULATION_SIZE();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: population size not specified");
            }
        } else {
            return Integer.valueOf(populationSize);
        }
    }

    private int getBigPopulationSize() {
        String bigPopulationSize
                = properties.getProperty(GeneticAlgorithmBuilder.BIG_POPULATION_SIZE_KEY);
        if (bigPopulationSize == null) {
            if (useDefaults) {
                return 2 * getPopulationSize();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: big population size not specified");
            }
        } else {
            return Integer.valueOf(bigPopulationSize);
        }
    }

    private double getPCrossOver() {
        String pCrossover
                = properties.getProperty(GeneticAlgorithmBuilder.P_CROSSOVER_KEY);
        if (pCrossover == null) {
            if (useDefaults) {
                return org.mate.Properties.P_CROSSOVER();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: p cross over not specified");
            }
        } else {
            return Double.valueOf(pCrossover);
        }
    }

    private double getPMutate() {
        String pMutate
                = properties.getProperty(GeneticAlgorithmBuilder.P_MUTATE_KEY);
        if (pMutate == null) {
            if (useDefaults) {
                return org.mate.Properties.P_MUTATE();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: p mutate not specified");
            }
        } else {
            return Double.valueOf(pMutate);
        }
    }

    private double getPSampleRandom() {
        String pSampleRandom
                = properties.getProperty(GeneticAlgorithmBuilder.P_SAMPLE_RANDOM_KEY);
        if (pSampleRandom == null) {
            if (useDefaults) {
                return org.mate.Properties.P_SAMPLE_RANDOM();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: p sample random not specified");
            }
        } else {
            return Double.valueOf(pSampleRandom);
        }
    }

    private double getFocusedSearchStart() {
        String focusedSearchStart
                = properties.getProperty(GeneticAlgorithmBuilder.FOCUSED_SEARCH_START_KEY);
        if (focusedSearchStart == null) {
            if (useDefaults) {
                return org.mate.Properties.P_FOCUSED_SEARCH_START();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: focused search start not specified");
            }
        } else {
            return Double.valueOf(focusedSearchStart);
        }
    }

    private int getMutationRate() {
        String mutationRate
                = properties.getProperty(GeneticAlgorithmBuilder.MUTATION_RATE_KEY);
        if (mutationRate == null) {
            if (useDefaults) {
                return org.mate.Properties.MUTATION_RATE();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: mutation rate not specified");
            }
        } else {
            return Integer.valueOf(mutationRate);
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
            throw new IllegalStateException(
                    "Without using defaults: tournament size not specified");
        }
    }
}
