package org.mate.exploration.intent.parsers;

import android.util.Xml;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.manifest.element.ComponentDescription;
import org.mate.commons.utils.manifest.element.ComponentType;
import org.mate.commons.utils.manifest.element.IntentFilterDescription;
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
import java.util.Set;

/**
 * Parses additional information about intents, e.g. the key and its type of a bundle entry.
 */
public final class IntentInfoParser {

    /**
     * The location of the staticIntentInfo.xml file.
     */
    private static final File STATIC_INFO_FILE
            = new File("/data/data/org.mate/staticIntentInfo.xml");

    private IntentInfoParser() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    /**
     * Parses additional information, i.e. string constants and bundle entries, for a component.
     * In addition, dynamic broadcast receivers are extracted.
     *
     * The staticIntentInfo.xml file can be obtained by an offline-analysis using the DexAnalyzer
     * tool. Note that the components required as parameter need to be extracted in advance from the
     * AndroidManifest.xml using {@link org.mate.utils.manifest.ManifestParser#parseManifest(String)}.
     *
     * @param components The list of components extracted from the manifest.
     * @return Returns the updated list of components.
     */
    public static List<ComponentDescription> parseIntentInfoFile(List<ComponentDescription> components) {

        try {
            InputStream inputStream = new FileInputStream(STATIC_INFO_FILE);

            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(inputStream, null);

            String componentName = null;
            Set<String> stringConstants = new HashSet<>();
            Map<String, String> extras = new HashMap<>();
            Set<IntentFilterDescription> intentFilters = new HashSet<>();
            IntentFilterDescription intentFilter = new IntentFilterDescription();
            boolean dynamicReceiver = false;
            boolean handlesOnNewIntent = false;

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {

                    switch (parser.getName()) {
                        case "activity":
                        case "service":
                        case "receiver":
                            extras = new HashMap<>();
                            stringConstants = new HashSet<>();
                            intentFilters = new HashSet<>();
                            dynamicReceiver = false;
                            handlesOnNewIntent = false;
                            componentName = parser.getAttributeValue(null, "name");
                            break;
                        case "string":
                            stringConstants.add(parser.getAttributeValue(null, "value"));
                            break;
                        case "extra":
                            extras.put(parser.getAttributeValue(null, "key"),
                                    parser.getAttributeValue(null, "type"));
                            break;
                        case "intent-filter":
                            intentFilter = new IntentFilterDescription();
                            break;
                        case "action":
                            intentFilter.addAction(parser.getAttributeValue(null, "name"));
                            break;
                        case "category":
                            intentFilter.addCategory(parser.getAttributeValue(null, "name"));
                            break;
                        case "dynamic":
                            dynamicReceiver = Boolean.parseBoolean(parser.getAttributeValue(null, "value"));
                            break;
                        case "on_new_intent":
                            handlesOnNewIntent = true;
                            break;
                    }

                } else if (parser.getEventType() == XmlPullParser.END_TAG) {

                    if (parser.getName().equals("intent-filter")) {
                        intentFilters.add(intentFilter);
                    } else if (parser.getName().equals("activity")
                            || parser.getName().equals("service")
                            || parser.getName().equals("receiver")) {

                        boolean foundComponent = false;

                        // lookup the component and extend it with additional information
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

                        // discovered dynamically registered broadcast receivers
                        if (!foundComponent && dynamicReceiver && !intentFilters.isEmpty()) {
                            MATELog.log_debug("Discovered Dynamic Broadcast Receiver: " + componentName
                                    + " (" + parser.getName() + ")");
                            MATELog.log_debug("Intent filters of dynamic receiver: " + intentFilters);

                            ComponentDescription receiver = new ComponentDescription(
                                    Registry.getPackageName(),
                                    componentName,
                                    ComponentType.BROADCAST_RECEIVER);
                            receiver.addStringConstants(stringConstants);
                            receiver.addExtras(extras);
                            receiver.addIntentFilters(intentFilters);
                            receiver.setDynamicReceiver(true);
                            receiver.setEnabled(true);
                            receiver.setExported(true);
                            components.add(receiver);
                        }
                    }
                }
            }
        } catch (IOException | XmlPullParserException e) {
            throw new IllegalStateException("Parsing staticIntentInfo.xml failed!", e);
        }
        return components;
    }
}
