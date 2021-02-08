package org.mate.exploration.intent;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.interaction.action.intent.ComponentDescription;
import org.mate.interaction.action.intent.ComponentType;
import org.mate.interaction.action.intent.IntentProvider;
import org.mate.model.TestCase;
import org.mate.interaction.action.Action;
import org.mate.utils.CoverageUtils;
import org.mate.utils.FitnessUtils;

public class IntentChromosomeFactory extends AndroidRandomChromosomeFactory {

    public static final String CHROMOSOME_FACTORY_ID = "intent_chromosome_factory";

    // stores the relative amount ([0,1]) of intent based actions that should be used
    private final float relativeIntentAmount;

    private float relativeActivityAmount;
    private float relativeServiceAmount;
    private float relativeReceiverAmount;
    private float relativeDynamicReceiverAmount;
    private float relativeSystemReceiverAmount;
    private float relativeActivityWithOnNewIntentAmount;

    private final IntentProvider intentProvider = new IntentProvider();

    public IntentChromosomeFactory(int maxNumEvents, float relativeIntentAmount) {
        super(maxNumEvents);

        assert relativeIntentAmount >= 0.0 && relativeIntentAmount <= 1.0;
        this.relativeIntentAmount = relativeIntentAmount;
        determineRelativeComponentAmount();
    }

    public IntentChromosomeFactory(boolean resetApp, int maxNumEvents, float relativeIntentAmount) {
        super(resetApp, maxNumEvents);

        assert relativeIntentAmount >= 0.0 && relativeIntentAmount <= 1.0;
        this.relativeIntentAmount = relativeIntentAmount;
        determineRelativeComponentAmount();
    }

    /**
     * Determines the relative amount of each component type. This information is used
     * for the selection probability of a certain component, see {@link #selectAction()}.
     */
    private void determineRelativeComponentAmount() {

        int numberOfComponents = intentProvider.getComponents().size()
                + intentProvider.getSystemEventReceivers().size() + intentProvider.getDynamicReceivers().size();

        relativeSystemReceiverAmount = ((float) intentProvider.getSystemEventReceivers().size()) / numberOfComponents;
        relativeDynamicReceiverAmount = ((float) intentProvider.getDynamicReceivers().size()) / numberOfComponents;

        int numberOfActivities = 0;
        int numberOfServices = 0;
        int numberOfReceivers = 0;
        int numberOfActivitiesHandlingOnNewIntent = 0;

        for (ComponentDescription component : intentProvider.getComponents()) {

            if (component.isActivity()) {
                if (component.isHandlingOnNewIntent()) {
                    numberOfActivitiesHandlingOnNewIntent++;
                }
                numberOfActivities++;
            } else if (component.isService()) {
                numberOfServices++;
            } else if (component.isBroadcastReceiver()) {
                numberOfReceivers++;
            }
        }

        relativeActivityAmount = ((float) numberOfActivities) / numberOfComponents;
        relativeServiceAmount = ((float) numberOfServices) / numberOfComponents;
        relativeReceiverAmount = ((float ) numberOfReceivers) / numberOfComponents;

        // this amount is relative to the total amount of activities handling onCreate and onNewIntent
        relativeActivityWithOnNewIntentAmount = ((float) numberOfActivitiesHandlingOnNewIntent)
                / numberOfActivitiesHandlingOnNewIntent + numberOfActivities;

        MATE.log("Total number of components: " + numberOfComponents);
        MATE.log("Number of Activities: " + numberOfActivities);
        MATE.log("Number of Services: " + numberOfServices);
        MATE.log("Number of Receivers: " + numberOfReceivers);
        MATE.log("Number of system Receivers: " + intentProvider.getSystemEventReceivers().size());
        MATE.log("Number of dynamic Receivers: " + intentProvider.getDynamicReceivers().size());
        MATE.log("Number of Activities handling OnNewIntent: " + numberOfActivitiesHandlingOnNewIntent);
    }


    @Override
    public IChromosome<TestCase> createChromosome() {

        if (resetApp) {
            uiAbstractionLayer.resetApp();
        }

        // TODO: If we can ensure that sdcard files are not touched by the app, then pushing
        //  those files is redundant and we could do this once before creating the first chromosome
        // push dummy files onto sd card
        MATE.log("Pushing custom media files: "
                + Registry.getEnvironmentManager().pushDummyFiles());

        // grant runtime permissions (read/write external storage) which are dropped after each reset
        MATE.log("Grant runtime permissions: "
                + Registry.getEnvironmentManager().grantRuntimePermissions(MATE.packageName));

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (int i = 0; i < maxNumEvents; i++) {
                if (!testCase.updateTestCase(selectAction(), i)) {
                    return chromosome;
                }
            }
        } finally {
            // store coverage, serialize, record stats about test case if desired
            if (!isTestSuiteExecution) {
                /*
                 * If we deal with a test suite execution, the storing of coverage
                 * and fitness data is handled by the AndroidSuiteRandomChromosomeFactory itself.
                 */
                FitnessUtils.storeTestCaseChromosomeFitness(chromosome);
                CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
                CoverageUtils.logChromosomeCoverage(chromosome);
            }
            testCase.finish();
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

        if (random < relativeIntentAmount) {
            // select an intent based action
            double rand = Math.random();

            // select a component based on its relative occurrence in the set of components
            if (rand < relativeServiceAmount && intentProvider.hasService()) {
                // select a service
                return intentProvider.getAction(ComponentType.SERVICE);
            } else if (rand < relativeServiceAmount
                    + relativeReceiverAmount && intentProvider.hasBroadcastReceiver()) {
                // select a broadcast receiver
                return intentProvider.getAction(ComponentType.BROADCAST_RECEIVER);
            } else if (rand < relativeServiceAmount + relativeReceiverAmount
                    + relativeDynamicReceiverAmount && intentProvider.hasDynamicReceiver()) {
                // select a dynamic broadcast receiver
                return intentProvider.getDynamicReceiverAction();
            } else if (rand < relativeServiceAmount + relativeReceiverAmount
                    + relativeDynamicReceiverAmount + relativeSystemReceiverAmount
                    && intentProvider.hasSystemEventReceiver()) {
                // select a system event
                return intentProvider.getSystemEventAction();
            } else {
                // we select an activity (at least the main activity must be exported)
                double rnd = Math.random();

                // select with p = 1/2 either onNewIntent or OnCreate
                if (rnd < 0.5 && intentProvider.isCurrentActivityHandlingOnNewIntent()) {
                    // we trigger the onNewIntent method of the current activity
                    return intentProvider.generateIntentBasedActionForCurrentActivity();
                } else {
                    // we trigger the onCreate method of any activity
                    return intentProvider.getAction(ComponentType.ACTIVITY);
                }
            }
        } else {
            // select a UI action
            return super.selectAction();
        }
    }
}
