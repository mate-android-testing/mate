package org.mate.utils;

public class StringUtils {

    /**
     * Escapes special characters used for regular expressions.
     *
     * @param unescaped The unescaped string.
     * @return Returns the escaped string.
     */
    public static String regexEscape(String unescaped) {
        return unescaped.replaceAll("[-.\\+*?\\[^\\]$(){}=!<>|:\\\\]", "\\\\$0");
    }

    /**
     * Determines whether the string is empty or consists exclusively of whitespace characters.
     *
     * @param string The string to check for whitespaces.
     * @return Returns {@code true} if the string is empty or consists only of whitespace characters,
     *         otherwise {@code false} is returned.
     */
    public static boolean isBlank(final String string) {

        if (string.isEmpty()) {
            return true;
        }

        final char[] chars = string.toCharArray();
        for (char c : chars)
            if (!Character.isWhitespace(c))
                return false;

        return true;
    }
}
