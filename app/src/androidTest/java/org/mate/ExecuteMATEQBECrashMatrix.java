package org.mate;

import org.junit.Test;
import org.mate.exploration.qlearning.qbe.algorithms.ApplicationTester;
import org.mate.exploration.qlearning.qbe.algorithms.SimpleTester;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.exploration.implementations.QBE;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEAction;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEApplication;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEState;
import org.mate.exploration.qlearning.qbe.qmatrix.QBEMatrixFactory;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionSystemSerializer;

public class ExecuteMATEQBECrashMatrix {
    private static final String TRANSITION_SYSTEM_DIR = "/data/data/org.mate/transition_systems";
    private static final String FILE_NAME = "transition_system.gz";

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting QBE crash matrix exploration...");
        try (final MATE ignored = new MATE()) {
            final QBEApplication app = new QBEApplication(Registry.getUiAbstractionLayer());
            final ExplorationStrategy<QBEState, QBEAction> explorationStrategy = new QBE<>(new QBEMatrixFactory().getMaximizesNumberOfCrashesQMatrix());

            if (Properties.QBE_RECORD_TRANSITION_SYSTEM()) {
                final ApplicationTester<QBEState, QBEAction> tester = new ApplicationTester<>(app, explorationStrategy, Registry.getTimeout(), Properties.MAX_NUMBER_EVENTS());
                MATE.log_acc("Starting timeout run...");
                tester.run();
                MATE.log_acc("Finished run due to timeout.");
                final TransitionSystemSerializer serializer = new TransitionSystemSerializer(TRANSITION_SYSTEM_DIR);
                serializer.serialize(tester.getTransitionSystem(), FILE_NAME);
            } else {
                final SimpleTester<QBEState, QBEAction> tester = new SimpleTester<>(app, explorationStrategy, Registry.getTimeout(), Properties.MAX_NUMBER_EVENTS());
                MATE.log_acc("Starting timeout run...");
                tester.run();
                MATE.log_acc("Finished run due to timeout.");
            }

            Registry.getEnvironmentManager().fetchTransitionSystem(TRANSITION_SYSTEM_DIR, FILE_NAME);
        }
    }
}
