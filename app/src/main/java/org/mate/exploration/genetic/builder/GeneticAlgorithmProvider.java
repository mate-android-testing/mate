package org.mate.exploration.genetic.builder;

import org.mate.exploration.genetic.algorithm.MOSA;
import org.mate.exploration.genetic.algorithm.Mio;
import org.mate.exploration.genetic.algorithm.NSGAII;
import org.mate.exploration.genetic.algorithm.OnePlusOne;
import org.mate.exploration.genetic.algorithm.RandomSearch;
import org.mate.exploration.genetic.algorithm.RandomWalk;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.AndroidSuiteRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.HeuristicalChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.algorithm.StandardGeneticAlgorithm;
import org.mate.exploration.genetic.chromosome_factory.PrimitiveAndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.core.GeneticAlgorithm;
import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.crossover.PrimitiveTestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.crossover.TestCaseMergeCrossOverFunction;
import org.mate.exploration.genetic.crossover.UniformSuiteCrossoverFunction;
import org.mate.exploration.genetic.fitness.ActivityFitnessFunction;
import org.mate.exploration.genetic.fitness.AmountCrashesFitnessFunction;
import org.mate.exploration.genetic.fitness.AndroidStateFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunction;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.exploration.genetic.fitness.SpecificActivityCoveredFitnessFunction;
import org.mate.exploration.genetic.fitness.StatementCoverageFitnessFunction;
import org.mate.exploration.genetic.fitness.SuiteActivityFitnessFunction;
import org.mate.exploration.genetic.fitness.TestLengthFitnessFunction;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.mutation.PrimitiveTestCaseShuffleMutationFunction;
import org.mate.exploration.genetic.mutation.SapienzSuiteMutationFunction;
import org.mate.exploration.genetic.mutation.SuiteCutPointMutationFunction;
import org.mate.exploration.genetic.selection.FitnessProportionateSelectionFunction;
import org.mate.exploration.genetic.selection.FitnessSelectionFunction;
import org.mate.exploration.genetic.selection.ISelectionFunction;
import org.mate.exploration.genetic.selection.IdSelectionFunction;
import org.mate.exploration.genetic.selection.RandomSelectionFunction;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.termination.IterTerminationCondition;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.model.TestCase;

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
        switch (algorithmName) {
            case StandardGeneticAlgorithm.ALGORITHM_NAME:
                return initializeGenericGeneticAlgorithm();
            case OnePlusOne.ALGORITHM_NAME:
                return initializeOnePlusOne();
            case NSGAII.ALGORITHM_NAME:
                return initializeNSGAII();
            case MOSA.ALGORITHM_NAME:
                return (GeneticAlgorithm<T>) initializeMOSA();
            case Mio.ALGORITHM_NAME:
                return initializeMio();
            case RandomWalk.ALGORITHM_NAME:
                return initializeRandomWalk();
            case RandomSearch.ALGORITHM_NAME:
                return initializeRandomSearch();
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

    private <T> RandomSearch<T> initializeRandomSearch() {
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

    private <T extends TestCase> MOSA<T> initializeMOSA() {
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

    private <T> Mio<T> initializeMio() {
        return new Mio<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition(),
                getPopulationSize(),
                getBigPopulationSize(),
                getPCrossOver(),
                getPMutate(),
                getPSampleRandom(),
                getFocusedSearchStart());
    }

    private <T> OnePlusOne<T> initializeOnePlusOne() {
        return new OnePlusOne<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeSelectionFunction(),
                this.<T>initializeCrossOverFunction(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                initializeTerminationCondition());
    }

    private <T> RandomWalk<T> initializeRandomWalk() {
        return new RandomWalk<>(
                this.<T>initializeChromosomeFactory(),
                this.<T>initializeMutationFunction(),
                this.<T>initializeFitnessFunctions(),
                this.initializeTerminationCondition());
    }

    private <T> IChromosomeFactory<T> initializeChromosomeFactory() {
        String chromosomeFactoryId
                = properties.getProperty(GeneticAlgorithmBuilder.CHROMOSOME_FACTORY_KEY);
        if (chromosomeFactoryId == null) {
            return null;
        }
        switch (chromosomeFactoryId) {
            case AndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new AndroidRandomChromosomeFactory(getNumEvents());
            case AndroidSuiteRandomChromosomeFactory.CHROMOSOME_FACTORY_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new AndroidSuiteRandomChromosomeFactory(getNumTestCases(), getNumEvents());
            case HeuristicalChromosomeFactory.CHROMOSOME_FACTORY_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new HeuristicalChromosomeFactory(getNumEvents());
            case PrimitiveAndroidRandomChromosomeFactory.CHROMOSOME_FACTORY_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IChromosomeFactory<T>) new PrimitiveAndroidRandomChromosomeFactory(getNumEvents());
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
            switch (selectionFunctionId) {
                case FitnessSelectionFunction.SELECTION_FUNCTION_ID:
                    return new FitnessSelectionFunction<T>();
                case RandomSelectionFunction.SELECTION_FUNCTION_ID:
                    return new RandomSelectionFunction<>();
                case FitnessProportionateSelectionFunction.SELECTION_FUNCTION_ID:
                    return new FitnessProportionateSelectionFunction<>();
                case IdSelectionFunction.SELECTION_FUNCTION_ID:
                    return new IdSelectionFunction<>();
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
            switch (crossOverFunctionId) {
                case TestCaseMergeCrossOverFunction.CROSSOVER_FUNCTION_ID:
                    // Force cast. Only works if T is TestCase. This fails if other properties expect a
                    // different T for their chromosomes
                    return (ICrossOverFunction<T>) new TestCaseMergeCrossOverFunction();
                case UniformSuiteCrossoverFunction.CROSSOVER_FUNCTION_ID:
                    return (ICrossOverFunction<T>) new UniformSuiteCrossoverFunction();
                case PrimitiveTestCaseMergeCrossOverFunction.CROSSOVER_FUNCTION_ID:
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
            switch (mutationFunctionId) {
                case CutPointMutationFunction.MUTATION_FUNCTION_ID:
                    // Force cast. Only works if T is TestCase. This fails if other properties expect a
                    // different T for their chromosomes
                    return (IMutationFunction<T>) new CutPointMutationFunction(getNumEvents());
                case SuiteCutPointMutationFunction.MUTATION_FUNCTION_ID:
                    // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                    // different T for their chromosomes
                    return (IMutationFunction<T>) new SuiteCutPointMutationFunction(getNumEvents());
                case SapienzSuiteMutationFunction.MUTATION_FUNCTION_ID:
                    // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                    // different T for their chromosomes
                    return (IMutationFunction<T>) new SapienzSuiteMutationFunction(getPInnerMutate());
                case PrimitiveTestCaseShuffleMutationFunction.MUTATION_FUNCTION_ID:
                    // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                    // different T for their chromosomes
                    return (IMutationFunction<T>) new PrimitiveTestCaseShuffleMutationFunction();
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

        switch (fitnessFunctionId) {
            case AndroidStateFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new AndroidStateFitnessFunction();
            case ActivityFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new ActivityFitnessFunction();
            case SpecificActivityCoveredFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>)
                        new SpecificActivityCoveredFitnessFunction(getFitnessFunctionArgument(index));
            case AmountCrashesFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>)
                        new AmountCrashesFitnessFunction();
            case TestLengthFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>)
                        new TestLengthFitnessFunction();
            case SuiteActivityFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestSuite. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>)
                        new SuiteActivityFitnessFunction();
            case StatementCoverageFitnessFunction.FITNESS_FUNCTION_ID:
                return new StatementCoverageFitnessFunction<>();
            case LineCoveredPercentageFitnessFunction.FITNESS_FUNCTION_ID:
                // Force cast. Only works if T is TestCase. This fails if other properties expect a
                // different T for their chromosomes
                return (IFitnessFunction<T>) new LineCoveredPercentageFitnessFunction(getFitnessFunctionArgument(index));
            case BranchDistanceFitnessFunction.FITNESS_FUNCTION_ID:
                return (IFitnessFunction<T>) new BranchDistanceFitnessFunction();
            case BranchDistanceFitnessFunctionMultiObjective.FITNESS_FUNCTION_ID:
                return (IFitnessFunction<T>) new BranchDistanceFitnessFunctionMultiObjective(getFitnessFunctionArgument(index));
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
        switch (terminationConditionId) {
            case IterTerminationCondition.TERMINATION_CONDITION_ID:
                return new IterTerminationCondition(getNumberIterations());
            case NeverTerminationCondition.TERMINATION_CONDITION_ID:
                return new NeverTerminationCondition();
            case ConditionalTerminationCondition.TERMINATION_CONDITION_ID:
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

    private double getPInnerMutate() {
        String pInnerMutate
                = properties.getProperty(GeneticAlgorithmBuilder.P_INNER_MUTATE_KEY);
        if (pInnerMutate == null) {
            if (useDefaults) {
                return org.mate.Properties.P_INNER_MUTATE();
            } else {
                throw new IllegalStateException(
                        "Without using defaults: p inner mutate not specified");
            }
        } else {
            return Double.valueOf(pInnerMutate);
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
}
