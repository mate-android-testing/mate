package org.mate.exploration.aco;

import org.mate.model.graph.EventEdge;

import java.util.Comparator;
import java.util.Vector;

/**
 * Created by marceloeler on 22/06/17.
 */

public class Ant {
    private Vector<EventEdge> traversedEventEdge;
    private Vector<EventEdge> benefitForFitnessEventEdge;
    private float antFitness;
    private EventEdge currentEventEdge;

    public Ant(){
        antFitness = 0;
        coveredActivity = new Vector<>();
        currentEventEdge = new EventEdge();
        traversedEventEdge = new Vector<>();
        benefitForFitnessEventEdge = new Vector<>();
    }


    public EventEdge getCurrentEventEdge() {
        return currentEventEdge;
    }

    public void setCurrentEventEdge(EventEdge currentEventEdge) {
        this.currentEventEdge = currentEventEdge;
    }

    private Vector<String> coveredActivity;

    public Vector<EventEdge> getTraversedEventEdge() {
        return traversedEventEdge;
    }

    public void setTraversedEventEdge(Vector<EventEdge> traversedEventEdge) {
        this.traversedEventEdge = traversedEventEdge;
    }

    public Vector<EventEdge> getBenefitForFitnessEventEdge() {
        return benefitForFitnessEventEdge;
    }

    public void setBenefitForFitnessEventEdge(Vector<EventEdge> benefitForFitnessEventEdge) {
        this.benefitForFitnessEventEdge = benefitForFitnessEventEdge;
    }

    public float getAntFitness() {
        return coveredActivity.size();
    }
    public Vector<String> getCoveredActivity() {
        return coveredActivity;
    }

    public void setCoveredActivity(Vector<String> coveredActivity) {
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
