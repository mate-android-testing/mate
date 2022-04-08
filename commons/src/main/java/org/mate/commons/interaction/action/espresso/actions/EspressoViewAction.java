package org.mate.commons.interaction.action.espresso.actions;

import androidx.test.espresso.ViewAction;

import org.mate.commons.interaction.action.espresso.EspressoCodeProducer;

public abstract class EspressoViewAction extends EspressoCodeProducer {
    private EspressoViewActionType type;

    public EspressoViewAction(EspressoViewActionType type) {
        this.type = type;
    }

    public EspressoViewActionType getType() {
        return type;
    }

    public abstract ViewAction getViewAction();
}
