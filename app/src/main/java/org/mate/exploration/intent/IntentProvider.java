package org.mate.exploration.intent;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.intent.parsers.IntentInfoParser;
import org.mate.exploration.intent.parsers.SystemActionParser;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.intent.IntentBasedAction;
import org.mate.interaction.action.intent.SystemAction;
import org.mate.utils.Randomness;
import org.mate.utils.manifest.element.ComponentDescription;
import org.mate.utils.manifest.element.ComponentType;
import org.mate.utils.manifest.element.IntentFilterDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A factory to generate intents matching the specified component and intent filter.
 */
public class IntentProvider {

    /**
     * The list of enabled and exported components that can be launched through an intent.
     */
    private final List<ComponentDescription> components;

    /**
     * The list of supported system events.
     */
    private final List<String> systemEventActions;

    /**
     * The list of broadcast receivers that react to system events.
     */
    private final List<ComponentDescription> systemEventReceivers;

    /**
     * The list of dynamic broadcast receivers.
     */
    private final List<ComponentDescription> dynamicReceivers;

    /**
     * Initialises the intent provider with the components and the supported system events.
     */
    public IntentProvider() {

        components = IntentInfoParser.parseIntentInfoFile(
                filterComponents(Registry.getManifest().getComponents().stream()
                .filter(component -> component.isEnabled() && component.isExported())
                .filter(component -> !component.isContentProvider())
                .collect(Collectors.toList())));

        systemEventActions = SystemActionParser.parseSystemEventActions();
        systemEventReceivers = extractSystemEventReceivers(components, systemEventActions);

        /*
        * TODO: We may need to derive the dynamic receivers before the system event receivers,
        *  since extractSystemEventReceivers() potentially removes components, see the inline
        *  comments!
         */
        dynamicReceivers = components.stream()
                .filter(ComponentDescription::isDynamicReceiver)
                // only receivers that define at least one intent filter are exported by default
                .filter(ComponentDescription::hasIntentFilter)
                .collect(Collectors.toList());

        MATE.log("Derived the following components: " + components);
        MATE.log("Derived the following system event receivers: " + systemEventReceivers);
        MATE.log("Derived the following dynamic receivers: " + dynamicReceivers);
    }

    /**
     * Checks whether an intent-filter describes a system event by comparing
     * the included actions against the list of system event actions.
     *
     * @param systemEvents The list of possible system events.
     * @param intentFilter The given intent-filter.
     * @return Returns {@code true} if the intent-filter describes a system event,
     *         otherwise {@code false}.
     */
    private boolean describesSystemEvent(List<String> systemEvents, IntentFilterDescription intentFilter) {

        for (String action : intentFilter.getActions()) {
            if (systemEvents.contains(action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts the system event receivers by inspecting the intent filters of the components.
     * Note that broadcast receivers solely reacting to system events are removed from the component
     * list.
     *
     * @param components The list of components.
     * @param systemEvents The list of system events supported on the given API.
     * @return Returns the list of system event receivers.
     */
    private List<ComponentDescription> extractSystemEventReceivers(List<ComponentDescription> components,
                                                                   List<String> systemEvents) {

        List<ComponentDescription> systemEventReceivers = new ArrayList<>();
        List<ComponentDescription> receiversToBeRemoved = new ArrayList<>();

        for (ComponentDescription component : components) {

            if (component.isBroadcastReceiver()) {

                // assume that we deal with a system event receiver
                ComponentDescription systemEventReceiver =
                        new ComponentDescription(component.getFullyQualifiedName(),
                                ComponentType.BROADCAST_RECEIVER);

                // track which intent filters need to be removed from the original component later
                List<IntentFilterDescription> systemEventFilters = new ArrayList<>();

                // collect the intent filters that describe system events
                for (IntentFilterDescription intentFilter : component.getIntentFilters()) {

                    /*
                    * TODO: If an intent filter contains multiple actions (which might be the case
                    *  for dynamic receivers, since those can only define a single filter), the
                    *  most correct option would be to split the intent filter into multiple filters
                    *  separating the actions from system event actions. Right now, we remove the
                    *  complete intent filter from the original component, which might contain a
                    *  mixture of system and intent-based actions.
                     */
                    if (describesSystemEvent(systemEvents, intentFilter)) {

                        systemEventReceiver.addIntentFilter(intentFilter);

                        // intent filter needs to be removed from original component afterwards
                        systemEventFilters.add(intentFilter);
                    }
                }

                // if we added an intent filter, the receiver is really a system event receiver
                if (!systemEventFilters.isEmpty()) {
                    systemEventReceivers.add(systemEventReceiver);

                    // remove the system event filters from the original component
                    component.removeIntentFilters(systemEventFilters);
                }

                /*
                * TODO: Only remove the receiver if really all actions contained in the intent
                *  filter(s) describe system events, see the above comment. Right now, we remove
                *  the receiver if at least a single action per intent filter refers to a system
                *  event. Hence, we may drop a (dynamic) receiver at this point, which might react
                *  to both kinds of actions.
                 */
                if (!component.hasIntentFilter()) {
                    /*
                     * If a broadcast receiver solely reacts to system events, we can't trigger it with
                     * a simple intent. Thus, we consider this receiver as a component that is not exported
                     * and remove it from the list of components.
                     */
                    receiversToBeRemoved.add(component);
                }
            }
        }

        MATE.log_debug("Removing components: " + components.removeAll(receiversToBeRemoved));
        return systemEventReceivers;
    }

    /**
     * Removes components that can't or shouldn't be targeted. This includes the tracer (receiver)
     * that we included to measure coverage as well as services and receivers from the Google
     * Analytics SDK.
     *
     * @param components The list of components parsed from the manifest.
     */
    private List<ComponentDescription> filterComponents(List<ComponentDescription> components) {

        List<ComponentDescription> toBeRemoved = new ArrayList<>();

        final String TRACER_PACKAGE = "de.uni_passau.fim.auermich";

        /*
         * These components are often not available because the Google Analytics SDK is missing on
         * emulators. Moreover, those components don't belong to the app's core functionality.
         */
        final Set<String> GOOGLE_ANALYTICS_COMPONENTS = new HashSet<String>() {{
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
            if (component.getFullyQualifiedName().startsWith(TRACER_PACKAGE)
                    || GOOGLE_ANALYTICS_COMPONENTS.contains(component.getFullyQualifiedName())) {
                toBeRemoved.add(component);
            }
        }

        components.removeAll(toBeRemoved);
        return components;
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
    public List<String> getSystemEventActions() {
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
     * Returns whether the set of components contains a service that can be invoked.
     *
     * @return Returns {@code true} if a service component is contained in the set of components,
     *         otherwise {@code false}.
     */
    public boolean hasService() {
        return components.stream().anyMatch(ComponentDescription::isService);
    }

    /**
     * Checks whether there is a dynamically registered broadcast receiver available.
     *
     * @return Returns {@code true} if a dynamically registered receiver could be found,
     *         otherwise {@code false} is returned.
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
            // select randomly a dynamic receiver
            ComponentDescription component = Randomness.randomElement(dynamicReceivers);
            IntentFilterDescription intentFilter = Randomness.randomElement(component.getIntentFilters());

            // we need to distinguish between a dynamic system receiver and dynamic receiver
            if (describesSystemEvent(systemEventActions, intentFilter)) {
                /*
                 * TODO: If the randomly selected action actually refers to a system event, we
                 *  require a system action only, otherwise this is a regular intent-based action.
                 */
                String action = Randomness.randomElement(intentFilter.getActions());
                SystemAction systemAction = new SystemAction(component, intentFilter, action);
                systemAction.markAsDynamic();
                return systemAction;
            } else {
                return generateIntentBasedAction(component, true);
            }
        }
    }

    /**
     * Returns whether the set of components contains a broadcast receiver that can be invoked.
     *
     * @return Returns {@code true} if a broadcast receiver component is contained in the set of
     *         components, otherwise {@code false} is returned.
     */
    public boolean hasBroadcastReceiver() {
        return components.stream().anyMatch(ComponentDescription::isBroadcastReceiver);
    }

    /**
     * Returns whether the set of components contains an activity that can be invoked.
     *
     * @return Returns {@code true} if an activity component is contained in the set of components,
     *         otherwise {@code false} is returned.
     */
    public boolean hasActivity() {
        return components.stream().anyMatch(ComponentDescription::isActivity);
    }

    /**
     * Checks  whether we have a broadcast receivers that reacts to system events.
     *
     * @return Returns {@code true} if a receiver listens for a system event, otherwise {@code false}.
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
     *         otherwise {@code false} is returned.
     */
    public boolean isCurrentActivityHandlingOnNewIntent() {

        String name = Registry.getUiAbstractionLayer().getCurrentActivity();
        String[] tokens = name.split("/");

        if (tokens.length < 2) {
            // couldn't fetch current activity name properly
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

        String name = Registry.getUiAbstractionLayer().getCurrentActivity();
        String[] tokens = name.split("/");

        if (tokens.length < 2) {
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
     *         the selected intent-filter and the generated intent.
     */
    private IntentBasedAction generateIntentBasedAction(ComponentDescription component,
                                                        boolean dynamicReceiver, boolean handleOnNewIntent) {

        /*
        * There are components that were explicitly exported although they don't offer any intent
        * filter, e.g. activity-aliases. In this case we simply construct an explicit intent without
        * any further attributes.
         */
        if (!component.hasIntentFilter()) {
            MATE.log_debug("Targeting component without intent filter: " + component.getFullyQualifiedName());
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(Registry.getPackageName(), component.getFullyQualifiedName()));
            return new IntentBasedAction(intent, component, new IntentFilterDescription());
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
            Uri uri = DataUriGenerator.generateRandomUri(intentFilter.getData());
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
            intent.setPackage(Registry.getPackageName());
        } else {
            // make every other intent explicit
            intent.setComponent(new ComponentName(Registry.getPackageName(), component.getFullyQualifiedName()));
        }

        // construct suitable key-value pairs
        if (component.hasExtras()) {
            intent.putExtras(BundleGenerator.generateRandomBundle(component));
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
