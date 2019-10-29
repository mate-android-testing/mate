package org.mate.ui;

import org.mate.MATE;
import org.mate.utils.Randomness;

import java.util.Arrays;

public class PrimitiveAction extends Action {
    ActionType actionType;
    private int x, y;

    public PrimitiveAction(int x, int y, ActionType actionType) {
        this.x = x;
        this.y = y;
        this.actionType = actionType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public static PrimitiveAction randomAction() {
        int x = Randomness.getRnd().nextInt(MATE.device.getDisplayWidth());
        int y = Randomness.getRnd().nextInt(MATE.device.getDisplayHeight());
        return new PrimitiveAction(x, y, Randomness.randomElement(Arrays.asList(ActionType.primitiveActionTypes)));
    }
}
