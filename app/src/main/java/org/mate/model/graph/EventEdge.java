package org.mate.model.graph;

import org.mate.ui.Action;
import org.mate.ui.WidgetAction;

/**
 * Created by marceloe on 08/12/16.
 */
public class EventEdge implements Cloneable{

    private String id;
    private WidgetAction event;
    private ScreenNode source;
    private ScreenNode target;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventEdge eventEdge = (EventEdge) o;

        if (id != null ? !id.equals(eventEdge.id) : eventEdge.id != null) return false;
        return true;

    }

    private float pheromone;

    private float fitness;
    private Integer weight;
    private boolean isUpdatedPheromone = false;

    public EventEdge(){}

    public EventEdge(ScreenNode source, ScreenNode target, WidgetAction event) {
        setSource(source);
        setTarget(target);
        setEvent(event);
        //TODO:a tiny button here, back button doesn't have id, so null pointer exeception
//        setId(source.getId()+"-"+this.event.getActionType()+":"+this.event.getWidget().getId()+"-"+target.getId());
        setId(source.getId()+"-"+this.event.getActionType()+":"+"-"+target.getId());
    }

    /**
     * TODO - Change the parameter if necessary
     * @param source
     * @param eventAction
     */
    public EventEdge(ScreenNode source, WidgetAction eventAction) {
        setSource(source);
        if(eventAction.isNewStateGenerated()) {
            setTarget(new ScreenNode(eventAction.getAdjScreen().getId(), eventAction.getAdjScreen()));
        }
        setEvent(eventAction);
        //TODO:a tiny button here, back button doesn't have id, so null pointer exeception
//        setId(source.getId()+"-"+this.event.getActionType()+":"+this.event.getWidget().getId()+"-"+target.getId());
        setId(source.getId()+"-"+eventAction.getActionType()+":"+"-"+ ((target!=null && target.getId()!=null) ? target.getId():"(n/a)"));
        this.weight = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Action getEvent() {
        return event;
    }

    public WidgetAction getWidgetAction() {
        return event;
    }

    public void setEvent(WidgetAction event) {
        this.event = event;
    }

    public ScreenNode getSource() {
        return source;
    }

    public void setSource(ScreenNode source) {
        this.source = source;
    }

    public ScreenNode getTarget() {
        return target;
    }

    public void setTarget(ScreenNode target) {
        this.target = target;
    }


    public float getPheromone() {
        return pheromone;
    }

    public void setPheromone(float pheromone) {
        this.pheromone = pheromone;
        event.setPheromone(pheromone);
    }

    public float getFitness() {
        return fitness;
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
        event.setFitness(fitness);
    }

    public void increaseFitness(float fitness) {
        this.fitness = this.fitness + fitness;
        event.setFitness(fitness);
    }

    public void decreaseFitness(float fitness) {
        this.fitness = this.fitness - fitness;
        event.setFitness(fitness);
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
        event.setWeight(weight);
    }

    public void increaseWeight(int weight) {
        this.weight = this.weight + weight;
        event.setWeight(weight);
    }

    public void decreaseWeight(int weight) {
        this.weight = this.weight - weight;
        event.setWeight(weight);
    }

    public void plusQtdeOfExec(){
        event.plusQtdeOfExec();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }



}
