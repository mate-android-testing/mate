package org.mate.interaction.intent;

import android.support.test.InstrumentationRegistry;
import android.util.Xml;

import org.mate.MATE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class IntentProvider {

    private static final String MANIFEST_NAME = "AndroidManifest.xml";

    public IntentProvider() {
        try {
            parseManifest();
        } catch (XmlPullParserException | IOException e) {
            MATE.log_acc("Couldn't parse AndroidManifest file!");
            MATE.log_acc(e.getMessage());
            throw new IllegalStateException("Couldn't initialise IntentProvider! Aborting.");
        }
    }

    private void parseManifest() throws XmlPullParserException, IOException {

        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        // the AndroidManifest.xml has to be pushed in advance to the app internal storage of MATE
        parser.setInput(InstrumentationRegistry.getTargetContext().openFileInput(MANIFEST_NAME), null);

        // move on to the first tag and verify that the package attribute matches the package name of the AUT
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "manifest");
        if (!parser.getAttributeValue(null, "package").equals(MATE.packageName)) {
            throw new IllegalArgumentException("Wrong manifest file! Found package was "
                    + parser.getAttributeValue(null, "package") + ", but should be " + MATE.packageName);
        }

        // parse each component tag
        while(parser.next() != XmlPullParser.END_DOCUMENT) {
            if(parser.getEventType() == XmlPullParser.START_TAG) {

                // check for component, e.g. activity, that requires no permission(s)
                if((parser.getName().equals("activity") || parser.getName().equals("service"))
                        && parser.getAttributeValue(null, "permission") == null) {

                    // check whether the component has been exported
                    boolean isComponentExported = parser.getAttributeValue(null, "exported") == null
                            || parser.getAttributeValue(null, "exported").equals("true");

                    // check whether the component has been enabled
                    boolean isComponentEnabled = parser.getAttributeValue(null, "enabled") == null
                            || parser.getAttributeValue(null, "enabled").equals("true");

                    // we can only target components that are enabled and exported
                    if(isComponentExported && isComponentEnabled) {

                        // parse the name (android:name)
                        String componentName = parser.getAttributeValue(null, "name");

                        // parse the type, e.g. activity or service
                        String componentType = parser.getName();

                        ComponentDescription component = new ComponentDescription(componentName, componentType);

                        // search for associated intent-filter tags
                        while (parser.next() != XmlPullParser.END_TAG && !parser.getName().equals(componentName)) {

                            // start of intent-filter tag
                            if (parser.getEventType() == XmlPullParser.START_TAG && parser.getName() != null
                                    && parser.getName().equals("intent-filter")) {

                                IntentFilterDescription intentFilter = new IntentFilterDescription();

                                // go until end of intent-filter tag
                                while (parser.next() == XmlPullParser.END_TAG && parser.getName() != null
                                        && parser.getName().equals("intent-filter")) {
                                    // parse the action, category or data tag
                                    parseIntentFilterElements(parser, intentFilter);
                                }

                                component.addIntentFilter(intentFilter);
                            }

                            if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName() != null
                                    && parser.getName().equals("intent-filter")) {
                            }

                        }

                        /*

                        ComponentIntentDescription newComponent = ComponentIntentDescription.fromXml(parser);
                        if(newComponent.hasIntentFilters()) // if exported it has at least one default filter
                            provider.addComponent(newComponent);


                         */
                    }
                } else if(parser.getName().equals("activity-alias") && parser.getAttributeValue(null, "permission") == null) {
                    if((parser.getAttributeValue(null, "enabled") == null || parser.getAttributeValue(null, "enabled").equals("true"))
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

    private void parseIntentFilterElements(XmlPullParser parser, IntentFilterDescription intentFilter) {

        // either an action, category or data tag
        String tagName = parser.getName();

        // TODO: extract the necessary attribute values of the given tag and add it to the intent filter

    }
}
