package org.mate.interaction.intent;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.mate.MATE;
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
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IntentBasedAction:" + System.lineSeparator());
        builder.append("------------------------" + System.lineSeparator());
        builder.append("Component: " + component + System.lineSeparator());
        builder.append("IntentFilter: " + intentFilter + System.lineSeparator());
        builder.append("------------------------" + System.lineSeparator());
        builder.append("Intent:" + System.lineSeparator());
        builder.append("------------------------" + System.lineSeparator());
        builder.append("ComponentType: " + getComponentType() + System.lineSeparator());
        builder.append("Receiver of Intent: " + intent.getComponent() + System.lineSeparator());
        builder.append("Action: " + intent.getAction() + System.lineSeparator());
        builder.append("Categories: " + intent.getCategories() + System.lineSeparator());
        builder.append("Data: " + intent.getData() + System.lineSeparator());
        builder.append("Extras: " + intent.getExtras() + System.lineSeparator());
        builder.append("------------------------" + System.lineSeparator());
        return builder.toString();
    }


}
