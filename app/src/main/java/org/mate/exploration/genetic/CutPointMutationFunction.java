package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.ui.UIAbstractionLayer;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CutPointMutationFunction implements IMutationFunction<TestCase> {
    public static final String MUTATION_FUNCTION_ID = "cut_point_mutation_function";

    private UIAbstractionLayer uiAbstractionLayer;
    private int maxNumEvents;

    public CutPointMutationFunction(int maxNumEvents) {
        this.uiAbstractionLayer = MATE.uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;
    }
    @Override
    public List<IChromosome<TestCase>> mutate(IChromosome<TestCase> chromosome) {
        uiAbstractionLayer.resetApp();

        List<IChromosome<TestCase>> mutations = new ArrayList<>();

        int cutPoint = chooseCutPoint(chromosome.getValue());

        TestCase mutant = new TestCase(UUID.randomUUID().toString());

        mutations.add(new Chromosome<>(mutant));

        updateTestCase(mutant, "init");

        for (int i = 0; i < maxNumEvents; i++) {
            Action newAction;
            if (i < cutPoint) {
                newAction = chromosome.getValue().getEventSequence().get(i);
            } else {
                newAction = Randomness.randomElement(uiAbstractionLayer.getExecutableActions());
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
                case SUCCESS_OUTBOUND: return mutations;
                case FAILURE_UNKNOWN:
                case FAILURE_EMULATOR_CRASH: throw new IllegalStateException("Emulator seems to have crashed. Cannot recover.");
                default: throw new UnsupportedOperationException("Encountered an unknown action result. Cannot continue.");
            }
        }

        return mutations;
    }

    private int chooseCutPoint(TestCase testCase) {
        return Randomness.getRnd().nextInt(testCase.getEventSequence().size());
    }

    private void updateTestCase(TestCase testCase, String event) {
        IScreenState currentScreenstate = uiAbstractionLayer.getCurrentScreenState();

        testCase.updateVisitedStates(currentScreenstate);
        testCase.updateVisitedActivities(currentScreenstate.getActivityName());
        testCase.updateStatesMap(currentScreenstate.getId(), event);
    }
}
