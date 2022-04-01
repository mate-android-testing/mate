package org.mate.utils.testcase.serialization;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.mate.commons.interaction.action.intent.IntentBasedAction;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.manifest.element.ComponentDescription;
import org.mate.commons.utils.manifest.element.ComponentType;
import org.mate.commons.utils.manifest.element.IntentFilterDescription;

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
            writer.startNode("target");
            // writer.setValue(intent.getComponent().getClassName());
            writer.setValue(intent.getComponent().toShortString());
            writer.endNode();
        } else if (intent.getPackage() != null) {
            writer.startNode("package");
            writer.setValue(intent.getPackage());
            writer.endNode();
        }

        if (intent.getType() != null) {
            writer.startNode("mime");
            writer.setValue(intent.getType());
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

        writer.startNode("component");
        context.convertAnother(intentBasedAction.getComponent());
        writer.endNode();

        writer.startNode("intent-filter");
        context.convertAnother(intentBasedAction.getIntentFilter());
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
     *          <target>name</target>
     *          <package>package</package>  (if target is not defined)
     *          <mime>mime type</mime>
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
            componentType = ComponentType.mapStringToComponent(type);
        }

        // leave 'type' tag
        reader.moveUp();

        // enter 'intent' tag
        reader.moveDown();

        Intent intent = new Intent();

        // NOTE: hasMoreChildren() depends on current reader state -> iterates until end of intent tag
        while (reader.hasMoreChildren()) {

            reader.moveDown();

            if (reader.getNodeName().equals("action")) {
                intent.setAction(reader.getValue());
            } else if (reader.getNodeName().equals("categories")) {
                String[] categories = reader.getValue().substring(1, reader.getValue().length()-1).split(",");
                for (String category : categories) {
                    intent.addCategory(category);
                }
            } else if (reader.getNodeName().equals("data")) {
                intent.setData(Uri.parse(reader.getValue()));
            } else if (reader.getNodeName().equals("target")) {
                String[] tokens = reader.getValue().substring(1, reader.getValue()
                        .length() - 1).split("/");
                intent.setComponent(new ComponentName(tokens[0], tokens[1]));
            } else if (reader.getNodeName().equals("extras")) {
                Bundle bundle = (Bundle)context.convertAnother(intent, Bundle.class);
                intent.putExtras(bundle);
            } else if (reader.getNodeName().equals("mime")) {
                // TODO: consider the internal restrictions of setType()
                //  setting the mime type clears an already defined uri
                intent.setType(reader.getValue());
            } else if (reader.getNodeName().equals("package")) {
                // TODO: consider the internal restrictions of setPackage()
                intent.setPackage(reader.getValue());
            }

            reader.moveUp();
        }

        // leave 'intent' tag
        reader.moveUp();

        // enter 'component' tag
        reader.moveDown();

        ComponentDescription component = null;

        if (reader.getNodeName().equals("component")) {
            component = (ComponentDescription) context.convertAnother(null,
                    ComponentDescription.class);
        }

        // leave 'component' tag
        reader.moveUp();

        // enter 'intent-filter' tag
        reader.moveDown();

        IntentFilterDescription intentFilter = new IntentFilterDescription();

        if (reader.getNodeName().equals("intent-filter")) {
            intentFilter = (IntentFilterDescription) context.convertAnother(null,
                    IntentFilterDescription.class);
        }

        // leave 'intent-filter' tag
        reader.moveUp();

        if (intent.getComponent() == null && intent.getPackage() == null) {
            // should hopefully never happen, at least this should not be the typical use case
            MATELog.log("Intent without specified component nor package name!");
        }

        return new IntentBasedAction(intent, component, intentFilter);
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
