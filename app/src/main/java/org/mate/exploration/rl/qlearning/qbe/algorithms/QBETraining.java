package org.mate.exploration.rl.qlearning.qbe.algorithms;

import org.mate.MATE;
import org.mate.exploration.genetic.chromosome_factory.AndroidRandomChromosomeFactory;

/**
 * Explores the AUT guided by the specified exploration strategy and records a model in the form of
 * a {@link org.mate.exploration.qlearning.qbe.transition_system.TransitionSystem}. From a set of
 * such models a final {@link org.mate.exploration.qlearning.qbe.qmatrix.QMatrix} is learned.
 */
public class QBETraining extends QBEAbstractAlgorithm {

    private final AndroidRandomChromosomeFactory chromosomeFactory;

    public QBETraining(final long timeoutInMilliseconds, final int maxNumEvents) {
        super(timeoutInMilliseconds);
        chromosomeFactory = new AndroidRandomChromosomeFactory(false, maxNumEvents);
    }

    @Override
    public void run() {

        final long startTime = System.currentTimeMillis();

        MATE.log_acc("Running QBE Training until timeout is reached...");

        while (!reachedTimeout(startTime)) {
            MATE.log_acc("Sampling new test case...");
            chromosomeFactory.createChromosome();
        }

    }
}
