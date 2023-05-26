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
public final class MaxNovelPrefix implements INoveltyEstimator {

    /**
     * The minimal length of considered binomial opinions.
     */
    private static final int MIN_LENGTH = 5;

    /**
     * The underlying SOSM model.
     */
    private final SOSMModel sosmModel;

    /**
     * A weight factor.
     */
    private final double alpha;

    public MaxNovelPrefix(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

    /**
     * Computes the novelty by multiplying at least the first {@link #MIN_LENGTH} coarsened binomial
     * opinions, and uses the binomial opinion that leads to the largest novelty score.
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

        // Consider the first n binomial opinions at least.
        RawBinomialOpinion opinion = BinomialOpinion.multiply(
                opinions.subList(0, Math.min(opinions.size(), MIN_LENGTH))).getRawOpinion();
        RawBinomialOpinion bestOpinion = opinion;
        double bestScore = opinion.getDisbelief() * alpha + opinion.getUncertainty();
        int bestLength = MIN_LENGTH;

        // Check if the multiplication of another binomial opinion leads to a better score/novelty.
        for (int i = MIN_LENGTH; i < opinions.size(); ++i) {
            opinion = opinion.multiply(opinions.get(i).getRawOpinion());
            final double score = opinion.getDisbelief() * alpha + opinion.getUncertainty();
            if (score > bestScore) {
                bestScore = score;
                bestOpinion = opinion;
                bestLength = i + 1;
            }
        }

        MATE.log_debug(String.format("Opinion on trace: (%f, %f, %f, %f)", bestOpinion.getBelief(),
                bestOpinion.getDisbelief(), bestOpinion.getUncertainty(), bestOpinion.getApriori()));
        MATE.log_debug(String.format("Best opinion length: %d", bestLength));
        return bestScore;
    }
}


