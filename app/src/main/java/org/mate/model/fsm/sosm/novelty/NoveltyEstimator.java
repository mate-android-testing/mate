package org.mate.model.fsm.sosm.novelty;

/**
 * The supported novelty estimator functions.
 */
public enum NoveltyEstimator {

    AVERAGE_SL_NOVELTY,
    DISCOUNTED_SUM_NOVELTY,
    MAX_NOVEL_PREFIX,
    MAX_NOVEL_SUBSEQUENCE,
    MULTI_SOURCE_FUSION,
    SOSM_NOVELTY;
}