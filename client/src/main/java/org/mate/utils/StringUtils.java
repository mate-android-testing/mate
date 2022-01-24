package org.mate.utils;

public class StringUtils {

    /**
     * Escape special characters used for regular expressions
     * @param unescaped unescaped String
     * @return String with special characters used for regular expressions escaped
     */
    public static String regexEscape(String unescaped) {
        return unescaped.replaceAll("[-.\\+*?\\[^\\]$(){}=!<>|:\\\\]", "\\\\$0");
    }
}
