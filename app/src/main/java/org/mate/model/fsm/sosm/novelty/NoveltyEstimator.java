package org.mate.model.fsm.sosm.novelty;

import org.mate.model.fsm.sosm.Trace;

/**
 * Functional intface for classes that estimate the novetly of a trace using Subjetive Logic.
 */
public interface NoveltyEstimator {

    /**
     * Return the novelty of the "test".
     *
     * @return Returns the novelty of the test.
     */
    double estimateNovelty(final Trace trace);
}