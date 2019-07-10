package org.mate.message.serialization;

import org.mate.message.Message;

import java.io.InputStream;
import java.util.Iterator;

public class Parser {
    private final Lexer lexer;

    public Parser(InputStream in) {
        lexer = new Lexer(in);
    }

    public Message nextMessage() {
        Iterator<Lexer.Token> tokenIterator = lexer.lexMessage().iterator();

        Lexer.Token currentToken = tokenIterator.next();

        if (currentToken.getType() != Lexer.Type.SUBJECT) {
            throw new IllegalStateException("Unexpected first token of type: " + currentToken.getType());
        }

        Message.MessageBuilder messageBuilder = new Message.MessageBuilder(currentToken.getContents());

        currentToken = tokenIterator.next();
        if (currentToken.getType() != Lexer.Type.END_PARAM && currentToken.getType() != Lexer.Type.END_MESSAGE) {
            throw new IllegalStateException("Unexpected token after subject of type: " + currentToken.getType());
        }

        while (currentToken.getType() != Lexer.Type.END_MESSAGE) {
            if (currentToken.getType() != Lexer.Type.END_PARAM) {
                throw new IllegalStateException("Unexpected token after param value of type: " + currentToken.getType());
            }

            currentToken = tokenIterator.next();

            if (currentToken.getType() != Lexer.Type.PARAM_KEY) {
                throw new IllegalStateException("Unexpected token instead of param key of type: " + currentToken.getType());
            }

            String paramKey = currentToken.getContents();

            currentToken = tokenIterator.next();

            if (currentToken.getType() != Lexer.Type.RELATION_SEPARATOR) {
                throw new IllegalStateException("Unexpected token instead of relation separator of type: " + currentToken.getType());
            }

            currentToken = tokenIterator.next();

            if (currentToken.getType() != Lexer.Type.VALUE) {
                throw new IllegalStateException("Unexpected token instead of param value of type: " + currentToken.getType());
            }

            messageBuilder.withParameter(paramKey, currentToken.getContents());

            currentToken = tokenIterator.next();
        }

        if (tokenIterator.hasNext()) {
            throw new IllegalStateException("Unexpected left over tokens after end of message");
        }

        return messageBuilder.build();
    }
}
