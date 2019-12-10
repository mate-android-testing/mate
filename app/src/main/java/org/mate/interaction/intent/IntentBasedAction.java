package org.mate.interaction.intent;

import android.content.Intent;

import org.mate.ui.Action;

public class IntentBasedAction extends Action {

    private final Intent intent;
    private final ComponentType componentType;

    IntentBasedAction(Intent intent, ComponentType componentType) {
        this.intent = intent;
        this.componentType = componentType;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public Intent getIntent() {
        return intent;
    }

    /**
     * A custom string representation for an Intent-based action.
     *
     * @return Returns a string representation for an Intent-based action.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IntentBasedAction" + System.lineSeparator());
        builder.append("------------------------");
        builder.append("ComponentType: " + componentType + System.lineSeparator());
        builder.append("Receiver of Intent: " + intent.getComponent());
        builder.append("------------------------");
        return builder.toString();
    }


}
