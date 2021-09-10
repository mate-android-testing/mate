package org.mate.exploration.qlearning.qbe.interfaces.implementations;

import org.mate.exploration.qlearning.qbe.interfaces.Action;
import org.mate.interaction.action.ui.UIAction;

import java.util.Objects;

public final class QBEAction implements Action {
    private final UIAction uiAction;

    public QBEAction(final UIAction uiAction) {
        this.uiAction = Objects.requireNonNull(uiAction);
    }

    public UIAction getUiAction() {
        return uiAction;
    }

    @Override
    public String toString() {
        return "{\"uiAction\":{\"actionType\":\"" + uiAction.getActionType() + "\",\"activityName\":\"" + uiAction.getActivityName() + "\"}}";
    }
}
