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

    @Override
    public String toString() {
        // TODO: find useful representation
        return null;
    }


}
