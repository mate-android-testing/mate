package org.mate.crash_reproduction.eda.representation.initializer;

import org.mate.interaction.action.ui.ActionType;

public class MoreRotationStoat extends StoatProbabilityInitialization {
    public MoreRotationStoat(double pPromisingAction) {
        super(pPromisingAction);
    }

    @Override
    protected double getEventTypeWeight(ActionType actionType) {
        if (actionType == ActionType.TOGGLE_ROTATION) {
            return 2;
        }
        return super.getEventTypeWeight(actionType);
    }
}
