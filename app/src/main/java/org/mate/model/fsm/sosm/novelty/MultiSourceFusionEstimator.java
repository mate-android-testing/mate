package org.mate.model.fsm.sosm.novelty;


import org.mate.MATE;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.model.fsm.sosm.subjective_logic.BinomialOpinion;
import org.mate.model.fsm.sosm.subjective_logic.RawBinomialOpinion;

import java.util.List;

import static java.util.Objects.requireNonNull;

// weighted average fusion (WAF)
public final class MultiSourceFusionEstimator implements NoveltyEstimator {

    private final SOSMModel sosmModel;
    private final double alpha;

    public MultiSourceFusionEstimator(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

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

