package org.mate.message.serialization;

import org.mate.message.Message;

import java.util.Map;

import static org.mate.message.serialization.Lexer.*;

public class Serializer {
    public static String escapeParameterValue(String unescaped) {
        StringBuilder sb = new StringBuilder();
        for (char c : unescaped.toCharArray()) {
            if (c == ESCAPE_CHAR || c == END_PARAMETER_CHAR || c == END_MESSAGE_CHAR || c == RELATION_SEPARATOR_CHAR) {
                sb.append(ESCAPE_CHAR);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static byte[] serialize(Message message) {
        StringBuilder sb = new StringBuilder(escapeParameterValue(message.getSubject()));
        if (!message.getParameters().isEmpty()) {
            sb.append(END_PARAMETER_CHAR);
        }
        for (Map.Entry<String, String> parameter : message.getParameters().entrySet()) {
            sb.append(escapeParameterValue(parameter.getKey()));
            sb.append(RELATION_SEPARATOR_CHAR);
            sb.append(escapeParameterValue(parameter.getValue()));
            sb.append(END_PARAMETER_CHAR);
        }
        if (!message.getParameters().isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        sb.append(END_MESSAGE_CHAR);
        return sb.toString().getBytes(CHARSET);
    }
}
