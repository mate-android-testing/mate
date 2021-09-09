package org.mate.exploration.qlearning.qbe.interfaces.implementations;

import android.os.Build;
import android.support.annotation.RequiresApi;

import org.mate.exploration.qlearning.qbe.interfaces.Application;
import org.mate.interaction.UIAbstractionLayer;

import java.util.Objects;
import java.util.Optional;

import static org.mate.interaction.UIAbstractionLayer.ActionResult;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QBEApplication implements Application<QBEState, QBEAction> {

    private final UIAbstractionLayer uiAbstractionLayer;

    public QBEApplication(final UIAbstractionLayer uiAbstractionLayer) {
        this.uiAbstractionLayer = Objects.requireNonNull(uiAbstractionLayer);
    }

    @Override
    public QBEState getInitialState() {
        return null; // TODO!
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Optional<QBEState> executeAction(final QBEAction action) {
        final ActionResult result = uiAbstractionLayer.executeAction(action.getUiAction());
        // TODO: What if exited app?
        if (result == ActionResult.SUCCESS || result == ActionResult.SUCCESS_NEW_STATE) {
            return Optional.of(new QBEState(uiAbstractionLayer.getLastScreenState()));
        } else {
            return Optional.empty();
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
