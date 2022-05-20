package org.mate.utils.testcase.serialization;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import org.mate.commons.interaction.action.ui.ActionType;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.interaction.action.ui.WidgetAction;

/**
 * Represents a custom converter from an {@link WidgetAction} to XML and vice versa.
 */
public class WidgetActionConverter implements Converter {

    /**
     * Converts an {@link WidgetAction} to XML.
     *
     * @param object The {@link WidgetAction} to be serialized.
     * @param writer The top-down XML writer instance.
     * @param context The serialization context.
     */
    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {

        WidgetAction widgetAction = (WidgetAction) object;

        writer.startNode("actionType");
        writer.setValue(widgetAction.getActionType().toString());
        writer.endNode();

        /*
        * NOTE: We write out the attributes widgetID, widgetResourceID and widgetClazz
        * redundantly, they are actually included in the widget class attribute. This
        * simplifies the parsing of a serialized test case object for the analysis framework.
        * In particular, we don't need to follow these nasty backward references in XML that way.
         */
        writer.startNode("widgetID");
        writer.setValue(widgetAction.getWidget().getId());
        writer.endNode();

        writer.startNode("widgetResourceID");
        writer.setValue(widgetAction.getWidget().getResourceID());
        writer.endNode();

        writer.startNode("widgetClazz");
        writer.setValue(widgetAction.getWidget().getClazz());
        writer.endNode();

        writer.startNode("widget");
        context.convertAnother(widgetAction.getWidget());
        writer.endNode();
    }

    /**
     * Converts XML to an {@link WidgetAction}.
     *
     * @param reader The top-down reader.
     * @param context The de-serialization context.
     * @return Returns the de-serialized {@link WidgetAction}.
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        // enter 'actionType' tag
        reader.moveDown();

        ActionType actionType = null;

        if (reader.getNodeName().equals("actionType")) {
            actionType = ActionType.valueOf(reader.getValue());
        } else {
            throw new IllegalStateException("Couldn't find actionType tag at expected position!");
        }

        // leave 'actionType' tag
        reader.moveUp();

        // we ignore the redundantly serialized attributes widgetID, widgetResourceID and widgetClazz
        reader.moveDown();
        reader.moveUp();
        reader.moveDown();
        reader.moveUp();
        reader.moveDown();
        reader.moveUp();

        // enter 'widget' tag
        reader.moveDown();

        Widget widget = null;

        if(reader.getNodeName().equals("widget")) {
            widget = (Widget) context.convertAnother(null, Widget.class);
        } else {
            throw new IllegalStateException("Couldn't find widget tag at expected position!");
        }

        // leave 'widget' tag
        reader.moveUp();

        return new WidgetAction(widget, actionType);
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
        return object.equals(WidgetAction.class);
    }
}
