package org.mate.exploration.genetic;

import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.ui.UIAbstractionLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CutPointMutationFunction implements IMutationFunction<TestCase> {
    private UIAbstractionLayer uiAbstractionLayer;
    private int maxNumEvents;
    private Random rnd;

    public CutPointMutationFunction(UIAbstractionLayer uiAbstractionLayer, int maxNumEvents) {
        this.uiAbstractionLayer = uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;
        rnd = new Random();
    }
    @Override
    public List<IChromosome<TestCase>> mutate(IChromosome<TestCase> parent) {
        List<IChromosome<TestCase>> offspring = new ArrayList<>();

        int cutPoint = chooseCutPoint(parent.getValue());

        TestCase mutant = new TestCase(UUID.randomUUID().toString());

        offspring.add(new Chromosome<>(mutant));

        updateTestCase(mutant, "init");

        for (int i = 0; i < maxNumEvents; i++) {
            Action newAction;
            if (i < cutPoint) {
                newAction = parent.getValue().getEventSequence().get(i);
            } else {
                newAction = uiAbstractionLayer.getRandomExecutableAction();
            }
            mutant.addEvent(newAction);
            UIAbstractionLayer.ActionResult actionResult = uiAbstractionLayer.executeAction(newAction);

            switch (actionResult) {
                case SUCCESS:
                case SUCCESS_NEW_STATE:
                    updateTestCase(mutant, String.valueOf(i));
                    break;
                case FAILURE_APP_CRASH:
                    mutant.setCrashDetected();
                case SUCCESS_OUTBOUND: return offspring;
                case FAILURE_UNKNOWN:
                case FAILURE_EMULATOR_CRASH: throw new IllegalStateException("Emulator seems to have crashed. Cannot recover.");
                default: throw new UnsupportedOperationException("Encountered an unknown action result. Cannot continue.");
            }
        }

        return offspring;
    }

    private int chooseCutPoint(TestCase testCase) {
        return rnd.nextInt(testCase.getEventSequence().size());
    }

    private void updateTestCase(TestCase testCase, String event) {
        IScreenState currentScreenstate = uiAbstractionLayer.getCurrentScreenState();

        testCase.updateVisitedStates(currentScreenstate);
        testCase.updateVisitedActivities(currentScreenstate.getActivityName());
        testCase.updateStatesMap(currentScreenstate.getId(), event);
    }
}
