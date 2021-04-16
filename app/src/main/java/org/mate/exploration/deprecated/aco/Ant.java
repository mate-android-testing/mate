package org.mate.exploration.deprecated.aco;

import org.mate.model.deprecated.graph.EventEdge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by marceloeler on 22/06/17.
 */

@Deprecated
public class Ant {
    private List<EventEdge> traversedEventEdge;
    private List<EventEdge> benefitForFitnessEventEdge;
    private float antFitness;
    private EventEdge currentEventEdge;

    public Ant(){
        antFitness = 0;
        coveredActivity = new ArrayList<>();
        currentEventEdge = new EventEdge();
        traversedEventEdge = new ArrayList<>();
        benefitForFitnessEventEdge = new ArrayList<>();
    }


    public EventEdge getCurrentEventEdge() {
        return currentEventEdge;
    }

    public void setCurrentEventEdge(EventEdge currentEventEdge) {
        this.currentEventEdge = currentEventEdge;
    }

    private List<String> coveredActivity;

    public List<EventEdge> getTraversedEventEdge() {
        return traversedEventEdge;
    }

    public void setTraversedEventEdge(List<EventEdge> traversedEventEdge) {
        this.traversedEventEdge = traversedEventEdge;
    }

    public List<EventEdge> getBenefitForFitnessEventEdge() {
        return benefitForFitnessEventEdge;
    }

    public void setBenefitForFitnessEventEdge(List<EventEdge> benefitForFitnessEventEdge) {
        this.benefitForFitnessEventEdge = benefitForFitnessEventEdge;
    }

    public float getAntFitness() {
        return coveredActivity.size();
    }
    public List<String> getCoveredActivity() {
        return coveredActivity;
    }

    public void setCoveredActivity(List<String> coveredActivity) {
        this.coveredActivity = coveredActivity;
    }
    public void setAntFitness(float antFitness) {
        this.antFitness = antFitness;
    }

    @Override
    public String toString() {
        return "Ant{" +
                ", antFitness=" + antFitness +
                ", currentEventEdge=" + currentEventEdge +
                ", coveredActivity=" + coveredActivity +
                '}';
    }

    //array ants in one generation with decreasing order
    public Comparator<Ant> compareAnt() {
        Comparator<Ant> comparator = new Comparator<Ant>() {
            @Override
            public int compare(Ant o1, Ant o2) {
                if (o1.getAntFitness()<o2.getAntFitness()){
                    return 1;
                }else {
                    return -1;
                }
            }
        };
        return comparator;
    }
}
