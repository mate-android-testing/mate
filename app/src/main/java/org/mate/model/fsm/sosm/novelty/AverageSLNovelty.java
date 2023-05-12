package org.mate.model.fsm.sosm.novelty;

import org.mate.MATE;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.model.fsm.sosm.subjective_logic.BinomialOpinion;
import org.mate.model.fsm.sosm.subjective_logic.RawBinomialOpinion;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A novelty estimator that estimates novelty by combing all coarsend binomial opinions of a trace
 * into single binomial opinion on that trace.
 *
 * Combines the binomial opinions by taking the average of belief, disbelief, uncertainty and
 * apriori.
 */
public final class AverageSLNovelty implements NoveltyEstimator {

    private final SOSMModel sosmModel;
    private final double alpha;

    public AverageSLNovelty(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

    @Override
    public double estimateNovelty(final Trace trace) {

        final List<BinomialOpinion> opinions = sosmModel.getCoarsenedBinomialOpinionsFor(trace);

        if (opinions.isEmpty()) {
            return 0.0;
        }

        double belief = 0.0;
        double disbelief = 0.0;
        double uncertainty = 0.0;

        for (final BinomialOpinion opinion : opinions) {
            final RawBinomialOpinion raw = opinion.getRawOpinion();
            belief += raw.getBelief();
            disbelief += raw.getDisbelief();
            uncertainty += raw.getUncertainty();
        }

        final double size = opinions.size();
        belief /= size;
        disbelief /= size;
        uncertainty /= size;

        final double score = alpha * disbelief + uncertainty;
        MATE.log_debug(String.format("Opinion on trace: (%f, %f, %f,%f)", belief, disbelief, uncertainty, score));
        return score;
    }
}

