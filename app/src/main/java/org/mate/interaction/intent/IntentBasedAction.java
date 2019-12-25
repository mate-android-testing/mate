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
        builder.append("Receiver of Intent: " + intent.getComponent() + System.lineSeparator());
        builder.append("Action: " + intent.getAction() + System.lineSeparator());
        builder.append("Categories: " + intent.getCategories() + System.lineSeparator());
        builder.append("Data: " + intent.getData() + System.lineSeparator());
        builder.append("Extras: " + intent.getExtras() + System.lineSeparator());
        builder.append("------------------------");
        return builder.toString();
    }


}
