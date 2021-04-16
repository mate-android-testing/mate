package org.mate.interaction.action.intent;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.interaction.action.Action;
import org.mate.interaction.EnvironmentManager;
import org.mate.utils.Randomness;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class IntentProvider {

    private List<ComponentDescription> components;
    private static final List<String> systemEventActions = SystemActionParser.loadSystemEventActions();
    private List<ComponentDescription> systemEventReceivers = new ArrayList<>();
    private List<ComponentDescription> dynamicReceivers = new ArrayList<>();

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

            // remove certain components, e.g. our custom tracer or google analytics receivers
            filterComponents(components);

            // add information about bundle entries and extracted string constants + collect dynamic receivers
            IntentInfoParser.parseIntentInfoFile(components, dynamicReceivers);

            // filter out system event intent filters
            systemEventReceivers = ComponentParser.filterSystemEventIntentFilters(components, systemEventActions);

            MATE.log("Derived the following components: " + components);
            MATE.log("Derived the following system event receivers: " + systemEventReceivers);
            MATE.log("Derived the following dynamic receivers: " + dynamicReceivers);
        } catch (XmlPullParserException | IOException e) {
            MATE.log("Couldn't parse the AndroidManifest/staticInfoIntent file!");
            throw new IllegalStateException(e);
        }
    }

    /**
     * We don't want to interfere with the tracer that is responsible for branch coverage/distance.
     * Likewise, there are certain receivers that rely on additional dependencies, which are
     * typically not available on the emulator, e.g. the Google Analytics SDK. Thus, we simply
     * remove those components from the targets of intents.
     *
     * @param components The list of components parsed from the manifest.
     */
    private void filterComponents(List<ComponentDescription> components) {

        List<ComponentDescription> toBeRemoved = new ArrayList<>();

        final String BRANCH_COVERAGE_TRACER = "de.uni_passau.fim.auermich.branchcoverage.tracer.Tracer";
        final String BRANCH_DISTANCE_TRACER = "de.uni_passau.fim.auermich.branchdistance.tracer.Tracer";
        final String TRACER = "de.uni_passau.fim.auermich.tracer.Tracer";

        /*
        * These components are often not available because the Google Analytics SDK is
        * missing on emulators. Moreover, those components don't belong to the app's
        * core functionality.
         */
        final Set<String> GOOGLE_ANALYTICS_COMPONENTS = new HashSet() {{
           add("com.google.android.gms.analytics.AnalyticsReceiver");
           add("com.google.android.gms.analytics.CampaignTrackingReceiver");
           add("com.google.android.apps.analytics.AnalyticsReceiver");
           add("com.google.android.gms.measurement.AppMeasurementReceiver");
           add("com.google.android.gms.measurement.AppMeasurementInstallReferrerReceiver");
           add("com.google.android.gms.measurement.AppMeasurementService");
           add("com.google.android.gms.measurement.AppMeasurementJobService");
           add("com.google.firebase.iid.FirebaseInstanceIdReceiver");
        }};

        for (ComponentDescription component : components) {
            if (component.getFullyQualifiedName().equals(BRANCH_COVERAGE_TRACER)
                    || component.getFullyQualifiedName().equals(BRANCH_DISTANCE_TRACER)
                    || component.getFullyQualifiedName().equals(TRACER)
                    || GOOGLE_ANALYTICS_COMPONENTS.contains(component.getFullyQualifiedName())) {
                toBeRemoved.add(component);
            }
        }

        components.removeAll(toBeRemoved);
    }

    /**
     * Returns the list of retrieved components.
     *
     * @return Returns the list of components.
     */
    public List<ComponentDescription> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * Returns the list of system event actions.
     *
     * @return Returns the list of actions describing system events.
     */
    public static List<String> getSystemEventActions() {
        return Collections.unmodifiableList(systemEventActions);
    }


    /**
     * Returns the list of broadcast receivers handling system events.
     *
     * @return Returns the list of broadcast receivers handling system events.
     */
    public List<ComponentDescription> getSystemEventReceivers() {
        return Collections.unmodifiableList(systemEventReceivers);
    }

    /**
     * Returns the list of dynamic broadcast receivers.
     *
     * @return Returns the list of dynamic broadcast receivers.
     */
    public List<ComponentDescription> getDynamicReceivers() {
        return Collections.unmodifiableList(dynamicReceivers);
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
     * Checks whether there is a dynamically registered broadcast receiver available.
     *
     * @return Returns {@code true} if a dynamically registered receiver could be found,
     *          otherwise {@code false} is returned.
     */
    public boolean hasDynamicReceiver() {
        return !dynamicReceivers.isEmpty();
    }

    /**
     * Returns an action which triggers a dynamically registered broadcast receiver.
     *
     * @return Returns an action encapsulating a dynamic broadcast receiver.
     */
    public Action getDynamicReceiverAction() {
        if (!hasDynamicReceiver()) {
            throw new IllegalStateException("No dynamic broadcast receiver found!");
        } else {
            // select randomly dynamic receiver
            ComponentDescription component = Randomness.randomElement(dynamicReceivers);
            IntentFilterDescription intentFilter = Randomness.randomElement(component.getIntentFilters());

            // we need to distinguish between a dynamic system receiver and dynamic receiver
            if (describesSystemEvent(systemEventActions, intentFilter)) {
                // use system event action
                String action = Randomness.randomElement(intentFilter.getActions());
                SystemAction systemAction = new SystemAction(component, intentFilter, action);
                systemAction.markAsDynamic();
                return systemAction;
            } else {
                // use intent based action
                return generateIntentBasedAction(component, true);
            }
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
    private boolean describesSystemEvent(List<String> systemEvents, IntentFilterDescription intentFilter) {

        for(String action : intentFilter.getActions()) {
            if (systemEvents.contains(action)) {
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
    public boolean hasSystemEventReceiver() {
        return !systemEventReceivers.isEmpty();
    }

    /**
     * Generates a random system event that is received by at least one broadcast receiver.
     *
     * @return Returns the corresponding action describing the system event.
     */
    public SystemAction getSystemEventAction() {
        if (!hasSystemEventReceiver()) {
            throw new IllegalStateException("No broadcast receiver is listening for system events!");
        } else {
            ComponentDescription component = Randomness.randomElement(systemEventReceivers);
            IntentFilterDescription intentFilter = Randomness.randomElement(component.getIntentFilters());
            String action = Randomness.randomElement(intentFilter.getActions());
            return new SystemAction(component, intentFilter, action);
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
        return generateIntentBasedAction(component);
    }

    /**
     * Checks whether the currently visible activity defines a callback for onNewIntent().
     *
     * @return Returns {@code true} if the current activity implements onNewIntent(),
     *          otherwise {@code false} is returned.
     */
    public boolean isCurrentActivityHandlingOnNewIntent() {

        String name = Registry.getEnvironmentManager().getCurrentActivityName();

        String[] tokens = name.split("/");

        if (name.equals(EnvironmentManager.ACTIVITY_UNKNOWN) || tokens.length < 2) {
            return false;
        }

        String packageName = tokens[0];
        String activity = tokens[1];

        if (activity.startsWith(".")) {
            activity = packageName + activity;
        }

        MATE.log("Current visible Activity is: " + activity);
        ComponentDescription component = ComponentDescription.getComponentByName(components, activity);

        return component != null && component.isActivity() && component.isHandlingOnNewIntent();
    }

    /**
     * Creates an IntentBasedAction that triggers the currently visible activity's onNewIntent method.
     * Should only be called when {@link #isCurrentActivityHandlingOnNewIntent()} yields {@code true}.
     *
     * @return Returns an IntentBasedAction that triggers the currently visible activity's onNewIntent method.
     */
    public IntentBasedAction generateIntentBasedActionForCurrentActivity() {

        String name = Registry.getEnvironmentManager().getCurrentActivityName();

        String[] tokens = name.split("/");

        if (name.equals(EnvironmentManager.ACTIVITY_UNKNOWN) || tokens.length < 2) {
            throw new IllegalStateException("Couldn't retrieve name of current activity!");
        }

        String packageName = tokens[0];
        String activity = tokens[1];

        if (activity.startsWith(".")) {
            activity = packageName + activity;
        }

        ComponentDescription component = ComponentDescription.getComponentByName(components, activity);

        if (component == null) {
            throw new IllegalStateException("No component description found for current activity!");
        }
        return generateIntentBasedAction(component, false, true);
    }

    private IntentBasedAction generateIntentBasedAction(ComponentDescription component) {
        return generateIntentBasedAction(component, false, false);
    }

    private IntentBasedAction generateIntentBasedAction(ComponentDescription component, boolean dynamicReceiver) {
        return generateIntentBasedAction(component, dynamicReceiver, false);
    }

    /**
     * Creates and fills an intent with information retrieved from the given component
     * in a random fashion. Returns the corresponding IntentBasedAction.
     *
     * @param component The component information.
     * @param dynamicReceiver Whether the component is a dynamic receiver.
     * @param handleOnNewIntent Whether the intent should trigger the onNewIntent method.
     * @return Returns the corresponding IntentBasedAction encapsulating the component,
     *          the selected intent-filter and the generated intent.
     */
    private IntentBasedAction generateIntentBasedAction(ComponentDescription component,
                                                        boolean dynamicReceiver, boolean handleOnNewIntent) {

        if (!component.hasIntentFilter()) {
            MATE.log("Component " + component + " doesn't declare any intent-filter!");
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
            //  the mimeType needs to be set in one pass together with the URI, see intent.setType()!
            Uri uri = intentFilter.getData().generateRandomUri();
            if (uri != null) {
                intent.setData(uri);
            }
        }

        /*
        * Android forbids to send an explicit intent to a dynamically registered broadcast receiver.
        * We can only specify the package-name to restrict the number of possible receivers of
        * the intent.
         */
        if (dynamicReceiver) {
            // will result in implicit resolution restricted to application package
            intent.setPackage(MATE.packageName);
        } else {
            // make every other intent explicit
            intent.setComponent(new ComponentName(MATE.packageName, component.getFullyQualifiedName()));
        }

        // construct suitable key-value pairs
        if (component.hasExtra()) {
            intent.putExtras(component.generateRandomBundle());
        }

        // trigger onNewIntent() instead of onCreate() (solely for activities)
        if (component.isHandlingOnNewIntent() && handleOnNewIntent) {
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        return new IntentBasedAction(intent, component, intentFilter);
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
        return Collections.unmodifiableList(targetComponents);
    }
}
