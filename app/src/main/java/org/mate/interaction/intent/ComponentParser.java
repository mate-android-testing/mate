package org.mate.interaction.intent;

import android.util.Xml;

import org.mate.MATE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ComponentParser {

    private static final String MANIFEST_FILE = "/data/data/org.mate/AndroidManifest.xml";

    private ComponentParser() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    /**
     * Parses the AndroidManifest.xml in order to retrieve a list of (exported and enabled) components
     * with its declared intent-filters. This yields for every component a description to which
     * actions, categories and data it can handle.
     *
     * @return Returns a list of exported and enabled components.
     * @throws XmlPullParserException Should never happen.
     * @throws IOException            Should never happen.
     */
    public static List<ComponentDescription> parseManifest() throws XmlPullParserException, IOException {

        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        InputStream inputStream = new FileInputStream(new File(MANIFEST_FILE));

        // the AndroidManifest.xml has to be pushed in advance to the app internal storage of MATE
        parser.setInput(inputStream, null);

        // move on to the first tag and verify that the package attribute matches the package name of the AUT
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "manifest");
        if (!parser.getAttributeValue(null, "package").equals(MATE.packageName)) {
            throw new IllegalArgumentException("Wrong manifest file! Found package was "
                    + parser.getAttributeValue(null, "package") + ", but should be " + MATE.packageName);
        }

        List<ComponentDescription> components = new ArrayList<>();
        ComponentDescription currentComponent = null;
        IntentFilterDescription currentIntentFilter = null;

        // parse each component tag
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {

                switch (parser.getName()) {
                    case "activity":
                    case "service":
                    case "receiver":

                        // check whether the component has been exported
                        boolean isComponentExported = parser.getAttributeValue(null, "exported") == null
                                || parser.getAttributeValue(null, "exported").equals("true");

                        // check whether the component has been enabled
                        boolean isComponentEnabled = parser.getAttributeValue(null, "enabled") == null
                                || parser.getAttributeValue(null, "enabled").equals("true");

                        // we can only target components that are enabled and exported
                        if (isComponentExported && isComponentEnabled) {

                            // parse the name (android:name)
                            String componentName = parser.getAttributeValue(null, "name");

                            // parse the type, e.g. activity or service
                            String componentType = parser.getName();

                            currentComponent = new ComponentDescription(componentName, componentType);
                        }
                        break;

                    case "intent-filter":
                        if (currentComponent != null) {
                            currentIntentFilter = new IntentFilterDescription();
                        }
                        break;

                    case "action":
                    case "category":
                    case "data":
                        if (currentComponent != null) {
                            parseIntentFilterElements(parser, currentIntentFilter);
                        }
                        break;

                    default:
                        break;
                }

            } else if (parser.getEventType() == XmlPullParser.END_TAG) {
                switch (parser.getName()) {
                    case "activity":
                    case "service":
                    case "receiver":
                        // only add components that define an intent-filter
                        if (currentComponent != null && currentComponent.hasIntentFilter()) {
                            // MATE.log_acc(currentComponent.toString());
                            components.add(currentComponent);
                            currentComponent = null;
                        }
                        break;
                    case "intent-filter":
                        if (currentComponent != null && currentIntentFilter != null) {
                            currentComponent.addIntentFilter(currentIntentFilter);
                            // reset filter
                            currentIntentFilter = null;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return components;
    }


    /**
     * Parses an intent-filter tag for the nested tags action, category or data.
     * If such tag is found, all its relevant attributes are retrieved.
     *
     * @param parser       The parser referring currently to the first tag within the intent-filter tag.
     * @param intentFilter An intent-filter storing action, category and data tags.
     */
    private static void parseIntentFilterElements(XmlPullParser parser, IntentFilterDescription intentFilter) {

        // either an action, category or data tag
        String tagName = parser.getName();
        MATE.log_acc("Found Tag: " + tagName);

        if (tagName.equals("action")) {
            intentFilter.addAction(parser.getAttributeValue(null, "name"));
        } else if (tagName.equals("category")) {
            intentFilter.addCategory(parser.getAttributeValue(null, "name"));
        } else if (tagName.equals("data")) {
            String scheme = parser.getAttributeValue(null, "scheme");
            String host = parser.getAttributeValue(null, "host");
            String port = parser.getAttributeValue(null, "port");
            String path = parser.getAttributeValue(null, "path");
            String pathPattern = parser.getAttributeValue(null, "pathPattern");
            String pathPrefix = parser.getAttributeValue(null, "pathPrefix");
            String mimeType = parser.getAttributeValue(null, "mimeType");
            intentFilter.addData(scheme, host, port, path, pathPattern, pathPrefix, mimeType);
        }
    }

    /**
     * Checks whether an intent-filter describes a system event by comparing
     * the included actions against the list of system event actions.
     *
     * @param systemEvents The list of possible system events.
     * @param intentFilter The given intent-filter.
     * @return Returns {@code true} if the intent-filter describes a system event,
     *      otherwise {@code false}.
     */
    private static boolean describesSystemEvent(List<String> systemEvents, IntentFilterDescription intentFilter) {

        for(String action : intentFilter.getActions()) {
            if (systemEvents.contains(action)) {
                return true;
            }
        }
        return false;
    }


    public static List<ComponentDescription> filterSystemEventIntentFilters(List<ComponentDescription> components, List<String> systemEvents) {

        List<ComponentDescription> systemEventReceivers = new ArrayList<>();

        // TODO: check whether ConcurrentModificationException can happen
        for (ComponentDescription component : components) {

            // intent filters that refer to system events
            List<IntentFilterDescription> systemEventFilters = new ArrayList<>();

            // typically only broad cast receivers react to system events
            if (component.isBroadcastReceiver()) {

                for (IntentFilterDescription intentFilter : component.getIntentFilters()) {
                    // check which intent filters refer to system events
                    if (describesSystemEvent(systemEvents, intentFilter)) {

                        ComponentDescription systemEventReceiver =
                                new ComponentDescription(component.getFullyQualifiedName(), "receiver");
                        systemEventReceiver.addIntentFilter(intentFilter);
                        systemEventReceivers.add(systemEventReceiver);

                        // track system event intent filters on component basis
                        systemEventFilters.add(intentFilter);
                    }
                }
            }

            // remove system event intent filters from original component
            component.removeIntentFilters(systemEventFilters);

            if (!component.hasIntentFilter()) {
                // no more intent filters attached -> remove it
                MATE.log("Removing component: " + component);
                MATE.log("Removing of component succeeded: " + components.remove(component));
            }
        }
        return systemEventReceivers;
    }

}
