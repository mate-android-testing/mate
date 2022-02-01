package org.mate.model.deprecated.graph;

import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.WidgetAction;

/**
 * Created by marceloe on 08/12/16.
 */
@Deprecated
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Action getEvent() {
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
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
