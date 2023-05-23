package org.mate.exploration.genetic.fitness;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.model.fsm.sosm.novelty.MultiSourceFusionEstimator;
import org.mate.model.fsm.sosm.novelty.NoveltyEstimator;

/**
 * A novelty fitness function that queries the SOSM model for determining the novelty of a test
 * case chromosome.
 */
public class SOSMNoveltyFitnessFunction implements ISOSMNoveltyFitnessFunction {

    /**
     * The underlying SOSM model.
     */
    private final SOSMModel sosmModel = (SOSMModel) Registry.getUiAbstractionLayer().getGuiModel();

    /**
     * The currently employed novelty estimator function.
     */
    private final NoveltyEstimator estimator;

    /**
     * Initialises the SOSM-based novelty fitness function.
     */
    public SOSMNoveltyFitnessFunction() {
        final double uncertaintyThreshold = Properties.SOSM_NOVELTY_DISBELIEF_WEIGHT();
        estimator = new MultiSourceFusionEstimator(sosmModel, uncertaintyThreshold);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getNovelty(final IChromosome<TestCase> chromosome, final Trace trace) {

        if (chromosome.getValue().reachedNewState()) {
            // Reaching a new state represents something extremely novel.
            return 1.0;
        }

        return estimator.estimateNovelty(trace);
    }
}

