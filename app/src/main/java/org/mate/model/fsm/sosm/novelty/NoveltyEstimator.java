package org.mate.model.fsm.sosm.novelty;

import org.mate.model.fsm.sosm.Trace;

public interface NoveltyEstimator {

    /**
     * Return the novelty of the "test".
     *
     * @return Returns the novelty of the test.
     */
    double estimateNovelty(final Trace trace);
}