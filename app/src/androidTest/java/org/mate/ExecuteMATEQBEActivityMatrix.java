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

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEQBEActivityMatrix {

   @Test
   public void useAppContext() {
      MATE.log_acc("Starting QBE activity coverage exploration...");
      final MATE mate = new MATE();
      final QBEApplication app = new QBEApplication(Registry.getUiAbstractionLayer());
      final ExplorationStrategy<QBEState, QBEAction> explorationStrategy = new QBE<>(new QBEMatrixFactory().getMaximizeActivityCoverageQMatrix());
      final ApplicationTester<QBEState, QBEAction> tester = new ApplicationTester<>(app, explorationStrategy, Registry.getTimeout(), Properties.MAX_NUMBER_EVENTS());
      MATE.log_acc("Starting timeout run...");
      tester.run();
      MATE.log_acc("Finished run due to timeout.");
      MATE.log(tester.getTransitionSystem().toString());
      mate.testApp(() -> {
      }); // De-register stuff.
   }
}
