package org.mate.model;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.ui.PrimitiveAction;
import org.mate.ui.WidgetAction;
import org.mate.utils.Optional;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestCase {
    private String id;
    private Set<String> visitedActivities;
    private Set<String> visitedStates;
    private List<Action> eventSequence;
    private float novelty;
    private boolean crashDetected;
    private double sparseness;
    private HashMap<String, String> statesMap;
    private HashMap<String, Integer> featureVector;
    private Optional<Integer> desiredSize = Optional.none();


    public TestCase(String id){
        setId(id);
        crashDetected = false;
        visitedActivities = new HashSet<>();
        visitedStates = new HashSet<>();
        eventSequence = new ArrayList<>();
        sparseness = 0;
        statesMap = new HashMap<>();
        featureVector = new HashMap<String, Integer>();

    }

    public void setDesiredSize(Optional<Integer> desiredSize) {
        this.desiredSize = desiredSize;
    }

    public Optional<Integer> getDesiredSize() {
        return desiredSize;
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public void addEvent(Action event){this.eventSequence.add(event);};

    public void updateVisitedActivities(String activity){this.visitedActivities.add(activity);};

    public Set<String> getVisitedActivities() {
        return visitedActivities;
    }

    public void updateVisitedStates(IScreenState GUIState){this.visitedStates.add(GUIState.getId());};

    public Set<String> getVisitedStates() {
        return visitedStates;
    }

    public List<Action> getEventSequence(){return this.eventSequence;};

    public boolean getCrashDetected(){return this.crashDetected;};

    public void setCrashDetected(){this.crashDetected=true;};

    public void setNovelty(float novelty) {
        this.novelty = novelty;
    }

    public float getNovelty() {
        return novelty;
    }

    public double getSparseness() {
        return sparseness;
    }

    public void setSparseness(double sparseness) {
        this.sparseness = sparseness;
    }

    public void updateStatesMap(String state, String event) {
        if (!statesMap.containsKey(state)){
            statesMap.put(state, event);
            //MATE.log_acc("TEST___added to states map the state: "+state+" at event: "+event);
        }
    }
    public HashMap<String, String> getStatesMap() {
        return statesMap;
    }

    public HashMap<String, Integer> getFeatureVector() {
        return featureVector;
    }

    public void updateFeatureVector(IGUIModel guiModel) {
        List<IScreenState> guiStates = guiModel.getStates();
        for(IScreenState state : guiStates){
            if(this.visitedStates.contains(state.getId())){
                featureVector.put(state.getId(),1);
            } else {
                featureVector.put(state.getId(),0);
            }
        }
    }

    public static TestCase newDummy() {
        return new TestCase("dummy");
    }

    //TODO: Load test case from cache if it was executed before
    public static TestCase fromDummy(TestCase testCase) {
        MATE.uiAbstractionLayer.resetApp();
        TestCase resultingTc = newInitializedTestCase();

        int finalSize = testCase.eventSequence.size();

        if (testCase.desiredSize.hasValue()) {
            finalSize = testCase.desiredSize.getValue();
        }

        int count = 0;
        for (Action action0 : testCase.eventSequence) {
            if (count < finalSize) {
                if (!(action0 instanceof WidgetAction) || MATE.uiAbstractionLayer.getExecutableActions().contains(action0)) {
                    if (!resultingTc.updateTestCase(action0, String.valueOf(count))) {
                        return resultingTc;
                    }
                    count++;
                } else {
                    break;
                }
            } else {
                return resultingTc;
            }
        }
        for (; count < finalSize; count++) {
            Action action;
            if (Properties.WIDGET_BASED_ACTIONS) {
                action = Randomness.randomElement(MATE.uiAbstractionLayer.getExecutableActions());
            } else {
                action = PrimitiveAction.randomAction();
            }
            if(!resultingTc.updateTestCase(action, String.valueOf(count))) {
                return resultingTc;
            }
        }

        return resultingTc;
    }

    /**
     * Initializes
     * @return
     */
    public static TestCase newInitializedTestCase() {
        TestCase tc = new TestCase(UUID.randomUUID().toString());
        tc.updateTestCase("init");
        return tc;
    }

    /**
     * Perform action and update TestCase accordingly.
     * @param a Action to perform
     * @param event Event name
     * @return True if action successful inbound false if outbound, crash, or some unkown failure
     */
    public boolean updateTestCase(Action a, String event) {
        if (a instanceof WidgetAction
                && !MATE.uiAbstractionLayer.getExecutableActions().contains(a)) {
            throw new IllegalStateException("Action not applicable to current state");
        }
        addEvent(a);
        UIAbstractionLayer.ActionResult actionResult = MATE.uiAbstractionLayer.executeAction(a);

        switch (actionResult) {
            case SUCCESS:
            case SUCCESS_NEW_STATE:
                updateTestCase(event);
                return true;
            case FAILURE_APP_CRASH:
                setCrashDetected();
            case SUCCESS_OUTBOUND:
                return false;
            case FAILURE_UNKNOWN:
            case FAILURE_EMULATOR_CRASH:
                return false;
            default:
                throw new UnsupportedOperationException("Encountered an unknown action result. Cannot continue.");
        }
    }

    private void updateTestCase(String event) {
        IScreenState currentScreenstate = MATE.uiAbstractionLayer.getLastScreenState();

        updateVisitedStates(currentScreenstate);
        updateVisitedActivities(currentScreenstate.getActivityName());
        updateStatesMap(currentScreenstate.getId(), event);
    }
}
