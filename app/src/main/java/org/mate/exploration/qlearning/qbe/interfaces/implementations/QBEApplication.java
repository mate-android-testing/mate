package org.mate.exploration.qlearning.qbe.interfaces.implementations;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.interfaces.Application;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.utils.Pair;

import java.util.Objects;
import java.util.Optional;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;

@RequiresApi(api = Build.VERSION_CODES.N)
public final class QBEApplication implements Application<QBEState, QBEAction> {

    private final UIAbstractionLayer uiAbstractionLayer;

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
        final ActionResult result = uiAbstractionLayer.executeAction(action.getUiAction());
        if (result == ActionResult.SUCCESS || result == ActionResult.SUCCESS_NEW_STATE) {
            return new Pair<>(Optional.of(new QBEState(uiAbstractionLayer.getLastScreenState())), result);
        } else {
            return new Pair<>(Optional.empty(), result);
        }
    }

    @Override
    public void reset() {
        uiAbstractionLayer.resetApp();
    }

    @Override
    public QBEState copyWithDummyComponent(final QBEState conflictingState) {
        final QBEState copy = new QBEState(conflictingState);
        copy.addDummyComponent();
        return copy;
    }
}
