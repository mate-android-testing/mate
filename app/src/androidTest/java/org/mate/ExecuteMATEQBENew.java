package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.rl.qlearning.qbe.QBE;
import org.mate.exploration.rl.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.rl.qlearning.qbe.exploration.QMMatrixBasedExploration;
import org.mate.exploration.rl.qlearning.qbe.exploration.RandomExploration;
import org.mate.exploration.rl.qlearning.qbe.qmatrix.QBEMatrixFactory;

import java.util.Optional;


@RunWith(AndroidJUnit4.class)
public class ExecuteMATEQBENew {

    private ExplorationStrategy getStrategy(final Strategy strategy) {
        final QBEMatrixFactory factory = new QBEMatrixFactory();

        switch (strategy) {
            case RANDOM:
                return new RandomExploration();
            case ACTIVITY_MATRIX:
                return new QMMatrixBasedExploration(factory.getMaximizeActivityCoverageQMatrix());
            case CRASH_MATRIX:
                return new QMMatrixBasedExploration(factory.getMaximizesNumberOfCrashesQMatrix());
            case CUSTOM_MATRIX:
                return new QMMatrixBasedExploration(factory.getCustomNewCoverageMatrix());
            default:
                throw new AssertionError("unreachable");
        }
    }

    @Test
    public void useAppContext() {
        final Strategy strategy = Optional.ofNullable(Properties.QBE_EXPLORATION_STRATEGY())
                .map(Strategy::valueOf)
                .orElse(Strategy.RANDOM);

        MATE.log_acc(String.format("Starting QBE %s...", strategy));
        final MATE mate = new MATE();
        mate.testApp(new QBE(Properties.MAX_NUMBER_EVENTS(), getStrategy(strategy)));
    }

    private enum Strategy {
        RANDOM,
        ACTIVITY_MATRIX,
        CRASH_MATRIX,
        CUSTOM_MATRIX,
    }
}
