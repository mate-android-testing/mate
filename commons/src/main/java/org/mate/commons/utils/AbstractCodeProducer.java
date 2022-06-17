package org.mate.commons.utils;

/**
 * Auxiliary class to provide common utilities to classes implementing CodeProducer interface.
 */
public abstract class AbstractCodeProducer implements CodeProducer {

    /**
     * Method to turn a normal String into its proper representation for using in a Java expression.
     * This includes escaping the appropriate characters and adding double quotes.
     * For example: "txt\n" => "\"txt\\n\""
     * @param str string to box
     * @return the boxed stirng
     */
    protected String boxString(String str) {
        return "\"" + escapeStringCharacters(str) + "\"";
    }

    /**
     * Escape special characters in string.
     * @param s the string to work on
     * @return the escaped string.
     */
    private String escapeStringCharacters(String s) {
        StringBuilder buffer = new StringBuilder();
        escapeStringCharacters(s.length(), s, buffer);
        return buffer.toString();
    }

    private void escapeStringCharacters(int length, String str, StringBuilder buffer) {
        escapeStringCharacters(length, str, "\"", buffer);
    }

    private StringBuilder escapeStringCharacters(int length,
                                                 String str,
                                                 String additionalChars,
                                                 StringBuilder buffer) {
        for (int idx = 0; idx < length; idx++) {
            char ch = str.charAt(idx);
            switch (ch) {
                case '\b':
                    buffer.append("\\b");
                    break;

                case '\t':
                    buffer.append("\\t");
                    break;

                case '\n':
                    buffer.append("\\n");
                    break;

                case '\f':
                    buffer.append("\\f");
                    break;

                case '\r':
                    buffer.append("\\r");
                    break;

                case '\\':
                    buffer.append("\\\\");
                    break;

                default:
                    if (additionalChars != null && additionalChars.indexOf(ch) > -1) {
                        buffer.append("\\").append(ch);
                    } else if (Character.isISOControl(ch)) {
                        String hexCode = Integer.toHexString(ch).toUpperCase();
                        buffer.append("\\u");
                        int paddingCount = 4 - hexCode.length();
                        while (paddingCount-- > 0) {
                            buffer.append(0);
                        }
                        buffer.append(hexCode);
                    } else {
                        buffer.append(ch);
                    }
            }
        }
        return buffer;
    }
}
