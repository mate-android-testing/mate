package org.mate.model.fsm.sosm.novelty;

import org.mate.model.fsm.sosm.Trace;

import static java.util.Objects.requireNonNull;

/**
 * This class extends the basic (SOSM) novelty estimator by discounting (normalizing/dividing) the
 * achieved novelty by the length (number of actions) of the test to avoid promoting long tests or
 * tests with cycles.
 */
public final class DiscountedSumNoveltyEstimator implements NoveltyEstimator {

    private final NoveltyEstimator estimator;

    public DiscountedSumNoveltyEstimator(final NoveltyEstimator estimator) {
        this.estimator = requireNonNull(estimator);
    }

    public double estimateNovelty(final Trace trace) {
        return estimator.estimateNovelty(trace) / trace.size();
    }
}

