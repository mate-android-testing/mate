package org.mate.interaction.action.intent;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.mate.MATE;
import org.mate.StaticStrings;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Parses additional information about intents, e.g. the key and its type of a bundle entry.
 */
public final class StaticStringsParser {

    private static final String ALL_STRINGS_XML_FILE =  "/data/data/org.mate/allStrings.xml";

    private StaticStringsParser() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    public static void parseAllStringsForActivity() {
        StaticStrings staticStrings = StaticStrings.getInstance();

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            InputStream inputStream = new FileInputStream(new File(ALL_STRINGS_XML_FILE));
            parser.setInput(inputStream, null);
            Set<String> strings = new HashSet<>();
            String className = null;
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("allstrings")) {
                        strings = new HashSet<>();
                        className = parser.getAttributeValue(null, "class");
                    } else if (parser.getName().equals("string")) {
                        strings.add(parser.getAttributeValue(null,"value"));
                    }
                } else if (parser.getEventType() == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("allstrings")) {
                            staticStrings.add(className, strings);
                    }
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }
}

