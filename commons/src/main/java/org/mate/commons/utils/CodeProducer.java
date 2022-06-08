package org.mate.commons.utils;

public abstract class CodeProducer {
    public abstract String getCode();

    /**
     * boxing string constant, including escape characters
     * "txt\n" => "\"txt\\n\""
     *
     * @param str
     * @return
     */
    protected String boxString(String str) {
        return "\"" + escapeStringCharacters(str) + "\"";
    }

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
