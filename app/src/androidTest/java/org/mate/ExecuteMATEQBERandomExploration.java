package org.mate;

import org.junit.Test;
import org.mate.exploration.qlearning.qbe.abstractions.action.QBEAction;
import org.mate.exploration.qlearning.qbe.abstractions.app.QBEApplication;
import org.mate.exploration.qlearning.qbe.abstractions.state.QBEState;
import org.mate.exploration.qlearning.qbe.algorithms.ApplicationTester;
import org.mate.exploration.qlearning.qbe.algorithms.SimpleTester;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.exploration.RandomExploration;
import org.mate.exploration.qlearning.qbe.transition_system.TransitionSystemSerializer;

public class ExecuteMATEQBERandomExploration {

    @Test
    public void useAppContext() {

        MATE.log_acc("Starting QBE activity coverage exploration...");

        try (final MATE mate = new MATE()) {
            final QBEApplication app = new QBEApplication(Registry.getUiAbstractionLayer());
            final ExplorationStrategy<QBEState, QBEAction> explorationStrategy = new RandomExploration<>();

            if (Properties.QBE_RECORD_TRANSITION_SYSTEM()) {
                final ApplicationTester<QBEState, QBEAction> tester
                        = new ApplicationTester<>(app, explorationStrategy, Registry.getTimeout(),
                        Properties.MAX_NUMBER_EVENTS());
                MATE.log_acc("Starting timeout run...");
                tester.run();
                MATE.log_acc("Finished run due to timeout.");
                final TransitionSystemSerializer serializer = new TransitionSystemSerializer();
                serializer.serialize(tester.getTransitionSystem());
                Registry.getEnvironmentManager().fetchTransitionSystem();
            } else {
                final SimpleTester<QBEState, QBEAction> tester
                        = new SimpleTester<>(app, explorationStrategy, Registry.getTimeout(),
                        Properties.MAX_NUMBER_EVENTS());
                MATE.log_acc("Starting timeout run...");
                tester.run();
                MATE.log_acc("Finished run due to timeout.");
            }
        }
    }
}
