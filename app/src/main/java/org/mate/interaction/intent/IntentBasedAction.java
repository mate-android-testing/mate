package org.mate.interaction.intent;

import android.content.Intent;

import org.mate.ui.Action;

class IntentBasedAction extends Action {

    private final Intent intent;
    private final ComponentType componentType;

    IntentBasedAction(Intent intent, ComponentType componentType) {
        this.intent = intent;
        this.componentType = componentType;
    }

    @Override
    public String toString() {
        // TODO: find useful representation
        return null;
    }


}
