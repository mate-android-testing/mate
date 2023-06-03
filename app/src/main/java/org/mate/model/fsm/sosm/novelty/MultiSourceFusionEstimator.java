package org.mate.model.fsm.sosm.novelty;


import android.annotation.SuppressLint;

import org.mate.MATE;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.model.fsm.sosm.subjective_logic.BinomialOpinion;
import org.mate.model.fsm.sosm.subjective_logic.RawBinomialOpinion;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A novelty estimator that estimates novelty by combing all coarsened binomial opinions of a trace
 * into a single binomial opinion.
 *
 */
public final class MultiSourceFusionEstimator implements INoveltyEstimator {

    /**
     * The underlying SOSM model.
     */
    private final SOSMModel sosmModel;

    /**
     * A weight factor.
     */
    private final double alpha;

    public MultiSourceFusionEstimator(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

    /**
     * The novelty is computed by taking the weighted average fusion (WAF) of all coarsened binomial
     * opinions.
     *
     * @param trace Describes the taken transitions by the test case.
     * @return Returns the estimated novelty.
     */
    @SuppressLint("DefaultLocale")
    @Override
    public double estimateNovelty(Trace trace) {
        final List<BinomialOpinion> opinions = sosmModel.getCoarsenedBinomialOpinionsFor(trace);
        final BinomialOpinion combined = BinomialOpinion.multiSourceFusion(opinions);
        final RawBinomialOpinion raw = combined.getRawOpinion();
        MATE.log_debug(String.format("Opinion on trace: (%f, %f, %f, %f)", raw.getBelief(),
                raw.getDisbelief(), raw.getUncertainty(), raw.getApriori()));
        return raw.getDisbelief() * alpha + raw.getUncertainty();
    }
}

