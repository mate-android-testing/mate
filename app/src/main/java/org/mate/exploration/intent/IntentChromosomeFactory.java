package org.mate.exploration.intent;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.exploration.genetic.fitness.BranchDistanceFitnessFunctionMultiObjective;
import org.mate.exploration.genetic.fitness.LineCoveredPercentageFitnessFunction;
import org.mate.interaction.intent.ComponentType;
import org.mate.interaction.intent.IntentBasedAction;
import org.mate.interaction.intent.IntentProvider;
import org.mate.interaction.intent.SystemAction;
import org.mate.model.TestCase;
import org.mate.serialization.TestCaseSerializer;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.ui.PrimitiveAction;
import org.mate.utils.Coverage;
import org.mate.utils.TestCaseOptimizer;
import org.mate.utils.TestCaseStatistics;
import org.mate.utils.TimeoutRun;

import java.util.Random;

public class IntentChromosomeFactory extends AndroidRandomChromosomeFactory {

    public static final String CHROMOSOME_FACTORY_ID = "intent_chromosome_factory";

    // stores the relative amount ([0,1]) of intent based actions that should be used
    private final float relativeIntentAmount;

    private final IntentProvider intentProvider = new IntentProvider();

    public IntentChromosomeFactory(int maxNumEvents, float relativeIntentAmount) {
        super(maxNumEvents);

        assert relativeIntentAmount >= 0.0 && relativeIntentAmount <= 1.0;
        this.relativeIntentAmount = relativeIntentAmount;
    }

    public IntentChromosomeFactory(boolean storeCoverage, boolean resetApp, int maxNumEvents, float relativeIntentAmount) {
        super(storeCoverage, resetApp, maxNumEvents);

        assert relativeIntentAmount >= 0.0 && relativeIntentAmount <= 1.0;
        this.relativeIntentAmount = relativeIntentAmount;
    }


    @Override
    public IChromosome<TestCase> createChromosome() {

        if (resetApp) {
            uiAbstractionLayer.resetApp();
        }

        // grant runtime permissions (read/write external storage) which are dropped after each reset
        Registry.getEnvironmentManager().grantRuntimePermissions(MATE.packageName);

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        if (Properties.REPLAY_TEST_CASE()) {
            testCase = TestCaseSerializer.deserializeTestCase();

            if (Properties.OPTIMISE_TEST_CASE()) {
                testCase = TestCaseOptimizer.optimise(testCase);
            }

            // we can only replay the number of actually serialized actions
            maxNumEvents = testCase.getEventSequence().size();
        }

        try {
            for (int i = 0; i < maxNumEvents; i++) {

                Action nextAction = null;

                if (Properties.REPLAY_TEST_CASE()) {
                    MATE.log("Replaying Action " + i);
                    nextAction = testCase.getEventSequence().get(i);
                } else {
                    nextAction = selectAction();
                }

                if (!testCase.updateTestCase(nextAction, String.valueOf(i))) {
                    return chromosome;
                }
            }
        } finally {

            if (Properties.RECORD_TEST_CASE()) {
                TestCaseSerializer.serializeTestCase(testCase);
            }

            // record stats about test case, in particular intent based actions
            TestCaseStatistics.recordStats(testCase, intentProvider.getComponents());

            /*
            //TODO: remove hack, when better solution implemented (query fitness function)
            if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
                LineCoveredPercentageFitnessFunction.retrieveFitnessValues(chromosome);
            } else if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {
                BranchDistanceFitnessFunctionMultiObjective.retrieveFitnessValues(chromosome);
            }
            */

            //store coverage in any case
            if (storeCoverage) {

                if (Properties.COVERAGE() == Coverage.LINE_COVERAGE) {
                    Registry.getEnvironmentManager().storeCoverageData(chromosome, null);

                    MATE.log_acc("Coverage of: " + chromosome.toString() + ": " + Registry.getEnvironmentManager()
                            .getCoverage(chromosome));

                } else if (Properties.COVERAGE() == Coverage.BRANCH_COVERAGE) {

                    // TODO: this should be depended on which fitness function is used
                    // BranchDistanceFitnessFunction.retrieveFitnessValues(chromosome);

                    Registry.getEnvironmentManager().storeBranchCoverage(chromosome);

                    MATE.log_acc("Coverage of: " + chromosome.toString() + ": " + Registry.getEnvironmentManager()
                            .getBranchCoverage(chromosome));
                }

                MATE.log_acc("Found crash: " + String.valueOf(chromosome.getValue().getCrashDetected()));
            }
        }
        return chromosome;
    }

    /**
     * Selects the next action to be executed. This can be either an
     * an Intent-based action, a system event notification or a UI action depending
     * on the probability specified by {@code relativeIntentAmount}.
     *
     * @return Returns the action to be performed next.
     */
    @Override
    protected Action selectAction() {

        double random = Math.random();
        final float EQUAL_INTERVAL_PROBABILITY = relativeIntentAmount / 6;

        if (random < relativeIntentAmount) {
            // generate an Intent-based action or a system event notification with equal probability

            if (random < EQUAL_INTERVAL_PROBABILITY && intentProvider.hasService()) {
                // select a service
                return intentProvider.getAction(ComponentType.SERVICE);
            } else if (random < 2 * EQUAL_INTERVAL_PROBABILITY && intentProvider.hasBroadcastReceiver()) {
                // select a broadcast receiver
                return intentProvider.getAction(ComponentType.BROADCAST_RECEIVER);
            } else if (random < 3 * EQUAL_INTERVAL_PROBABILITY && intentProvider.hasSystemEventReceiver()) {
                // select a system event
                return intentProvider.getSystemEventAction();
            } else if (random < 4 * EQUAL_INTERVAL_PROBABILITY && intentProvider.hasDynamicReceiver()) {
                // select a dynamic broadcast receiver
                return intentProvider.getDynamicReceiverAction();
            } else if (random < 5 * EQUAL_INTERVAL_PROBABILITY && intentProvider.isCurrentActivityHandlingOnNewIntent()) {
                // trigger the current activity's onNewIntent method
                return intentProvider.generateIntentBasedActionForCurrentActivity();
            } else {
                // select an activity (there must be at least the main activity exported!
                return intentProvider.getAction(ComponentType.ACTIVITY);
                // TODO: may integrate probability to swap activity
            }
        } else {
            // select a UI action
            return super.selectAction();
        }
    }
}
