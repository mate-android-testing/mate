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
 */
public final class MaxNovelSubsequence implements NoveltyEstimator {

    /**
     * The minimal length of considered coarsened binomial opinions.
     */
    private static final int MIN_LENGTH = 3;

    /**
     * The minimal fraction of coarsened binomial opinions that should be considered.
     */
    private final static double SEQUENCE_FRACTION = 0.2;

    /**
     * The underlying SOSM model.
     */
    private final SOSMModel sosmModel;

    /**
     * A weight factor.
     */
    private final double alpha;

    public MaxNovelSubsequence(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

    /**
     * Computes many binomial opinions by computing all subsequences of coarsened binomial opinions
     * that have a length of at least {@link #MIN_LENGTH} and a length of at least
     * {@link #SEQUENCE_FRACTION} of the total number of coarsened binomial opinions. The opinions
     * in each sequence are multiplied to get a candidate best opinion. For each candidate best
     * opinion the novelty is computed and the candidate best opinion that yields the highest novelty
     * is used.
     *
     * @param trace Describes the taken transitions by the test case.
     * @return Returns the estimated novelty.
     */
    @SuppressLint("DefaultLocale")
    @Override
    public double estimateNovelty(final Trace trace) {

        final List<BinomialOpinion> opinions = sosmModel.getCoarsenedBinomialOpinionsFor(trace);

        if (opinions.isEmpty()) {
            return 0.0;
        }

        final int size = opinions.size();
        final int minLength = Math.min(
                Math.max((int) Math.ceil(size * SEQUENCE_FRACTION), MIN_LENGTH), size);

        RawBinomialOpinion bestOpinion = null;
        double bestScore = -1.0;
        int bestLength = -1;
        int bestStart = -1;

        for (int i = 0; i <= size - minLength; ++i) {

            // Determine the max novel prefix.
            RawBinomialOpinion opinion
                    = BinomialOpinion.multiply(opinions.subList(i, i + minLength)).getRawOpinion();
            double score = alpha * opinion.getDisbelief() + opinion.getUncertainty();

            if (score > bestScore) {
                bestOpinion = opinion;
                bestLength = minLength;
                bestStart = i;
                bestScore = score;
            }

            // Determine whether multiplying with a further binomial opinion is beneficial.
            for (int j = i + minLength; j < opinions.size(); ++j) {

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

        MATE.log_debug(String.format("Opinion on trace: (%f, %f, %f, %f)", bestOpinion.getBelief(),
                bestOpinion.getDisbelief(), bestOpinion.getUncertainty(), bestOpinion.getApriori()));
        MATE.log_debug(String.format("Best score: %f, best start: %d, best len: %d", bestScore,
                bestStart, bestLength));
        return bestScore;
    }
}

