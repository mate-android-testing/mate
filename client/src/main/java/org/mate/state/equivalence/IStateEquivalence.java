package org.mate.state.equivalence;

import org.mate.state.IScreenState;

/**
 * Provides an interface for state equivalence checks between two {@link IScreenState}s.
 */
public interface IStateEquivalence {

    /**
     * Checks two {@link IScreenState}s for equivalence.
     *
     * @param first The first screen state.
     * @param second The second screen state.
     * @return Returns {@code true} if both {@link IScreenState}s are equal, otherwise {@code false}
     *         is returned.
     */
    boolean checkEquivalence(final IScreenState first, final IScreenState second);
}

