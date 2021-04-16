package org.mate.interaction.action.intent;

import android.util.Xml;

import org.mate.MATE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
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
public final class IntentInfoParser {

    // the file containing the additional information (obtained from pre-conducted static analysis)
    private static final String STATIC_INFO_FILE = "/data/data/org.mate/staticIntentInfo.xml";

    private IntentInfoParser() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    /**
     * Parses additional information, i.e. string constants and bundle entries, for a component.
     * This information has been retrieved by a pre-conducted static analysis on the classes.dex
     * files contained in an APK file. In addition, we also collect up dynamic broadcast receivers.
     * Prior to the execution of this method, the AndroidManifest.xml
     * needs to be parsed. This method assumes that the XML file
     * specified by the constant {@code STATIC_INFO_FILE} has been pushed to the app internal
     * storage of MATE.
     *
     * @param components The list of components retrieved from the AndroidManifest.xml.
     * @throws XmlPullParserException Should never happen.
     * @throws IOException            Should never happen.
     */
    public static void parseIntentInfoFile(List<ComponentDescription> components,
                                           List<ComponentDescription> dynamicReceivers)
            throws XmlPullParserException, IOException {

        Objects.requireNonNull(components, "Ensure that components have been parsed previously!");

        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        InputStream inputStream = new FileInputStream(new File(STATIC_INFO_FILE));

        // the AndroidManifest.xml has to be pushed in advance to the app internal storage of MATE
        parser.setInput(inputStream, null);

        // for each component, we parse the name, the strings and extras used in bundle objects
        String componentName = null;
        Set<String> stringConstants = new HashSet<>();
        Map<String, String> extras = new HashMap<>();
        Set<IntentFilterDescription> intentFilters = new HashSet<>();
        IntentFilterDescription intentFilter = new IntentFilterDescription();
        boolean dynamicReceiver = false;
        boolean handlesOnNewIntent = false;

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {

                // check whether we found a new component tag
                if (parser.getName().equals("activity")
                        || parser.getName().equals("service")
                        || parser.getName().equals("receiver")) {

                    // new component -> reset
                    extras = new HashMap<>();
                    stringConstants = new HashSet<>();
                    intentFilters = new HashSet<>();
                    dynamicReceiver = false;
                    handlesOnNewIntent = false;
                    componentName = parser.getAttributeValue(null, "name");

                    // we found a string constant tag
                } else if (parser.getName().equals("string")) {
                    stringConstants.add(parser.getAttributeValue(null, "value"));

                    // we found an extra tag
                } else if (parser.getName().equals("extra")) {
                    extras.put(parser.getAttributeValue(null, "key"),
                            parser.getAttributeValue(null, "type"));
                } else if (parser.getName().equals("intent-filter")) {
                    // reset intent-filter
                    intentFilter = new IntentFilterDescription();
                } else if (parser.getName().equals("action")) {
                    intentFilter.addAction(parser.getAttributeValue(null, "name"));
                } else if (parser.getName().equals("category")) {
                    intentFilter.addCategory(parser.getAttributeValue(null, "name"));
                } else if (parser.getName().equals("dynamic")) {
                    dynamicReceiver = Boolean.parseBoolean(parser.getAttributeValue(null, "value"));
                } else if (parser.getName().equals("on_new_intent")) {
                    handlesOnNewIntent = true;
                }

            } else if (parser.getEventType() == XmlPullParser.END_TAG) {

                if (parser.getName().equals("intent-filter")) {
                    intentFilters.add(intentFilter);
                }

                // check whether we found a the component's end tag
                if (parser.getName().equals("activity")
                        || parser.getName().equals("service")
                        || parser.getName().equals("receiver")) {

                    boolean foundComponent = false;

                    // add collected information
                    for (ComponentDescription component : components) {
                        if (component.getFullyQualifiedName().equals(componentName)) {
                            foundComponent = true;

                            component.addStringConstants(stringConstants);
                            component.addExtras(extras);
                            component.setHandlingOnNewIntent(handlesOnNewIntent);

                            if (!intentFilters.isEmpty()) {
                                component.addIntentFilters(intentFilters);
                            }
                            break;
                        }
                    }

                    // handle dynamically registered broadcast receivers (only those with intent-filters)
                    if (!foundComponent && dynamicReceiver && !intentFilters.isEmpty()) {
                        MATE.log("Discovered Dynamic Broadcast Receiver: " + componentName
                                + " (" + parser.getName() + ")");

                        ComponentDescription receiver = new ComponentDescription(componentName,
                                ComponentType.BROADCAST_RECEIVER);
                        receiver.addStringConstants(stringConstants);
                        receiver.addExtras(extras);
                        receiver.addIntentFilters(intentFilters);
                        dynamicReceivers.add(receiver);
                    }
                }
            }
        }
    }


}
