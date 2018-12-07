package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.ui.UIAbstractionLayer;
import org.mate.utils.Randomness;

import java.util.UUID;

public class AndroidRandomChromosomeFactory implements IChromosomeFactory<TestCase> {
    public static final String CHROMOSOME_FACTORY_ID = "android_random_chromosome_factory";

    private UIAbstractionLayer uiAbstractionLayer;
    private int maxNumEvents;

    public AndroidRandomChromosomeFactory(int maxNumEvents) {
        this.uiAbstractionLayer = MATE.uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        uiAbstractionLayer.resetApp();

        TestCase testCase = new TestCase(UUID.randomUUID().toString());
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        updateTestCase(testCase, "init");

        for (int i = 0 ; i < maxNumEvents; i++) {
            Action newAction = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
            testCase.addEvent(newAction);
            UIAbstractionLayer.ActionResult actionResult = uiAbstractionLayer.executeAction(newAction);

            switch (actionResult) {
                case SUCCESS:
                case SUCCESS_NEW_STATE:
                    updateTestCase(testCase, String.valueOf(i));
                    break;
                case FAILURE_APP_CRASH:
                    testCase.setCrashDetected();
                case SUCCESS_OUTBOUND: return chromosome;
                case FAILURE_UNKNOWN:
                case FAILURE_EMULATOR_CRASH: throw new IllegalStateException("Emulator seems to have crashed. Cannot recover.");
                default: throw new UnsupportedOperationException("Encountered an unknown action result. Cannot continue.");
            }
        }

        return chromosome;
    }

    private void updateTestCase(TestCase testCase, String event) {
        IScreenState currentScreenstate = uiAbstractionLayer.getCurrentScreenState();

        testCase.updateVisitedStates(currentScreenstate);
        testCase.updateVisitedActivities(currentScreenstate.getActivityName());
        testCase.updateStatesMap(currentScreenstate.getId(), event);
    }
}
