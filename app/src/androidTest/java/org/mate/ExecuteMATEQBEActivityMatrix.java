package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.qlearning.qbe.algorithms.ApplicationTester;
import org.mate.exploration.qlearning.qbe.exploration.ExplorationStrategy;
import org.mate.exploration.qlearning.qbe.exploration.implementations.QBE;
import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.exploration.qlearning.qbe.interfaces.State;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEAction;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEApplication;
import org.mate.exploration.qlearning.qbe.interfaces.implementations.QBEState;
import org.mate.exploration.qlearning.qbe.qmatrix.QBEMatrixFactory;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionSystem;
import org.mate.exploration.qlearning.qbe.transitionSystem.TransitionSystemSerializer;
import org.mate.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATEQBEActivityMatrix {
   private static final String TRANSITION_SYSTEM_DIR = "/data/data/org.mate/transition_systems";
   private static final String FILE_NAME = "transition_system.gz";

   private static <S extends State<A>, A extends Action> void writeTransitionSystem(final TransitionSystem<S, A> transitionSystem)  {
      final File dir = new File(TRANSITION_SYSTEM_DIR);
      if (!dir.exists()) {
         MATE.log("Creating transition system folder succeeded: " + dir.mkdir());
      }

      final File outputFile = new File(dir, FILE_NAME);
      try(final PrintWriter writer = new PrintWriter(outputFile)) {
          writer.write(transitionSystem.toString());
      } catch (FileNotFoundException e) {
         MATE.log_error(e.getMessage());
         e.printStackTrace();
      }

   }

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
      final TransitionSystemSerializer serializer = new TransitionSystemSerializer(TRANSITION_SYSTEM_DIR);
      serializer.serialize(tester.getTransitionSystem(), FILE_NAME);
      mate.testApp(() -> {
      }); // De-register stuff.
   }
}
