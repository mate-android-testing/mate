package org.mate.exploration.genetic.fitness;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.model.fsm.sosm.novelty.MaxNovelSubsequence;
import org.mate.model.fsm.sosm.novelty.NoveltyEstimator;
import org.mate.utils.coverage.CoverageUtils;

/**
 * A novelty fitness function that combines novelty and coverage in a weighted fashion.
 */
public final class SOSMNoveltyWithCoverageFitnessFunction implements ISOSMNoveltyFitnessFunction {

    /**
     * The underlying SOSM model.
     */
    private final SOSMModel sosmModel = (SOSMModel) Registry.getUiAbstractionLayer().getGuiModel();

    /**
     * The currently employed novelty estimator function.
     */
    private final NoveltyEstimator noveltyEstimator;

    /**
     * A weight factor that controls the influence of novelty and coverage for the fitness value.
     */
    private final double weight = Properties.NOVELTY_AND_COVERAGE_COMBINATION_WEIGHT();

    /**
     * Initialises the novelty fitness function.
     */
    public SOSMNoveltyWithCoverageFitnessFunction() {
        final double uncertaintyThreshold = Properties.SOSM_NOVELTY_DISBELIEF_WEIGHT();
        noveltyEstimator = new MaxNovelSubsequence(sosmModel, uncertaintyThreshold);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNovelty(final IChromosome<TestCase> chromosome, final Trace trace) {

        if (chromosome.getValue().reachedNewState()) {
            // Reaching a new state represents something extremely novel.
            return 1.0;
        } else {
            // combine coverage and novelty
            final double novelty = noveltyEstimator.estimateNovelty(trace);
            final double coverage = CoverageUtils.getCoverage(Properties.COVERAGE(), chromosome)
                    .getCoverage(Properties.COVERAGE()) / 100;
            return weight * novelty + (1.0 - weight) * coverage;
        }
    }
}

