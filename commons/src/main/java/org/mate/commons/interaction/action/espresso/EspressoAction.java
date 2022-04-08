package org.mate.commons.interaction.action.espresso;

import androidx.annotation.NonNull;

import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;

/**
 * An Espresso action is composed of a ViewMatcher (that tells Espresso which is the target view)
 * and a ViewAction (that tells Espresso what action to perform on the target view).
 */
public class EspressoAction extends Action {

    private final EspressoViewAction viewAction;
    private final EspressoViewMatcher viewMatcher;

    public EspressoAction(EspressoViewAction viewAction, EspressoViewMatcher viewMatcher) {
        this.viewAction = viewAction;
        this.viewMatcher = viewMatcher;
    }

    /**
     * Compares two Espresso actions for equality.
     *
     * @param o The object to which we compare.
     * @return Returns {@code true} if both actions are equal,
     * otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            EspressoAction other = (EspressoAction) o;
            return viewAction == other.viewAction && viewMatcher.equals(other.viewMatcher);
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the Espresso action.
     */
    @Override
    public int hashCode() {
        return viewAction.hashCode() + viewMatcher.hashCode();
    }

    /**
     * The string representation of the Espresso action.
     */
    @NonNull
    @Override
    public String toString() {
        return String.format("onView(%s).perform(%s)",
                viewMatcher.toString(), viewAction.toString());
    }

    @NonNull
    @Override
    public String toShortString() {
        return toString();
    }

    @Override
    public int getIntForActionSubClass() {
        return ACTION_SUBCLASS_ESPRESSO;
    }
}
