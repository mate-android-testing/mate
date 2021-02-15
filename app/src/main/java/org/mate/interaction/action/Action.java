package org.mate.interaction.action;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class Action {

    @NonNull
    public abstract String toString();

    public abstract int hashCode();

    public abstract boolean equals(@Nullable Object o);
}
