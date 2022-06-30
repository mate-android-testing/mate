package org.mate.state.equivalence.checks;

import org.mate.state.IScreenState;
import org.mate.state.equivalence.IStateEquivalence;
import org.mate.state.equivalence.StateEquivalenceLevel;

import java.util.Objects;

/**
 * Compares two {@link IScreenState}s for equality based on the number of widgets and its positions.
 * This corresponds to the equivalence level
 * {@link StateEquivalenceLevel#WIDGET}.
 */
public class WidgetEquivalence implements IStateEquivalence {

    @Override
    public boolean checkEquivalence(IScreenState first, IScreenState second) {

        Objects.requireNonNull(first, "First screen state must be not null!");
        Objects.requireNonNull(second, "Second screen state must be not null!");

        if (first == second) {
            return true;
        }

        return Objects.equals(first.getPackageName(), second.getPackageName())
                && Objects.equals(first.getActivityName(), second.getActivityName())
                && Objects.equals(first.getWidgets(), second.getWidgets());
    }
}

