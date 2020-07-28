package org.mate.state.executables;

import org.mate.state.IScreenState;

public class RelatedState {

    private IScreenState state;
    private String differences;

    public RelatedState(IScreenState state, String differences){
        this.state = state;
        this.differences = differences;
    }


    public IScreenState getState() {
        return state;
    }

    public void setState(IScreenState state) {
        this.state = state;
    }

    public String getDifferences() {
        return differences;
    }

    public void setDifferences(String differences) {
        this.differences = differences;
    }
}
