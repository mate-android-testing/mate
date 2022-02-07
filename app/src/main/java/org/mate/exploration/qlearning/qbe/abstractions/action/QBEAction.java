package org.mate.exploration.qlearning.qbe.abstractions.action;

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
            final QBEAction action = (QBEAction) o;
            return uiAction.equals(action.uiAction);
        }
    }

    @Override
    public int hashCode() {
        return uiAction.hashCode();
    }
}
