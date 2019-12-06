package org.mate.interaction.intent;

import android.support.test.InstrumentationRegistry;
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

public class IntentProvider {

    private static final String MANIFEST_FILE = "/data/data/org.mate/AndroidManifest.xml";

    private List<ComponentDescription> components;

    public IntentProvider() {
        try {
            components = parseManifest();
        } catch (XmlPullParserException | IOException e) {
            MATE.log_acc("Couldn't parse AndroidManifest file!");
            MATE.log_acc(e.getMessage());
            throw new IllegalStateException("Couldn't initialise IntentProvider! Aborting.");
        }
    }

    /**
     * Parses the AndroidManifest.xml in order to retrieve a list of (exported and enabled) components
     * with its declared intent-filters. This yields for every component a description to which
     * actions, categories and data it can handle.
     *
     * @return Returns a list of exported and enabled components.
     * @throws XmlPullParserException Should never happen.
     * @throws IOException Should never happen.
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
                        if (currentComponent != null) {
                            MATE.log_acc(currentComponent.toString());
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


    private void parseManifest2() throws XmlPullParserException, IOException {

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

        // parse each component tag
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {

                // check for component, e.g. activity, that requires no permission(s)
                if ((parser.getName().equals("activity")
                        || parser.getName().equals("service")
                        || parser.getName().equals("receiver"))
                        && parser.getAttributeValue(null, "permission") == null) {

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

                        ComponentDescription component = new ComponentDescription(componentName, componentType);

                        // search for associated intent-filter tags
                        while (parser.nextTag() != XmlPullParser.END_TAG && !parser.getName().equals(componentName)) {

                            // start of intent-filter tag
                            if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName() != null
                                    && parser.getName().equals("intent-filter")) {

                                MATE.log_acc("Found new Intent-Filter TAG!");
                                IntentFilterDescription intentFilter = new IntentFilterDescription();

                                // go until end of intent-filter tag
                                while (parser.next() != XmlPullParser.END_TAG && parser.getName() != null
                                        && parser.getName().equals("intent-filter")) {
                                    // parse the action, category or data tag
                                    parseIntentFilterElements(parser, intentFilter);
                                }

                                // add intent filter to component
                                component.addIntentFilter(intentFilter);
                            }
                        }

                        MATE.log_acc(component.toString());

                        /*

                        ComponentIntentDescription newComponent = ComponentIntentDescription.fromXml(parser);
                        if(newComponent.hasIntentFilters()) // if exported it has at least one default filter
                            provider.addComponent(newComponent);


                         */
                    }
                } else if (parser.getName().equals("activity-alias") && parser.getAttributeValue(null, "permission") == null) {
                    if ((parser.getAttributeValue(null, "enabled") == null || parser.getAttributeValue(null, "enabled").equals("true"))
                            && (parser.getAttributeValue(null, "exported") == null || parser.getAttributeValue(null, "exported").equals("true"))) {

                        /*
                        boolean activityAlreadyAdded = false;
                        for (ComponentIntentDescription desc : provider.activityDescs) {
                            if (desc.getName().equals(IntentHelper.fullComponentName(parser.getAttributeValue(null, "targetActivity")))) {
                                desc.addAliasFromXml(parser);
                                activityAlreadyAdded = true;
                                break;
                            }
                        }
                        if(!activityAlreadyAdded) {
                            ComponentIntentDescription newComponent = new ComponentIntentDescription(parser.getAttributeValue(null, "targetActivity"), false);
                            newComponent.addAliasFromXml(parser);
                            if(newComponent.hasIntentFilters()) // if (and only if) component not exported and alias not exported then there is no filter
                                provider.addComponent(newComponent);
                        }
                        */
                    }
                }
            }
        }
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
