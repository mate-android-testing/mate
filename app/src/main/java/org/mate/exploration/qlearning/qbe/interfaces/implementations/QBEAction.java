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
        return "{\"uiAction\":{\"actionType\":\"" + uiAction.getActionType() + "\",\"activityName\":\"" + uiAction.getActivityName() + "\",\"hash\":" + uiAction.hashCode() + "}}";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final UIAction action = ((QBEAction) o).uiAction;
            return uiAction.getActivityName().equals(action.getActivityName())
                    && uiAction.getActionType().equals(action.getActionType());
        }
    }

    @Override
    public int hashCode() {
        return uiAction.hashCode();
    }
}
