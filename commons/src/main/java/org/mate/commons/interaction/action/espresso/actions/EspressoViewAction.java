package org.mate.commons.interaction.action.espresso.actions;

public class EspressoViewAction {
    private EspressoViewActionType type;

    public EspressoViewAction(EspressoViewActionType type) {
        this.type = type;
    }

    public EspressoViewActionType getType() {
        return type;
    }
}
