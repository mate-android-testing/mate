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
 * Computes many binomial opinions by computing all subsquence of coarsened binomial opinions that
 * have a length of at least 3 and a length of at least 20% of the total number of coarsened
 * binomial opinions. The opinions in each sequence are multiplied to get a candidate best opinion.
 * For each candidate best opinion the novetlty is computed and the candidate best opinion that
 * yields the highest is used.
 */
public final class MaxNovelSubsequence implements NoveltyEstimator {

    private final static double SEQUENCE_FRACTION = 0.2;

    private final SOSMModel sosmModel;
    private final double alpha;

    public MaxNovelSubsequence(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

    @Override
    public double estimateNovelty(final Trace trace) {

        final List<BinomialOpinion> opinions = sosmModel.getCoarsenedBinomialOpinionsFor(trace);

        if (opinions.isEmpty()) {
            return 0.0;
        }

        final int size = opinions.size();
        final int min_length = Math.min(Math.max((int) Math.ceil(size * SEQUENCE_FRACTION), 3), size);

        RawBinomialOpinion bestOpinion = null;
        double bestScore = -1.0;
        int bestLength = -1;
        int bestStart = -1;

        for (int i = 0; i <= size - min_length; ++i) {
            RawBinomialOpinion opinion
                    = BinomialOpinion.multiply(opinions.subList(i, i + min_length)).getRawOpinion();
            double score = alpha * opinion.getDisbelief() + opinion.getUncertainty();

            if (score > bestScore) {
                bestOpinion = opinion;
                bestLength = min_length;
                bestStart = i;
                bestScore = score;
            }

            for (int j = i + min_length; j < opinions.size(); ++j) {
                opinion = opinion.multiply(opinions.get(j).getRawOpinion());
                score = alpha * opinion.getDisbelief() + opinion.getUncertainty();

                if (score > bestScore) {
                    bestOpinion = opinion;
                    bestLength = j - i;
                    bestStart = i;
                    bestScore = score;
                }

            }
        }

        assert bestOpinion != null;
        MATE.log_debug(String.format("Opinion on trace: (%f, %f, %f, %f)", bestOpinion.getBelief(), bestOpinion.getDisbelief(), bestOpinion.getUncertainty(), bestOpinion.getApriori()));
        MATE.log_debug(String.format("Best score: %f, best start: %d, best len: %d", bestScore, bestStart, bestLength));
        return bestScore;
    }
}

