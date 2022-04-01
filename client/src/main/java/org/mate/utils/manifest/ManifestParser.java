package org.mate.utils.manifest;

import android.util.Xml;

import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.manifest.Manifest;
import org.mate.commons.utils.manifest.element.ComponentDescription;
import org.mate.commons.utils.manifest.element.IntentFilterDescription;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A parser for the AndroidManifest.xml file.
 */
public final class ManifestParser {

    /**
     * The location of the AndroidManifest.xml file.
     */
    private static final File MANIFEST_FILE
            = new File("/data/data/org.mate/AndroidManifest.xml");

    private ManifestParser() {
        throw new UnsupportedOperationException("Trying to instantiate utility class!");
    }

    /**
     * Parses the AndroidManifest.xml file that needs to be located within the app internal
     * storage of the MATE app.
     *
     * @return Returns the parsed manifest.
     * @throws XmlPullParserException Should never happen.
     * @throws IOException Should never happen.
     */
    public static Manifest parseManifest(String packageName) throws XmlPullParserException, IOException {

        InputStream inputStream = new FileInputStream(MANIFEST_FILE);

        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(inputStream, null);

        // verify that the package name of the manifest matches the package name of the AUT
        parser.nextTag();
        parser.require(XmlPullParser.START_TAG, null, "manifest");
        if (!parser.getAttributeValue(null, "package").equals(packageName)) {
            throw new IllegalArgumentException("Wrong manifest file! Found package was "
                    + parser.getAttributeValue(null, "package")
                    + ", but should be " + packageName);
        }

        final List<ComponentDescription> components = new ArrayList<>();
        ComponentDescription currentComponent = null;
        IntentFilterDescription currentIntentFilter = null;
        String mainActivity = null;

        // parse each component tag
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {

                final String startTag = parser.getName();

                switch (startTag) {
                    case "activity":
                    case "activity-alias":
                    case "service":
                    case "receiver":

                        boolean exported = parser.getAttributeValue(null, "exported") != null
                                && parser.getAttributeValue(null, "exported").equals("true");

                        boolean enabled = parser.getAttributeValue(null, "enabled") == null
                                || parser.getAttributeValue(null, "enabled").equals("true");

                        // parse the component name (android:name)
                        String componentName = parser.getAttributeValue(null, "name");

                        // parse the component type, e.g. activity
                        String componentType = parser.getName();

                        currentComponent = new ComponentDescription(packageName, componentName,
                                componentType);
                        currentComponent.setExported(exported);
                        currentComponent.setEnabled(enabled);

                        if (startTag.equals("activity-alias")) {
                            currentComponent.setActivityAlias(true);
                            final String targetActivity
                                    = parser.getAttributeValue(null, "targetActivity");
                            currentComponent.setTargetActivity(targetActivity);
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
                    case "activity-alias":
                    case "service":
                    case "receiver":
                        if (currentComponent != null && currentComponent.hasIntentFilter()) {
                            /*
                             * According to the official docs, a component that defines an intent
                             * filter should be exported. However, it is unclear whether an explicit
                             * 'false' value overrides this behaviour.
                             */
                            currentComponent.setExported(true);
                        }

                        components.add(currentComponent);
                        currentComponent = null;
                        break;
                    case "intent-filter":
                        if (currentComponent != null && currentIntentFilter != null) {
                            currentComponent.addIntentFilter(currentIntentFilter);
                            if (mainActivity == null && describesMainActivity(currentIntentFilter)) {
                                mainActivity = currentComponent.getFullyQualifiedName();
                            }
                            currentIntentFilter = null;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return new Manifest(packageName, components, mainActivity);
    }

    /**
     * Checks whether the given intent filter describes the main activity.
     *
     * @param intentFilter The given intent filter.
     * @return Returns {@code true} if the intent filter describes the main activity, otherwise
     *         {@code false} is returned.
     */
    private static boolean describesMainActivity(final IntentFilterDescription intentFilter) {
        return intentFilter.getActions().contains("android.intent.action.MAIN")
                && intentFilter.getCategories().contains("android.intent.category.LAUNCHER");
    }

    /**
     * Parses an intent-filter tag for the nested tags action, category or data. If such tag is
     * found, all its relevant attributes are retrieved.
     *
     * @param parser The parser referring currently to the first tag within the intent-filter tag.
     * @param intentFilter An intent-filter storing action, category and data tags.
     */
    private static void parseIntentFilterElements(final XmlPullParser parser,
                                                  final IntentFilterDescription intentFilter) {

        String tagName = parser.getName();

        switch (tagName) {
            case "action":
                intentFilter.addAction(parser.getAttributeValue(null, "name"));
                break;
            case "category":
                intentFilter.addCategory(parser.getAttributeValue(null, "name"));
                break;
            case "data":
                String scheme = parser.getAttributeValue(null, "scheme");
                String host = parser.getAttributeValue(null, "host");
                String port = parser.getAttributeValue(null, "port");
                String path = parser.getAttributeValue(null, "path");
                String pathPattern = parser.getAttributeValue(null, "pathPattern");
                String pathPrefix = parser.getAttributeValue(null, "pathPrefix");
                String mimeType = parser.getAttributeValue(null, "mimeType");
                intentFilter.addData(scheme, host, port, path, pathPattern, pathPrefix, mimeType);
                break;
            default:
                MATELog.log_warn("Tag " + tagName + " not supported yet!");
                break;
        }
    }
}
