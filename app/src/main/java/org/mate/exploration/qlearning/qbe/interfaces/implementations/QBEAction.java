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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final QBEAction qbeAction = (QBEAction) o;
            return uiAction.equals(qbeAction.uiAction);
        }
    }

    @Override
    public int hashCode() {
        return uiAction.hashCode();
    }
}
