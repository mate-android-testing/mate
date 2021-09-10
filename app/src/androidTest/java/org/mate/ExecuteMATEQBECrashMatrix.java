package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.qlearning.qbe.algorithms.ApplicationTester;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.exploration.implementations.QBE;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEAction;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEApplication;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEState;
import org.mate.exploration.qlearning.qbe.qmatrix.QBEMatrixFactory;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionSystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEQBECrashMatrix {

    private static void writeTransitionSystem(final TransitionSystem<QBEState, QBEAction> transitionSystem, final String filePath) {
        try (final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))) {
            writer.write(transitionSystem.toString());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void useAppContext() {
        MATE.log_acc("Starting QBE crash matrix exploration...");
        final MATE mate = new MATE();
        final QBEApplication app = new QBEApplication(Registry.getUiAbstractionLayer());
        final ExplorationStrategy<QBEState, QBEAction> explorationStrategy = new QBE<>(new QBEMatrixFactory().getMaximizesNumberOfCrashesQMatrix());
        final ApplicationTester<QBEState, QBEAction> tester = new ApplicationTester<>(app, explorationStrategy, Properties.MAX_NUMBER_EVENTS());
        mate.testApp(tester);

        // TODO: Figure out how to makes this an option in mate.properties
        writeTransitionSystem(tester.getTransitionSystem(), "/home/michael/transitionSystem.txt");
    }
}




