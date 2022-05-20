package org.mate.message;

import org.junit.Test;
import org.mate.message.serialization.Parser;
import org.mate.message.serialization.Serializer;

import java.io.ByteArrayInputStream;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Tests the serialization and de-serialization functionality of the
 * {@link Message} and {@link Parser} class, respectively.
 */
public class SerializerTest {

    @Test
    public void testMessageWithSubjectOnly() {
        Message message = new Message("test");
        byte[] serializedMessage = Serializer.serialize(message);
        Parser messageParser = new Parser(new ByteArrayInputStream(serializedMessage));
        assertEquals(message, messageParser.nextMessage());
    }

    @Test
    public void testMessageWithSubjectAndStringParameter() {
        Message message = new Message("test");
        message.addParameter("key", "value");
        byte[] serializedMessage = Serializer.serialize(message);
        Parser messageParser = new Parser(new ByteArrayInputStream(serializedMessage));
        assertEquals(message, messageParser.nextMessage());
    }

    @Test
    public void testMessageWithSubjectAndComplexParameter() {
        Message message = new Message("test");
        message.addParameter("key", String.valueOf(new Date()));
        byte[] serializedMessage = Serializer.serialize(message);
        Parser messageParser = new Parser(new ByteArrayInputStream(serializedMessage));
        assertEquals(message, messageParser.nextMessage());
    }

    @Test
    public void testMessageWithSubjectAndMultipleParameters() {
        Message message = new Message("test");
        message.addParameter("key1", "value");
        message.addParameter("key2", String.valueOf(new Date()));
        byte[] serializedMessage = Serializer.serialize(message);
        Parser messageParser = new Parser(new ByteArrayInputStream(serializedMessage));
        assertEquals(message, messageParser.nextMessage());
    }
}
