package org.mate.model.fsm.sosm.novelty;

import org.mate.MATE;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.model.fsm.sosm.subjective_logic.BinomialOpinion;
import org.mate.model.fsm.sosm.subjective_logic.RawBinomialOpinion;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A novelty estimator that estimates novelty by combing all coarsened binomial opinions of a trace
 * into single binomial opinion on that trace.
 *
 * The binomial opinion is computed by multiplying all coarsened binomial opinions.
 */
public final class SOSMNovelty implements NoveltyEstimator {

    private final SOSMModel sosmModel;
    private final double alpha;

    public SOSMNovelty(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

    @Override
    public double estimateNovelty(final Trace trace) {
        final List<BinomialOpinion> opinions = sosmModel.getCoarsenedBinomialOpinionsFor(trace);
        final BinomialOpinion combined = BinomialOpinion.multiply(opinions);
        final RawBinomialOpinion raw = combined.getRawOpinion();
        MATE.log_debug(String.format("Opinion on trace: (%f, %f, %f, %f)", raw.getBelief(),
                raw.getDisbelief(), raw.getUncertainty(), raw.getApriori()));
        return raw.getDisbelief() * alpha + raw.getUncertainty();
    }
}

