package org.mate.model.fsm.sosm.novelty;

import org.mate.model.fsm.sosm.Trace;

import static java.util.Objects.requireNonNull;

/**
 * This class extends the basic (SOSM) novelty estimator by dividing the achieved novelty by the
 * trace length (number of actions) of the test to avoid promoting long tests or tests with cycles.
 */
public final class DiscountedSumNoveltyEstimator implements NoveltyEstimator {

    /**
     * The core novelty estimator function.
     */
    private final NoveltyEstimator estimator;

    public DiscountedSumNoveltyEstimator(final NoveltyEstimator estimator) {
        this.estimator = requireNonNull(estimator);
    }

    /**
     * Divides the achieved novelty by the trace size, i.e. the number of actions/transitions.
     *
     * @param trace Describes the taken transitions by the test case.
     * @return Returns the estimated novelty.
     */
    public double estimateNovelty(final Trace trace) {
        return estimator.estimateNovelty(trace) / trace.size();
    }
}

