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
 * Computes n binomial opinions by multiplying the first n coarsend binomial opnions, and uses the
 * binomial opinion that leads to the largest novetly score.
 */
public final class MaxNovelPrefix implements NoveltyEstimator {

    private final SOSMModel sosmModel;
    private final double alpha;

    public MaxNovelPrefix(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

    @Override
    public double estimateNovelty(final Trace trace) {

        final List<BinomialOpinion> opinions = sosmModel.getCoarsenedBinomialOpinionsFor(trace);

        if (opinions.isEmpty()) {
            return 0.0;
        }

        RawBinomialOpinion opinion = BinomialOpinion.multiply(opinions.subList(0, Math.min(opinions.size(), 5))).getRawOpinion();
        RawBinomialOpinion bestOpinion = opinion;
        double bestScore = opinion.getDisbelief() * alpha + opinion.getUncertainty();
        int bestLength = 5; // TODO: Use constant 'MIN_LENGTH'.

        for (int i = 5; i < opinions.size(); ++i) {
            opinion = opinion.multiply(opinions.get(i).getRawOpinion());
            final double score = opinion.getDisbelief() * alpha + opinion.getUncertainty();
            if (score > bestScore) {
                bestScore = score;
                bestOpinion = opinion;
                bestLength = i + 1;
            }
        }

        MATE.log_debug(String.format("Opinion on trace: (%f, %f, %f, %f)", bestOpinion.getBelief(), bestOpinion.getDisbelief(), bestOpinion.getUncertainty(), bestOpinion.getApriori()));
        MATE.log_debug(String.format("Best opinion length: %d", bestLength));
        return bestScore;
    }
}


