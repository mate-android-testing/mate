package org.mate.model.fsm.sosm.novelty;

import org.mate.model.fsm.sosm.SOSMModel;

import static java.util.Objects.requireNonNull;

/**
 * A factory for initialising a novelty estimator function.
 */
public final class NoveltyEstimatorFactory {

    /**
     * The underlying SOSM model.
     */
    private final SOSMModel sosmModel;

    /**
     * A weight factor.
     */
    private final double alpha;

    /**
     * Initialises the novelty estimator factory with the necessary SOSM model and the weight factor
     * alpha.
     *
     * @param sosmModel The SOSM model.
     * @param alpha The weight factor alpha.
     */
    public NoveltyEstimatorFactory(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

    /**
     * Instantiates a novelty estimator function based on the given property.
     *
     * @param noveltyEstimator The novelty estimator that should be instantiated.
     * @return Returns the instantiated novelty estimator function.
     */
    public INoveltyEstimator getNoveltyEstimator(final NoveltyEstimator noveltyEstimator) {
        switch (noveltyEstimator) {
            case AVERAGE_SL_NOVELTY:
                return new AverageSLNovelty(sosmModel, alpha);
            case DISCOUNTED_SUM_NOVELTY:
                // TODO: Avoid hardcoding inner novelty estimator function.
                return new DiscountedSumNoveltyEstimator(new MaxNovelSubsequence(sosmModel, alpha));
            case MAX_NOVEL_PREFIX:
                return new MaxNovelPrefix(sosmModel, alpha);
            case MAX_NOVEL_SUBSEQUENCE:
                return new MaxNovelSubsequence(sosmModel, alpha);
            case MULTI_SOURCE_FUSION:
                return new MultiSourceFusionEstimator(sosmModel, alpha);
            case SOSM_NOVELTY:
                return new SOSMNovelty(sosmModel, alpha);
            default:
                throw new UnsupportedOperationException("Novelty Estimator Function "
                        + noveltyEstimator + " not yet supported!");
        }
    }
}
