package org.mate.state.equivalence;

import org.mate.state.equivalence.checks.ActivityNameEquivalence;
import org.mate.state.equivalence.checks.CosineSimilarity;
import org.mate.state.equivalence.checks.PackageNameEquivalence;
import org.mate.state.equivalence.checks.WidgetEquivalence;
import org.mate.state.equivalence.checks.WidgetWithAttributesEquivalence;

/**
 * A factory for retrieving the {@link IStateEquivalence} check based on a given
 * {@link StateEquivalenceLevel}.
 */
public class StateEquivalenceFactory {

    /**
     * Returns the state equivalence check based on the given {@link StateEquivalenceLevel}.
     *
     * @return Returns the state equivalence check matching the selected state equivalence level.
     */
    public static IStateEquivalence getStateEquivalenceCheck(StateEquivalenceLevel level) {

        switch (level) {
            case PACKAGE_NAME:
                return new PackageNameEquivalence();
            case ACTIVITY_NAME:
                return new ActivityNameEquivalence();
            case WIDGET:
                return new WidgetEquivalence();
            case WIDGET_WITH_ATTRIBUTES:
                return new WidgetWithAttributesEquivalence();
            case COSINE_SIMILARITY:
                return new CosineSimilarity();
            default:
                throw new UnsupportedOperationException("Unsupported state equivalence level: " + level);
        }
    }
}

