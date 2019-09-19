package org.mate.ui;

import org.mate.MATE;
import org.mate.utils.Randomness;

public class PrimitiveAction extends Action {
    private int x, y;

    public PrimitiveAction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static PrimitiveAction randomAction() {
        int x = Randomness.getRnd().nextInt(MATE.device.getDisplayWidth());
        int y = Randomness.getRnd().nextInt(MATE.device.getDisplayHeight());
        return new PrimitiveAction(x, y);
    }
}
