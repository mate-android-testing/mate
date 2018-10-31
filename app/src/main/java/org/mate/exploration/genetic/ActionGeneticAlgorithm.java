package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.ui.UIAbstractionLayer;
import org.mate.ui.UIAbstractionLayer.ActionResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class ActionGeneticAlgorithm extends GeneticAlgorithm {
    protected final UIAbstractionLayer uiAbstractionLayer;
    private int maxNumEvents;
    private final Map<String, Set<String>> coverageArchive;
    private final Set<String> crashArchive;

    public ActionGeneticAlgorithm(int populationSize, int generationSurvivorCount, float pCrossover, float pMutate, final UIAbstractionLayer uiAbstractionLayer, int maxNumEvents) {
        super(populationSize, generationSurvivorCount, pCrossover, pMutate);

        this.uiAbstractionLayer = uiAbstractionLayer;
        this.maxNumEvents = maxNumEvents;

        coverageArchive = new HashMap<>();
        crashArchive = new HashSet<>();
    }

    @Override
    protected IChromosome createInitialRandomChromosome() {
        uiAbstractionLayer.resetApp();

        TestCase testCase = new TestCase(UUID.randomUUID().toString());
        ActionChromosome chromosome = new ActionChromosome(testCase);

        updateTestCase(testCase, "init");

        for (int i = 0 ; i < maxNumEvents; i++) {
            Action newAction = uiAbstractionLayer.getRandomExecutableAction();
            testCase.addEvent(newAction);
            ActionResult actionResult = uiAbstractionLayer.executeAction(newAction);

            switch (actionResult) {
                case SUCCESS: break;
                case SUCCESS_NEW_STATE:
                    MATE.log("New State found:" + uiAbstractionLayer.getCurrentScreenState().getId());
                    break;
                case FAILURE_APP_CRASH:
                    testCase.setCrashDetected();
                    crashArchive.add(testCase.getId());
                case SUCCESS_OUTBOUND: return chromosome;
                case FAILURE_UNKOWN:
                case FAILURE_EMULATOR_CRASH: throw new IllegalStateException("Emulator seems to have crashed. Cannot recover.");
                default: throw new UnsupportedOperationException("Encountered an unknown action result. Cannot continue.");
            }
        }

        return chromosome;
    }

    protected void updateTestCase(TestCase testCase, String event) {
        IScreenState currentScreenstate = uiAbstractionLayer.getCurrentScreenState();

        testCase.updateVisitedStates(currentScreenstate);
        testCase.updateVisitedActivities(currentScreenstate.getActivityName());
        testCase.updateStatesMap(currentScreenstate.getId(), event);
        updateCoverageArchive(currentScreenstate.getId(), testCase.getId());
    }

    protected void updateCoverageArchive(String state, String tc) {
        if (!coverageArchive.containsKey(state)){
            Set<String> coveredStates = new HashSet<>();
            coveredStates.add(tc);
            coverageArchive.put(state, coveredStates);
        }
        else {
            coverageArchive.get(state).add(tc);
        }
    }
}
