package org.mate.interaction.intent;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.mate.ui.Action;

import java.util.Set;

public class IntentBasedAction extends Action {

    private final Intent intent;
    private final ComponentDescription component;
    private final IntentFilterDescription intentFilter;

    public IntentBasedAction(Intent intent, ComponentDescription component,
                             IntentFilterDescription intentFilter) {
        this.intent = intent;
        this.component = component;
        this.intentFilter = intentFilter;
    }

    public ComponentType getComponentType() {
        return component.getType();
    }

    public Intent getIntent() {
        return intent;
    }

    public ComponentDescription getComponent() {
        return component;
    }

    public IntentFilterDescription getIntentFilter() {
        return intentFilter;
    }

    /**
     * A custom string representation for an Intent-based action.
     *
     * @return Returns a string representation for an Intent-based action.
     */
    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("intent-based action: ");

        if (intent.getAction() != null) {
            builder.append("act=" + intent.getAction() + " ");
        }

        if (intent.getCategories() != null && !intent.getCategories().isEmpty()) {
            builder.append("cat=" + intent.getCategories() + " ");
        }

        if (intent.getDataString() != null) {
            builder.append("uri=" + intent.getDataString() + " ");
        }

        if (intent.getComponent() != null) {
            builder.append("cmp=" + intent.getComponent().toShortString() + " ");
        } else if (intent.getPackage() != null) {
            builder.append("pkt=" + intent.getPackage() + " ");
        }

        if (intent.getType() != null) {
            builder.append("typ=" + intent.getType() + " ");
        }

        if (intent.getExtras() != null) {
            builder.append("ext=[");
            String prefix = "";
            Set<String> keys = intent.getExtras().keySet();
            for (String key : keys) {
                builder.append(prefix);
                prefix = " || ";
                builder.append(key + "=" + intent.getExtras().get(key));
            }
            builder.append("]");
        }

        return builder.toString();
    }


}
