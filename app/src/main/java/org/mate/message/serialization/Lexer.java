package org.mate.message.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    public static final char ESCAPE_CHAR = '\\';
    public static final char END_PARAMETER_CHAR = ';';
    public static final char RELATION_SEPARATOR_CHAR = ':';
    public static final char END_MESSAGE_CHAR = '~';
    public static final Charset CHARSET = Charset.forName("UTF-8");

    private final InputStreamReader reader;

    public Lexer(InputStream in) {
        reader = new InputStreamReader(in, CHARSET);
    }

    public List<Token> lexMessage() {
        List<Token> tokens = new ArrayList<>();
        LexResult lexResult = lexSubject();

        panicOnFailure(lexResult);
        tokens.add(lexResult.token);

        if (lexResult.endsMessage) {
            tokens.add(new Token(Type.END_MESSAGE));
            return tokens;
        }

        tokens.add(new Token(Type.END_PARAM));

        while (true) {
            lexResult = lexParamKey();
            panicOnFailure(lexResult);
            tokens.add(lexResult.token);
            tokens.add(new Token(Type.RELATION_SEPARATOR));

            lexResult = lexValue();
            panicOnFailure(lexResult);
            tokens.add(lexResult.token);

            if (lexResult.endsMessage) {
                tokens.add(new Token(Type.END_MESSAGE));
                return tokens;
            }
            tokens.add(new Token(Type.END_PARAM));
        }
    }

    public LexResult lexSubject() {
        LexResult lexResult = lexValue();
        if (lexResult.failed) {
            return LexResult.failure("Lexing subject failed: " + lexResult.failureMessage);
        }

        return LexResult.success(lexResult.endsMessage, new Token(Type.SUBJECT, lexResult.token.contents));
    }

    public LexResult lexValue() {
        int nextChar;
        StringBuilder value = new StringBuilder();
        boolean escapedCharacter = false;
        while (true) {

            try {
                nextChar = reader.read();
            } catch (IOException e) {
                return LexResult.failure("Lexing value failed: IO error while reading from input: " + e.getLocalizedMessage());
            }

            if (nextChar == -1) {
                return LexResult.failure("Lexing value key failed: unexpected EOF");
            }

            char chr = (char) nextChar;

            if (escapedCharacter) {
                escapedCharacter = false;
                value.append(chr);
                continue;
            }

            if (chr == ESCAPE_CHAR) {
                escapedCharacter = true;
            } else if (chr == END_MESSAGE_CHAR) {
                return LexResult.success(true, new Token(Type.VALUE, value.toString()));
            } else if (chr == END_PARAMETER_CHAR) {
                return LexResult.success(false, new Token(Type.VALUE, value.toString()));
            } else {
                value.append(chr);
            }
        }
    }

    public LexResult lexParamKey() {
        int nextChar;
        StringBuilder paramKey = new StringBuilder();
        while (true) {

            try {
                nextChar = reader.read();
            } catch (IOException e) {
                return LexResult.failure("Lexing param key failed: IO error while reading from input: " + e.getLocalizedMessage());
            }

            if (nextChar == -1) {
                return LexResult.failure("Lexing param key failed: unexpected EOF");
            }

            char chr = (char) nextChar;

            if (Character.isLetterOrDigit(chr)) {
                paramKey.append(chr);
            } else if (chr == END_MESSAGE_CHAR) {
                return LexResult.failure("Lexing param key failed: unexpected end of message character.");
            } else if (chr == END_PARAMETER_CHAR) {
                return LexResult.failure("Lexing param key failed: unexpected end of parameter character.");
            } else if (chr == RELATION_SEPARATOR_CHAR) {
                return LexResult.success(false, new Token(Type.PARAM_KEY, paramKey.toString()));
            } else {
                return LexResult.failure("Lexing param key failed: unexpected non alpha numeric character: '" + chr + "'");
            }
        }
    }

    private void panicOnFailure(LexResult lexResult) {
        if (lexResult.failed) {
            throw new IllegalStateException(lexResult.failureMessage);
        }
    }

    private static class LexResult {
        private final boolean endsMessage;
        private final Token token;
        private final boolean failed;
        private final String failureMessage;

        private LexResult(boolean endsMessage, Token token, boolean failed, String failureMessage) {
            this.endsMessage = endsMessage;
            this.token = token;
            this.failed = failed;
            this.failureMessage = failureMessage;
        }

        public static LexResult success(boolean endsMessage, Token token) {
            return new LexResult(endsMessage, token, false, null);
        }

        public static LexResult failure(String failureMessage) {
            return new LexResult(false, null, true, failureMessage);
        }
    }

    public static enum Type {
        END_PARAM, END_MESSAGE, RELATION_SEPARATOR, SUBJECT, PARAM_KEY, VALUE;
    }

    public static class Token {
        private final Type type;
        private final String contents;

        public Token(Type type) {
            this(type, null);
        }

        public Token(Type type, String contents) {
            this.type = type;
            this.contents = contents;
        }

        public Type getType() {
            return type;
        }

        public String getContents() {
            return contents;
        }
    }
}
