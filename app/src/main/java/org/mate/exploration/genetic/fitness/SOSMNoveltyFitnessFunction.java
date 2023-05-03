package org.mate.exploration.genetic.fitness;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.fsm.sosm.SOSMModel;
import org.mate.model.fsm.sosm.Trace;
import org.mate.model.fsm.sosm.novelty.MultiSourceFusionEstimator;
import org.mate.model.fsm.sosm.novelty.NoveltyEstimator;

public class SOSMNoveltyFitnessFunction implements ISOSMNoveltyFitnessFunction {

    private final SOSMModel sosmModel = (SOSMModel) Registry.getUiAbstractionLayer().getGuiModel();

    private final NoveltyEstimator estimator;

    public SOSMNoveltyFitnessFunction() {
        final double uncertaintyThreshold = Properties.SOSM_NOVELTY_DISBELIEF_WEIGHT();
        estimator = new MultiSourceFusionEstimator(sosmModel, uncertaintyThreshold);
    }

    public double getNovelty(final IChromosome<TestCase> chromosome, final Trace trace) {

        if (chromosome.getValue().reachedNewState()) {
            return 1.0;
        }

        return estimator.estimateNovelty(trace);
    }
}

