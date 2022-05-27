package org.mate.exploration.intent;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.manifest.element.ComponentDescription;
import org.mate.commons.utils.manifest.element.ComponentType;
import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;
import org.mate.model.TestCase;
import org.mate.model.fsm.surrogate.SurrogateModel;
import org.mate.utils.FitnessUtils;
import org.mate.utils.coverage.CoverageUtils;

/**
 * Provides a chromosome factory that produces {@link TestCase}s consisting of a combination of
 * {@link UIAction}, {@link org.mate.commons.interaction.action.intent.IntentBasedAction}
 * and {@link org.mate.commons.interaction.action.intent.SystemAction} actions.
 */
public class IntentChromosomeFactory extends AndroidRandomChromosomeFactory {

    /**
     * The relative amount of intent and system actions.
     */
    private final float relativeIntentAmount;

    /**
     * The relative amount of the respective components.
     */
    private float relativeActivityAmount;
    private float relativeServiceAmount;
    private float relativeReceiverAmount;
    private float relativeDynamicReceiverAmount;
    private float relativeSystemReceiverAmount;
    private float relativeActivityWithOnNewIntentAmount;

    /**
     * Generates the requested intent-based and system actions.
     */
    private IntentProvider intentProvider;

    /**
     * Initialises the chromosome factory with the maximal number of actions and the probability
     * for generating an intent-based or system action instead of a ui action.
     *
     * @param maxNumEvents The maximal number of actions per test case.
     * @param relativeIntentAmount The probability in [0,1] for generating an intent-based or
     *                             system action.
     */
    public IntentChromosomeFactory(int maxNumEvents, float relativeIntentAmount) {

        super(maxNumEvents);

        this.intentProvider = new IntentProvider();

        assert relativeIntentAmount >= 0.0 && relativeIntentAmount <= 1.0;
        this.relativeIntentAmount = relativeIntentAmount;
        determineRelativeComponentAmount();
    }

    /**
     * Initialises the chromosome factory with the maximal number of actions and the probability
     * for generating an intent-based or system action instead of a ui action.
     *
     * @param resetApp Whether the app should be reset before creating a new test case.
     * @param maxNumEvents The maximal number of actions per test case.
     * @param relativeIntentAmount The probability in [0,1] for generating an intent-based or
     *                             system action.
     */
    public IntentChromosomeFactory(boolean resetApp, int maxNumEvents, float relativeIntentAmount) {
        super(resetApp, maxNumEvents);

        this.intentProvider = new IntentProvider();

        assert relativeIntentAmount >= 0.0 && relativeIntentAmount <= 1.0;
        this.relativeIntentAmount = relativeIntentAmount;
        determineRelativeComponentAmount();
    }

    /**
     * Determines the relative amount of each component type. This information is used for the
     * selection probability of a certain component, see {@link #selectAction()}.
     */
    private void determineRelativeComponentAmount() {

        /*
        * TODO: Fix the relative component type computation. The problem is that broadcast receivers
        *  can be a mixture of usual, dynamic and system event receivers, whereas a system event
        *  receiver can be as well dynamic in nature. Moreover, a system event receiver shows up
        *  multiple times in the list of system event receivers, one entry for each intent filter.
         */
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

        MATELog.log("Total number of components: " + numberOfComponents);
        MATELog.log("Number of Activities: " + numberOfActivities);
        MATELog.log("Number of Services: " + numberOfServices);
        MATELog.log("Number of Receivers: " + numberOfReceivers);
        MATELog.log("Number of system Receivers: " + intentProvider.getSystemEventReceivers().size());
        MATELog.log("Number of dynamic Receivers: " + intentProvider.getDynamicReceivers().size());
        MATELog.log("Number of Activities handling OnNewIntent: " + numberOfActivitiesHandlingOnNewIntent);
    }

    /**
     * Creates a new chromosome wrapping a test case which in turn consists of a combination of
     * intent-based, system and ui actions.
     *
     * @return Returns the generated chromosome.
     */
    @Override
    public IChromosome<TestCase> createChromosome() {

        if (resetApp) {
            uiAbstractionLayer.resetApp();
        }

        // TODO: If we can ensure that sdcard files are not touched by the app, then pushing
        //  those files is redundant and we could do this once before creating the first chromosome
        // push dummy files onto sd card
        MATELog.log("Pushing custom media files: "
                + Registry.getEnvironmentManager().pushDummyFiles());

        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        try {
            for (int i = 0; i < maxNumEvents; i++) {
                if (!testCase.updateTestCase(selectAction(), i)) {
                    return chromosome;
                }
            }
        } finally {

            if (Properties.SURROGATE_MODEL()) {
                // update sequences + write traces to external storage
                SurrogateModel surrogateModel = (SurrogateModel) uiAbstractionLayer.getGuiModel();
                surrogateModel.updateTestCase(testCase);
            }

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
     * Selects the next action to be executed. This can be either an an intent-based action,
     * a system event notification or a ui action depending on the probability specified by
     * {@link #relativeIntentAmount}.
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
