package org.mate.interaction.intent;

import android.content.Intent;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public final class IntentBasedActionConverter implements Converter {

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext marshallingContext) {

        IntentBasedAction intentBasedAction = (IntentBasedAction) object;

        writer.startNode("type");
        writer.setValue(intentBasedAction.getComponentType().toString());
        writer.endNode();

        Intent intent = intentBasedAction.getIntent();

        writer.startNode("intent");

        writer.startNode("action");
        writer.setValue(intent.getAction());
        writer.endNode();

        writer.startNode("categories");
        writer.setValue(intent.getCategories().toString());
        writer.endNode();

        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
        return null;
    }

    @Override
    public boolean canConvert(Class object) {
        return object.equals(IntentBasedAction.class);
    }

}
