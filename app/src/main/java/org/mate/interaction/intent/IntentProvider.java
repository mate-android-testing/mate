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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private List<ComponentDescription> components;

    private static final List<String> systemEvents = SystemEventParser.loadSystemEvents();
    private List<ComponentDescription> systemEventReceivers = new ArrayList<>();

    public IntentProvider() {
        parseXMLFiles();
    }

    /**
     * Parses both the AndroidManifest.xml and the static intent info file. In addition,
     * filters out receivers describing system events.
     */
    private void parseXMLFiles() {

        try {
            // extract all exported and enabled components declared in the manifest
            components = ComponentParser.parseManifest();

            // filter out system event intent filters
            systemEventReceivers = ComponentParser.filterSystemEventIntentFilters(components, systemEvents);

            // add information about bundle entries and extracted string constants
            IntentInfoParser.parseIntentInfoFile(components);

            // TODO: consider to parse dynamically registered broadcast receivers
            MATE.log_acc("Derived the following components: " + components);
        } catch (XmlPullParserException | IOException e) {
            MATE.log_acc("Couldn't parse the AndroidManifest/staticInfoIntent file!");
            throw new IllegalStateException(e);
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
     * Checks  whether we have a broadcast receivers that reacts to
     * system events.
     *
     * @return Returns {@code true} if a receiver listens for a system event,
     *      otherwise {@code false}.
     */
    public boolean hasSystemEvent() {
        return !systemEventReceivers.isEmpty();
    }

    /**
     * Generates a random system event that is received by at least one broadcast receiver.
     *
     * @return Returns the corresponding action describing the system event.
     */
    public SystemAction getSystemEvent() {
        if (!hasSystemEvent()) {
            throw new IllegalStateException("No broadcast receiver is listening for system events!");
        } else {
            ComponentDescription component = Randomness.randomElement(systemEventReceivers);
            IntentFilterDescription intentFilter = Randomness.randomElement(component.getIntentFilters());
            String action = Randomness.randomElement(intentFilter.getActions());
            return new SystemAction(component.getFullyQualifiedName(), action);
        }
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

        // add random categories if present
        if (intentFilter.hasCategory()) {

            Set<String> categories = intentFilter.getCategories();

            final double ALPHA = 1;
            double decreasingFactor = ALPHA;
            Random random = new Random();

            // add with decreasing probability categories (at least one)
            while (random.nextDouble() < ALPHA / decreasingFactor) {
                String category = Randomness.randomElement(categories);
                intent.addCategory(category);
                decreasingFactor /= 2;

                // we reached the maximal amount of categories we can add
                if (intent.getCategories().size() == categories.size()) {
                    break;
                }
            }
        }

        // add a data tag
        if (intentFilter.hasData()) {
            // TODO: consider integration of mimeType -> should be derived automatically otherwise
            Uri uri = intentFilter.getData().generateRandomUri();
            if (uri != null) {
                intent.setData(uri);
            }
        }

        // make every intent explicit
        intent.setComponent(new ComponentName(MATE.packageName, component.getFullyQualifiedName()));

        // construct suitable key-value pairs
        if (component.hasExtra()) {
            intent.putExtras(component.generateRandomBundle());
        }

        // TODO: may add flags, e.g. FLAG_ACTIVITY_NEW_TASK
        return intent;
    }

    /**
     * Returns the components representing a specific type.
     *
     * @param componentType The type of component, e.g. an activity.
     * @return Returns the list of components matching the given type.
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
}
