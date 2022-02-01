package org.mate.service.execution;

import org.mate.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.Properties;
import org.mate.exploration.fuzzing.greybox.GreyBoxFuzzer;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.AndroidSuiteRandomChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.PrimitiveAndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.mutation.CutPointMutationFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.mutation.PrimitiveTestCaseShuffleMutationFunction;
import org.mate.exploration.genetic.mutation.SuiteCutPointMutationFunction;
import org.mate.exploration.genetic.termination.ConditionalTerminationCondition;
import org.mate.exploration.genetic.termination.ITerminationCondition;
import org.mate.exploration.genetic.termination.IterTerminationCondition;
import org.mate.exploration.genetic.termination.NeverTerminationCondition;
import org.mate.exploration.genetic.termination.TerminationCondition;

public class ExecuteMATEGreyBoxFuzzing {

    public static void run(String packageName, IRepresentationLayerInterface representationLayer) {

        MATE.log_acc("Starting GreyBox Fuzzing...");

        MATE mate = new MATE(packageName, representationLayer);

        final GreyBoxFuzzer<?> greyBoxFuzzer = new GreyBoxFuzzer<>(
                translateChromosomeFactory(Properties.CHROMOSOME_FACTORY()),
                translateMutationFunction(Properties.MUTATION_FUNCTION()),
                translateTerminationCondition(Properties.TERMINATION_CONDITION()),
                Properties.SEED_CORPUS_SIZE(),
                Properties.MAX_ENERGY()
        );

        mate.testApp(greyBoxFuzzer);
        MATE.log_acc("Total number of crashes: " + greyBoxFuzzer.getCrashingInputs().size());
    }

    private static IChromosomeFactory translateChromosomeFactory(ChromosomeFactory chromosomeFactory) {
        switch (chromosomeFactory) {
            case ANDROID_RANDOM_CHROMOSOME_FACTORY:
                return new AndroidRandomChromosomeFactory(true, Properties.MAX_NUMBER_EVENTS());
            case ANDROID_SUITE_RANDOM_CHROMOSOME_FACTORY:
                return new AndroidSuiteRandomChromosomeFactory(Properties.NUMBER_TESTCASES(),
                        Properties.MAX_NUMBER_EVENTS());
            case PRIMITIVE_ANDROID_RANDOM_CHROMOSOME_FACTORY:
                return new PrimitiveAndroidRandomChromosomeFactory(true, Properties.MAX_NUMBER_EVENTS());
            default:
                throw new IllegalArgumentException("Chromosome factory " + chromosomeFactory
                        + " not yet supported for greybox fuzzing!");
        }
    }

    private static IMutationFunction translateMutationFunction(MutationFunction mutationFunction) {
        switch (mutationFunction) {
            case TEST_CASE_CUT_POINT_MUTATION:
                return new CutPointMutationFunction(Properties.MAX_NUMBER_EVENTS());
            case TEST_SUITE_CUT_POINT_MUTATION:
                return new SuiteCutPointMutationFunction(Properties.MAX_NUMBER_EVENTS());
            case PRIMITIVE_SHUFFLE_MUTATION:
                return new PrimitiveTestCaseShuffleMutationFunction();
            default:
                throw new IllegalArgumentException("Mutation function " + mutationFunction
                        + " not yet supported for greybox fuzzing!");
        }
    }

    private static ITerminationCondition translateTerminationCondition(TerminationCondition terminationCondition) {
        switch (terminationCondition) {
            case NEVER_TERMINATION:
                return new NeverTerminationCondition();
            case ITERATION_TERMINATION:
                return new IterTerminationCondition(Properties.EVO_ITERATIONS_NUMBER());
            case CONDITIONAL_TERMINATION:
                return new ConditionalTerminationCondition();
            default:
                throw new IllegalArgumentException("Termination condition " + terminationCondition
                        + " not yet supported for greybox fuzzing!");
        }
    }
}
