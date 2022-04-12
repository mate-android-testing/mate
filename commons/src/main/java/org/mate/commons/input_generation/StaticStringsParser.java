package org.mate.commons.input_generation;

import android.util.Xml;

import org.mate.commons.utils.MATELog;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides a parser for the 'staticStrings.xml' file that contains the static string constants
 * per activity and fragment.
 */
public final class StaticStringsParser {

    /**
     * The location where the xml file should reside.
     */
    private static final String STATIC_STRINGS_XML_FILE =  "/sdcard/staticStrings.xml";

    /**
     * The private utility class constructor.
     */
    private StaticStringsParser() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    /**
     * Parses the 'staticStrings.xml' file.
     *
     * @return Returns a static string object containing the parsed string constants.
     */
    public static StaticStrings parseStaticStrings() {

        StaticStrings staticStrings = StaticStrings.getInstance();

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            File staticStringsXMLFile = new File(STATIC_STRINGS_XML_FILE);

            if (staticStringsXMLFile.exists()) {
                InputStream inputStream = new FileInputStream(staticStringsXMLFile);
                parser.setInput(inputStream, null);

                Set<String> strings = new HashSet<>();
                String className = null;

                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() == XmlPullParser.START_TAG) {
                        if (parser.getName().equals("strings")) {
                            strings = new HashSet<>();
                            className = parser.getAttributeValue(null, "class");
                        } else if (parser.getName().equals("string")) {
                            strings.add(parser.getAttributeValue(null, "value"));
                        }
                    } else if (parser.getEventType() == XmlPullParser.END_TAG) {
                        if (parser.getName().equals("strings")) {
                            staticStrings.add(className, strings);
                        }
                    }
                }

                staticStrings.setInitialised(true);
            }
        } catch (Exception e) {
            MATELog.log_warn("Couldn't parse the static string constants!");
            e.printStackTrace();
        }

        return staticStrings;
    }
}

