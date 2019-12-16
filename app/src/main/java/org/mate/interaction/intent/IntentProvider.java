package org.mate.interaction.intent;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.v4.view.KeyEventDispatcher;
import android.util.Xml;

import org.mate.MATE;
import org.mate.utils.Randomness;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class IntentProvider {

    private static final String MANIFEST_FILE = "/data/data/org.mate/AndroidManifest.xml";
    private static final String STATIC_INFO_FILE = "/data/data/org.mate/staticIntentInfo.xml";

    private List<ComponentDescription> components;

    public IntentProvider() {
        parseXMLFiles();
    }

    private void parseXMLFiles() {

        try {
            components = parseManifest();
            parseIntentInfoFile();
            MATE.log_acc("Derived the following components: " + components);
        } catch (XmlPullParserException | IOException e) {
            MATE.log_acc("Couldn't parse the AndroidManifest/staticInfoIntent file!");
            MATE.log_acc(e.getMessage());
            throw new IllegalStateException("Couldn't initialise IntentProvider! Aborting.");
        }


    }

    /**
     * Returns whether the set of components contains a service
     * that can be invoked.
     *
     * @return Returns {@code true} if a service component is contained
     * in the set of components, otherwise {@code false}.
     */
    public boolean hasService() {

        for (ComponentDescription component : components) {
            if (component.isService()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the set of components contains a broadcast receiver
     * that can be invoked.
     *
     * @return Returns {@code true} if a broadcast receiver component is contained
     * in the set of components, otherwise {@code false}.
     */
    public boolean hasBroadcastReceiver() {

        for (ComponentDescription component : components) {
            if (component.isBroadcastReceiver()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the set of components contains an activity
     * that can be invoked.
     *
     * @return Returns {@code true} if an activity component is contained
     * in the set of components, otherwise {@code false}.
     */
    public boolean hasActivity() {

        for (ComponentDescription component : components) {
            if (component.isActivity()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an Intent-based action representing the invocation of a certain component.
     *
     * @param componentType The component type.
     * @return Returns an Intent-based action for a certain component.
     */
    public IntentBasedAction getAction(ComponentType componentType) {
        ComponentDescription component = Randomness.randomElement(getComponents(componentType));
        Intent intent = fillIntent(component);
        return new IntentBasedAction(intent, componentType);
    }

    /**
     * Creates and fills an intent with information retrieved from the given component
     * in a random fashion.
     *
     * @param component The component information.
     * @return Returns an intent for a given component.
     */
    private Intent fillIntent(ComponentDescription component) {

        if (!component.hasIntentFilter()) {
            MATE.log_acc("Component " + component + " doesn't declare any intent-filter!");
            throw new IllegalStateException("Component without intent-filter!");
        }

        Set<IntentFilterDescription> intentFilters = component.getIntentFilters();

        // select a random intent filter
        IntentFilterDescription intentFilter = Randomness.randomElement(intentFilters);

        Intent intent = new Intent();

        // add a random action if present
        if (intentFilter.hasAction()) {
            String action = Randomness.randomElement(intentFilter.getActions());
            intent.setAction(action);
        }

        // add a random category if present
        // TODO: add decreasing probability for adding multiple categories
        if (intentFilter.hasCategory()) {
            String category = Randomness.randomElement(intentFilter.getCategories());
            intent.addCategory(category);
        }

        // add a data tag
        if (intentFilter.hasData()) {
            // TODO: consider integration of mimeType -> should be derived automatically otherwise
            Uri uri = intentFilter.getData().generateRandomUri();
            intent.setData(uri);
        }

        // make every intent explicit
        intent.setComponent(new ComponentName(MATE.packageName, component.getFullyQualifiedName()));

        if (component.hasExtra()) {



        }

        // TODO: add data + extras + component name + outsource in method
        return intent;
    }

    /**
     * Returns the components representing a service.
     *
     * @return Returns the list of service components.
     */
    private List<ComponentDescription> getComponents(ComponentType componentType) {
        List<ComponentDescription> targetComponents = new ArrayList<>();
        for (ComponentDescription component : components) {
            if (component.getType() == componentType) {
                targetComponents.add(component);
            }
        }
        MATE.log_acc("Found " + targetComponents.size() + " " + componentType);
        return Collections.unmodifiableList(targetComponents);
    }

    /**
     * Parses additional information, i.e. string constants and bundle entries, for a component.
     * This information has been retrieved by a pre-conducted static analysis on the classes.dex
     * files contained in an APK file. Prior to the execution of this method, the AndroidManifest.xml
     * needs to be parsed by {@link #parseManifest()}. This method assumes that the XML file
     * specified by the constant {@code STATIC_INFO_FILE} has been pushed to the app internal
     * storage of MATE.
     *
     * @throws XmlPullParserException Should never happen.
     * @throws IOException Should never happen.
     */
    private void parseIntentInfoFile() throws XmlPullParserException, IOException {

        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        InputStream inputStream = new FileInputStream(new File(STATIC_INFO_FILE));

        // the AndroidManifest.xml has to be pushed in advance to the app internal storage of MATE
        parser.setInput(inputStream, null);

        // for each component, we parse the name, the strings and extras used in bundle objects
        String componentName = null;
        Set<String> stringConstants = new HashSet<>();
        Map<String, String> extras = new HashMap<>();

        while(parser.next() != XmlPullParser.END_DOCUMENT) {
            if(parser.getEventType() == XmlPullParser.START_TAG) {

                // check whether we found a new component tag
                if(parser.getName().equals("activity")
                        || parser.getName().equals("service")
                        || parser.getName().equals("receiver")) {

                    // new component -> reset
                    extras = new HashMap<>();
                    stringConstants = new HashSet<>();
                    componentName = parser.getAttributeValue(null, "name");

                    // we found a string constant tag
                } else if(parser.getName().equals("string")) {
                    stringConstants.add(parser.getAttributeValue(null, "value"));

                    // we found an extra tag
                } else if(parser.getName().equals("extra")) {
                    extras.put(parser.getAttributeValue(null, "key"),
                            parser.getAttributeValue(null, "type"));
                    // TODO: try to specify the expected type already in the DexAnalyzer
                    // identifyExtraType(parser.getAttributeValue(null, "type")));
                }

            } else if(parser.getEventType() == XmlPullParser.END_TAG) {

                // check whether we found a the component's end tag
                if(parser.getName().equals("activity")
                        || parser.getName().equals("service")
                        || parser.getName().equals("receiver")) {

                    // add collected information
                    for (ComponentDescription component : components) {
                        if (component.getFullyQualifiedName().equals(componentName)) {
                            component.addStringConstants(stringConstants);
                            component.addExtras(extras);
                            break;
                        }
                    }

                    // reset component information
                    componentName = null;
                    stringConstants = new HashSet<>();
                    extras = new HashMap<>();
                }
            }
        }
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
    private List<ComponentDescription> parseManifest() throws XmlPullParserException, IOException {

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
    private void parseIntentFilterElements(XmlPullParser parser, IntentFilterDescription intentFilter) {

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
}
