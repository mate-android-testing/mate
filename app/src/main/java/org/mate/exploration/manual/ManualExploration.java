package org.mate.exploration.manual;

import android.support.annotation.NonNull;

import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.interaction.action.Action;
import org.mate.model.TestCase;
import org.mate.state.IScreenState;
import org.mate.utils.Either;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Performs a manual exploration where the user is requested to enter an action or command through
 * a dialog.
 */
public class ManualExploration implements Algorithm {

    /**
     * Determines when the end of the manual exploration is reached.
     */
    private boolean stop = false;

    /**
     * Explores the AUT until the time budget is reached.
     */
    @Override
    public void run() {
        while (!stop) {
            explore();
        }
    }

    private void explore() {
        Optional<List<ManualExploration.ExplorationStep>> steps;
        do {
            steps = getExplorationStepsUnsafe();
        } while (steps.isPresent());
    }

    /**
     * Explores the AUT with actions supplied by the user until the AUT is left, the maximal number
     * of actions is reached or the user enters STOP or RESET.
     *
     * @return Returns the list of conducted exploration steps.
     */
    private Optional<List<ManualExploration.ExplorationStep>> getExplorationStepsUnsafe() {

        final List<ManualExploration.ExplorationStep> explorationSteps = new LinkedList<>();

        Registry.getUiAbstractionLayer().resetApp();

        final TestCase testCase = TestCase.newInitializedTestCase();
        IScreenState screenState = Registry.getUiAbstractionLayer().getLastScreenState();
        explorationSteps.add(createStep(screenState, null));

        try {
            for (int actionsCount = 0; actionsCount < Properties.MAX_NUMBER_EVENTS(); actionsCount++) {

                Either<Action, ManualExploration.ExplorationCommand> actionOrCommand = askUserToPickAction();
                boolean finishTest;

                if (actionOrCommand.hasLeft()) {
                    final Action action = actionOrCommand.getLeft();
                    finishTest = !testCase.updateTestCase(action, actionsCount);
                    screenState = Registry.getUiAbstractionLayer().getLastScreenState();
                    explorationSteps.add(createStep(screenState, action));
                } else {
                    switch (actionOrCommand.getRight()) {
                        case STOP: stop = true; return Optional.empty();
                        case RESET: finishTest = true; break;
                        default: throw new IllegalArgumentException("Unknown command: " + actionOrCommand.getRight().description);
                    }
                }

                if (finishTest) {
                    return Optional.of(explorationSteps);
                }
            }
        } finally {
            testCase.finish();
        }

        return Optional.of(explorationSteps);
    }

    /**
     * Asks the user to supply an action or a command.
     *
     * @return Returns the supplied action or command.
     */
    private Either<Action, ManualExploration.ExplorationCommand> askUserToPickAction() {

        List<Action> availableActions = new LinkedList<>(Registry.getUiAbstractionLayer().getExecutableActions());
        availableActions.add(0, new ManualExploration.ExplorationCommandAction(ManualExploration.ExplorationCommand.STOP));
        availableActions.add(1, new ManualExploration.ExplorationCommandAction(ManualExploration.ExplorationCommand.RESET));

        Action pickedAction = Registry.getEnvironmentManager().askUserToPick(availableActions);

        if (pickedAction instanceof ManualExploration.ExplorationCommandAction) {
            return Either.right(((ManualExploration.ExplorationCommandAction) pickedAction).command);
        } else {
            return Either.left(pickedAction);
        }
    }

    private ManualExploration.ExplorationStep createStep(IScreenState state, Action action) {
        return new ManualExploration.ExplorationStep(state, action);
    }

    public static class ExplorationStep {

        private final IScreenState state;
        private final Action action;

        public ExplorationStep(IScreenState state, Action action) {
            this.state = state;
            this.action = action;
        }

        @Override
        public String toString() {
            return "ExplorationStep{" +
                    "state=" + state +
                    ", action=" + action +
                    '}';
        }
    }

    private enum ExplorationCommand {

        STOP("Stop exploration"),
        RESET("Reset exploration");

        private final String description;

        ExplorationCommand(String description) {
            this.description = description;
        }
    }

    private static class ExplorationCommandAction extends Action {

        private final ManualExploration.ExplorationCommand command;

        private ExplorationCommandAction(ManualExploration.ExplorationCommand command) {
            this.command = command;
        }

        @NonNull
        @Override
        public String toString() {
            return command.description;
        }

        @NonNull
        @Override
        public String toShortString() {
            return toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ManualExploration.ExplorationCommandAction that = (ManualExploration.ExplorationCommandAction) o;
            return command == that.command;
        }

        @Override
        public int hashCode() {
            return Objects.hash(command);
        }
    }
}
