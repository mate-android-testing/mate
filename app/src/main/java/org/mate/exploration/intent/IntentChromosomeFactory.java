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
import org.mate.interaction.intent.IntentProvider;
import org.mate.model.TestCase;
import org.mate.serialization.TestCaseSerializer;
import org.mate.ui.Action;
import org.mate.ui.PrimitiveAction;
import org.mate.utils.Coverage;

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

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (int i = 0; i < maxNumEvents; i++) {
                if (!testCase.updateTestCase(selectAction(), String.valueOf(i))) {
                    return chromosome;
                }
            }
        } finally {

            // TODO: add boolean flag for record/replay
            // record test case
            TestCaseSerializer.serializeTestCase(testCase);

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
     * an Intent-based action or a UI action depending
     * on the probability specified by {@code relativeIntentAmount}.
     *
     * @return Returns the action to be performed next.
     */
    @Override
    protected Action selectAction() {

        // TODO: make each case equally-probably (activity,service,broadcast-receiver)
        // integrate certain system-level events, e.g. screen_rotate into UI actions via UIDevice-API

        double random = Math.random();

        if (random < relativeIntentAmount) {
            // generate an Intent-based action

            if (intentProvider.hasService() && random < Properties.SERVICE_SELECTION_PROBABILITY()) {
                // select a service
                MATE.log_acc("Selecting a service as target component!");
                return intentProvider.getAction(ComponentType.SERVICE);

            } else if (intentProvider.hasBroadcastReceiver()
                    && random < Properties.BROADCAST_RECEIVER_SELECTION_PROBABILITY()) {
                // select a broadcast receiver
                MATE.log_acc("Selecting a broadcast receiver as target component!");
                return intentProvider.getAction(ComponentType.BROADCAST_RECEIVER);

            } else if (intentProvider.hasActivity()) {
                // select an activity
                MATE.log_acc("Selecting an activity as target component!");
                return intentProvider.getAction(ComponentType.ACTIVITY);
                // TODO: may integrate probability to swap activity

            } else {
                // fall back to UI action
                MATE.log_acc("Fallback to UI action!");
                return super.selectAction();
            }
        } else {
            // select a UI action
            MATE.log_acc("Selecting a UI action!");
            return super.selectAction();
        }
    }
}
