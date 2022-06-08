package org.mate.interaction.action;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Describes the virtual (re)-start action of the AUT.
 */
public final class StartAction extends Action {

    /**
     * Provides a textual representation of the virtual start action.
     *
     * @return Returns the string representation of the virtual start action.
     */
    @NonNull
    @Override
    public String toString() {
        return "START_ACTION";
    }

    /**
     * Another simple text representation used for the {@link org.mate.model.IGUIModel}.
     *
     * @return Returns a simplified string representation.
     */
    @NonNull
    @Override
    public String toShortString() {
        return "START_ACTION";
    }

    /**
     * Computes the hash code for the given action.
     *
     * @return Returns the hash code for the given action.
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Compares two {@link Action}s for equality.
     *
     * @param o The other action.
     * @return Returns {@code true} if both action represent a start action, otherwise {@code false}
     *         is returned.
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else {
            return o != null && getClass().equals(o.getClass());
        }
    }
}
