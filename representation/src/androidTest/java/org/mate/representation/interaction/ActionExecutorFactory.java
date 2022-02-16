package org.mate.representation.interaction;

import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.intent.IntentBasedAction;
import org.mate.commons.interaction.action.intent.SystemAction;
import org.mate.commons.interaction.action.ui.UIAction;
import org.mate.commons.interaction.action.ui.WidgetAction;

public class ActionExecutorFactory {
    public static ActionExecutor getExecutor(Action action) {
        if (action instanceof WidgetAction) {
            return new UiActionExecutor();
        }/* else if (action instanceof PrimitiveAction) {
            return new PrimitiveActionExecutor();
        }*/ else if (action instanceof IntentBasedAction) {
            return new IntentBasedActionExecutor();
        } else if (action instanceof SystemAction) {
            return new SystemActionExecutor();
        }/* else if (action instanceof MotifAction) {
            return new MotifActionExecutor();
        }*/ else if (action instanceof UIAction) {
            return new UiActionExecutor();
        } else {
            throw new UnsupportedOperationException("Actions class "
                    + action.getClass().getSimpleName() + " not yet supported");
        }
    }
}
