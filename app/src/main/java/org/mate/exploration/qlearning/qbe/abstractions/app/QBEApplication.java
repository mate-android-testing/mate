package org.mate.exploration.qlearning.qbe.abstractions.app;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.qlearning.qbe.abstractions.action.QBEAction;
import org.mate.exploration.qlearning.qbe.abstractions.state.QBEState;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.interaction.action.ActionResult;
import org.mate.model.TestCase;
import org.mate.utils.Pair;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Objects;
import java.util.Optional;

import static org.mate.interaction.action.ActionResult.SUCCESS;
import static org.mate.interaction.action.ActionResult.SUCCESS_NEW_STATE;

/**
 * Provides the necessary functionality to interact with the AUT, e.g. executing an action and
 * retrieving the current state.
 */
public final class QBEApplication implements Application<QBEState, QBEAction> {

    /**
     * Enables the interaction with the AUT.
     */
    private final UIAbstractionLayer uiAbstractionLayer;

    /**
     * The current test case that is constructed.
     */
    private TestCase currentTestCase = TestCase.newInitializedTestCase();

    /**
     * The current test case length.
     */
    private int testCaseLength = 0;

    /**
     * Initialises a new AUT.
     *
     * @param uiAbstractionLayer The interaction engine.
     */
    public QBEApplication(final UIAbstractionLayer uiAbstractionLayer) {
        this.uiAbstractionLayer = Objects.requireNonNull(uiAbstractionLayer);
    }

    /**
     * Retrieves the current state the application is in.
     *
     * @return Returns the current state.
     */
    @Override
    public QBEState getCurrentState() {
        return new QBEState(uiAbstractionLayer.getLastScreenState());
    }

    /**
     * Executes the given action.
     *
     * @param action The action to be executed.
     * @return Returns a pair consisting of the new state and the action result.
     */
    @Override
    public Pair<Optional<QBEState>, ActionResult> executeAction(final QBEAction action) {

        ActionResult result = currentTestCase.updateTestCaseGetResult(action.getUiAction(), testCaseLength++);

        if (result == SUCCESS || result == SUCCESS_NEW_STATE) {
            /*
             * UiAbstractionLayer.executeAction(...) currently does not check whether a new state is
             * reached. So SUCCESS is returned even if SUCCESS_NEW_STATE would be the more
             * appropriate result. To check whether a new state is reached
             * UiAbstractionLayer.reachedNewState() has to be used explicitly.
             */
            if (result == SUCCESS && uiAbstractionLayer.reachedNewState()) {
                result = SUCCESS_NEW_STATE;
            }
            return new Pair<>(Optional.of(new QBEState(uiAbstractionLayer.getLastScreenState())), result);
        } else {
            return new Pair<>(Optional.empty(), result);
        }
    }

    /**
     * Resets the AUT. As a side effect, coverage data is collected.
     */
    @Override
    public void reset() {
        if (testCaseLength > 0) {
            final Chromosome<TestCase> chromosome = new Chromosome<>(currentTestCase);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
            CoverageUtils.logChromosomeCoverage(chromosome);
            currentTestCase.finish();
            currentTestCase = TestCase.newInitializedTestCase();
            testCaseLength = 0;
        }
        uiAbstractionLayer.resetApp();
    }

    @Override
    public QBEState copyWithDummyComponent(final QBEState conflictingState) {
        final QBEState copy = new QBEState(conflictingState);
        copy.addDummyComponent();
        return copy;
    }
}
