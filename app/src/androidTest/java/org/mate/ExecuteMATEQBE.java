package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.qlearning.qbe.abstractions.action.QBEAction;
import org.mate.exploration.qlearning.qbe.abstractions.app.QBEApplication;
import org.mate.exploration.qlearning.qbe.abstractions.state.QBEState;
import org.mate.exploration.qlearning.qbe.algorithms.QBETesterFactory;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.exploration.QBE;
import org.mate.exploration.qlearning.qbe.exploration.RandomExploration;
import org.mate.exploration.qlearning.qbe.qmatrix.QBEMatrixFactory;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEQBE {

    private ExplorationStrategy<QBEState, QBEAction> getStrategy(final Strategy strategy) {
        final QBEMatrixFactory factory = new QBEMatrixFactory();

        switch (strategy) {
            case RANDOM:
                return new RandomExploration<>();
            case ACTIVITY_MATRIX:
                return new QBE<>(factory.getMaximizeActivityCoverageQMatrix());
            case CRASH_MATRIX:
                return new QBE<>(factory.getMaximizesNumberOfCrashesQMatrix());
            case CUSTOM_MATRIX:
                return new QBE<>(factory.getCustomNewCoverageMatrix());
            default:
                throw new AssertionError("unreachable");
        }
    }

    @Test
    public void useAppContext() {
        final String strategy = Properties.QBE_EXPLORATION_STRATEGY();
        final Strategy s = strategy != null ? Strategy.valueOf(strategy) : Strategy.RANDOM;

        MATE.log_acc(String.format("Starting QBE %s...", s));

        final MATE mate = new MATE();
        final QBEApplication app = new QBEApplication(Registry.getUiAbstractionLayer());
        final QBETesterFactory<QBEState, QBEAction> factory = new QBETesterFactory<>(app, getStrategy(s));
        mate.testApp(factory.get());
    }

    private enum Strategy {
        RANDOM,
        ACTIVITY_MATRIX,
        CRASH_MATRIX,
        CUSTOM_MATRIX,
    }
}
