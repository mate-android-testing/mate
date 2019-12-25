package org.mate.interaction.intent;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.DefaultImplementationsMapper;
import com.thoughtworks.xstream.mapper.DefaultMapper;

import org.mate.MATE;
import org.mate.ui.Action;

import java.util.Set;

/**
 * Represents a custom converter from an {@link IntentBasedAction} to XML and vice versa.
 */
public final class IntentBasedActionConverter implements Converter {

    /**
     * Converts an {@link IntentBasedAction} to XML.
     *
     * @param object The {@link IntentBasedAction} to be serialized.
     * @param writer The top-down XML writer instance.
     * @param context The serialization context.
     */
    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {

        IntentBasedAction intentBasedAction = (IntentBasedAction) object;

        writer.startNode("type");
        writer.setValue(intentBasedAction.getComponentType().toString());
        writer.endNode();

        Intent intent = intentBasedAction.getIntent();

        writer.startNode("intent");

        if (intent.getAction() != null) {
            writer.startNode("action");
            writer.setValue(intent.getAction());
            writer.endNode();
        }

        if (intent.getCategories() != null) {
            writer.startNode("categories");
            // TODO: use pre-defined collection converter
            // context.convertAnother(intent.getCategories());
            writer.setValue(intent.getCategories().toString());
            writer.endNode();
        }

        if (intent.getData() != null) {
            writer.startNode("data");
            writer.setValue(intent.getDataString());
            writer.endNode();
        }

        if (intent.getComponent() != null) {
            writer.startNode("name");
            writer.setValue(intent.getComponent().getClassName());
            writer.endNode();
        }

        if (intent.getExtras() != null) {
            // use default converter - seems to work as expected
            writer.startNode("extras");
            context.convertAnother(intent.getExtras());
            writer.endNode();
        }

        // end intent tag
        writer.endNode();
    }

    /**
     * Converts XML to an {@link IntentBasedAction}, where
     * the input must comply to the following XML structure:
     *
     *      <type>componentType</type>
     *      <intent>
     *          <action>action</action>
     *          <categories>categories</categories>
     *          <data>uri</data>
     *          <name>name</name>
     *          <extras>bundle</extras>
     *      </intent>
     *
     * @param reader The top-down reader.
     * @param context The de-serialization context.
     * @return Returns the de-serialized {@link IntentBasedAction}.
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        // enter 'type' tag
        reader.moveDown();

        ComponentType componentType = null;

        if (reader.getNodeName().equals("type")) {
            String type = reader.getValue();
            System.out.println("Component type: " + type);
            componentType = ComponentType.mapStringToComponent(type);
        }

        // leave 'type' tag
        reader.moveUp();

        // enter 'intent' tag
        reader.moveDown();
        System.out.println("Inspecting node: " + reader.getNodeName());

        Intent intent = new Intent();

        // NOTE: hasMoreChildren() depends on current reader state -> iterates until end of intent tag
        while (reader.hasMoreChildren()) {

            reader.moveDown();

            System.out.println("Inspecting (inner) node: " + reader.getNodeName());

            if (reader.getNodeName().equals("action")) {
                System.out.println("Action: " + reader.getValue());
                intent.setAction(reader.getValue());
            } else if (reader.getNodeName().equals("categories")) {
                System.out.println("Categories: " + reader.getValue());
                String[] categories = reader.getValue().substring(1, reader.getValue().length()-1).split(",");
                for (String category : categories) {
                    System.out.println("Category extracted: " + category);
                    intent.addCategory(category);
                }
            } else if (reader.getNodeName().equals("data")) {
                System.out.println("Data: " + reader.getValue());
                intent.setData(Uri.parse(reader.getValue()));
            } else if (reader.getNodeName().equals("name")) {
                System.out.println("Target Component: " + reader.getValue());
                intent.setComponent(new ComponentName(MATE.packageName, reader.getValue()));
            } else if (reader.getNodeName().equals("extras")) {
                Bundle bundle = (Bundle)context.convertAnother(intent, Bundle.class);
                System.out.println("Extras: " + bundle);
                intent.putExtras(bundle);
            }

            reader.moveUp();
        }

        // leave 'intent' tag
        reader.moveUp();

        return new IntentBasedAction(intent, componentType);
    }

    /**
     * Defines which kinds of objects can be handled by this converter.
     *
     * @param object The object to be converted.
     * @return Returns {@code true} if the given object can be converted by
     *      this converter, otherwise {@code false}.
     */
    @Override
    public boolean canConvert(Class object) {
        return object.equals(IntentBasedAction.class);
    }

}
