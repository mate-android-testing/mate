package org.mate.model.fsm.sosm.novelty;

import org.mate.model.fsm.sosm.Trace;

/**
 * Functional interface for classes that estimate the novelty of a trace using subjective logic.
 */
public interface INoveltyEstimator {

    /**
     * Estimates the novelty of a given test case.
     *
     * @param trace Describes the taken transitions by the test case.
     * @return Returns the novelty of the test case.
     */
    double estimateNovelty(final Trace trace);
}