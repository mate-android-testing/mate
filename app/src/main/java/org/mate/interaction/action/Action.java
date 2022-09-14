package org.mate.interaction.action;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Action {

    /**
     * A detailed description of the action. Primarily used
     * for the analysis framework.
     *
     * @return Returns a detailed string representation.
     */
    @NonNull
    public abstract String toString();

    /**
     * A simplified description of the action. Primarily used
     * for the gui model.
     *
     * @return Returns a simplified string representation.
     */
    @NonNull
    public abstract String toShortString();

    public abstract int hashCode();

    public abstract boolean equals(@Nullable Object o);
}
