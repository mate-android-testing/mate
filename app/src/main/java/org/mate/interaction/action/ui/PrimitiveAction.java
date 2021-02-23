package org.mate.interaction.action.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mate.MATE;
import org.mate.interaction.action.Action;
import org.mate.utils.Randomness;

import java.util.Arrays;
import java.util.Objects;

public class PrimitiveAction extends Action {

    private final ActionType actionType;
    private final int x, y;

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

    /**
     * Generates a random primitive action, i.e. a random action on some random coordinates.
     *
     * @return Returns a randomly generated primitive action.
     */
    public static PrimitiveAction randomAction() {
        int x = Randomness.getRnd().nextInt(MATE.device.getDisplayWidth());
        int y = Randomness.getRnd().nextInt(MATE.device.getDisplayHeight());
        return new PrimitiveAction(x, y, Randomness.randomElement(Arrays.asList(ActionType.primitiveActionTypes)));
    }

    /**
     * Compares two primitive actions for equality.
     *
     * @param o The object to which we compare.
     * @return Returns {@code true} if both actions are equal,
     *          otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            PrimitiveAction other = (PrimitiveAction) o;
            return this.x == other.x && this.y == other.y && actionType == other.actionType;
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the primitive action.
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y, actionType);
    }

    /**
     * The string representation used in combination with analysis framework.
     *
     * @return Returns the string representation of a primitive action.
     */
    @NonNull
    @Override
    public String toString() {
        return "primitive action: " + actionType + " at (" + x + "," + y + ")";
    }

    /**
     * A simplified textual representation used for the {@link org.mate.model.IGUIModel}.
     *
     * @return Returns a simplified string representation.
     */
    @NonNull
    @Override
    public String toShortString() {
        return actionType + " at (" + x + "," + y + ")";
    }
}
