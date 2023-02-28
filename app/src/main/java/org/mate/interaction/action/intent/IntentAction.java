package org.mate.interaction.action.intent;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mate.interaction.action.Action;
import org.mate.utils.manifest.element.ComponentDescription;
import org.mate.utils.manifest.element.IntentFilterDescription;

/**
 * An abstract action that is backed up by an intent.
 */
public abstract class IntentAction extends Action {

    /**
     * The intent receiving component.
     */
    protected final ComponentDescription component;

    /**
     * The intent filter on which the intent is based.
     */
    protected final IntentFilterDescription intentFilter;

    /**
     * Constructs a new intent action for the given component and based on the given intent filter.
     *
     * @param component The intent receiving component.
     * @param intentFilter The intent filter on which the intent is based.
     */
    public IntentAction(ComponentDescription component, IntentFilterDescription intentFilter) {
        this.component = component;
        this.intentFilter = intentFilter;
    }

    public ComponentDescription getComponent() {
        return component;
    }

    public IntentFilterDescription getIntentFilter() {
        return intentFilter;
    }

    @NonNull
    @Override
    public abstract String toString();

    @NonNull
    @Override
    public abstract String toShortString();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object o);
}
