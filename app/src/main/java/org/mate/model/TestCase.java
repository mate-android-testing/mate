package org.mate.model;

import org.mate.state.IScreenState;
import org.mate.ui.Action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class TestCase {
    private String id;
    private Set<String> visitedActivities;
    private Set<String> visitedStates;
    private Vector<Action> eventSequence;
    private float novelty;
    private boolean crashDetected;
    private double sparseness;
    private HashMap<String, String> statesMap;
    private HashMap<String, Integer> featureVector;


    public TestCase(String id){
        setId(id);
        crashDetected = false;
        visitedActivities = new HashSet<>();
        visitedStates = new HashSet<>();
        eventSequence = new Vector<>();
        sparseness = 0;
        statesMap = new HashMap<>();
        featureVector = new HashMap<String, Integer>();

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

    public Vector<Action> getEventSequence(){return this.eventSequence;};

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
        Vector<IScreenState> guiStates = guiModel.getStates();
        for(IScreenState state : guiStates){
            if(this.visitedStates.contains(state.getId())){
                featureVector.put(state.getId(),1);
            } else {
                featureVector.put(state.getId(),0);
            }
        }
    }
}
