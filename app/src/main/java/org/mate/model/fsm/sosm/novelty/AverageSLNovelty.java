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
public final class AverageSLNovelty implements INoveltyEstimator {

    /**
     * The underlying SOSM model.
     */
    private final SOSMModel sosmModel;

    /**
     * A weight factor.
     */
    private final double alpha;

    public AverageSLNovelty(final SOSMModel sosmModel, final double alpha) {
        this.sosmModel = requireNonNull(sosmModel);
        this.alpha = alpha;
    }

    /**
     * Combines the binomial opinions by taking the average of belief, disbelief, uncertainty and
     * apriori belief.
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
        MATE.log_debug(String.format("Opinion on trace: (%f, %f, %f,%f)", belief, disbelief,
                uncertainty, score));
        return score;
    }
}

