package org.mate.exploration.qlearning.qbe.interfaces.implementations;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.SUCCESS;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.SUCCESS_NEW_STATE;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.genetic.chromosome.Chromosome;
import org.mate.exploration.qlearning.qbe.interfaces.Application;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;
import org.mate.utils.Pair;
import org.mate.utils.coverage.CoverageUtils;

import java.util.Objects;
import java.util.Optional;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class QBEApplication implements Application<QBEState, QBEAction> {

    private final UIAbstractionLayer uiAbstractionLayer;
    private TestCase currentTestcase = TestCase.newInitializedTestCase();
    private int testcaseLength = 0;

    public QBEApplication(final UIAbstractionLayer uiAbstractionLayer) {
        this.uiAbstractionLayer = Objects.requireNonNull(uiAbstractionLayer);
    }

    @Override
    public QBEState getCurrentState() {
        return new QBEState(uiAbstractionLayer.getLastScreenState());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Pair<Optional<QBEState>, ActionResult> executeAction(final QBEAction action) {
        ActionResult result = currentTestcase.updateTestCaseGetResult(action.getUiAction(), testcaseLength++);
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

    @Override
    public void reset() {
        if (testcaseLength > 0) {
            final Chromosome<TestCase> chromosome = new Chromosome<>(currentTestcase);
            CoverageUtils.storeTestCaseChromosomeCoverage(chromosome);
            CoverageUtils.logChromosomeCoverage(chromosome);
            currentTestcase.finish();
            currentTestcase = TestCase.newInitializedTestCase();
            testcaseLength = 0;

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
