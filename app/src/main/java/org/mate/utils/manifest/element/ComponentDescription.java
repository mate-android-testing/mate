package org.mate.utils.manifest.element;

import org.mate.Registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Describes an app component declared in the AndroidManifest.xml.
 */
public class ComponentDescription {

    /**
     * The name of the component.
     */
    private final String name;

    /**
     * The type of the component, e.g. activity.
     */
    private final ComponentType type;

    /**
     * Whether the component has been exported or not.
     */
    private boolean exported;

    /**
     * Whether the component has been enabled or not.
     */
    private boolean enabled;

    /**
     * Whether the component handles 'onNewIntent'. Solely relevant for activities.
     */
    private boolean handleOnNewIntent = false;

    /**
     * The set of intent filters associated with the component.
     */
    private final Set<IntentFilterDescription> intentFilters = new HashSet<>();

    /**
     * Whether the component is a dynamic broadcast receiver. Solely relevant for receivers.
     */
    private boolean isDynamicReceiver = false;

    /**
     * String constants discovered through a static analysis of the bytecode, see
     * {@link org.mate.exploration.intent.parsers.IntentInfoParser}.
     */
    private final Set<String> stringConstants = new HashSet<>();

    /**
     * Extras (key-value pairs) discovered through a static analysis of the bytecode, see
     * {@link org.mate.exploration.intent.parsers.IntentInfoParser}.
     */
    private final Map<String, String> extras = new HashMap<>();

    /**
     * Initialises a new component with the given name and type.
     *
     * @param name The component name.
     * @param type The component type, e.g. activity.
     */
    public ComponentDescription(String name, String type) {
        this.name = name;
        this.type = ComponentType.mapStringToComponent(type);
    }

    /**
     * Initialises a new component with the given name and type.
     *
     * @param name The component name.
     * @param type The component type, e.g. activity.
     */
    public ComponentDescription(String name, ComponentType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Whether the component has been exported or not.
     *
     * @return Returns {@code true} if the component has been exported, otherwise {@code false} is
     *         returned.
     */
    public boolean isExported() {
        return exported;
    }

    /**
     * Sets the 'exported' flag of the component.
     *
     * @param exported The new exported value.
     */
    public void setExported(boolean exported) {
        this.exported = exported;
    }

    /**
     * Whether the component has been enabled or not.
     *
     * @return Returns {@code true} if the component has been enabled, otherwise {@code false} is
     *         returned.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the 'enabled' flag of the component.
     *
     * @param enabled The new enabled value.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Whether the component is a dynamic broadcast receiver.
     *
     * @return Returns {@code true} if the component is a dynamic receiver, otherwise {@code false} is
     *         returned.
     */
    public boolean isDynamicReceiver() {
        return isBroadcastReceiver() && isDynamicReceiver;
    }

    /**
     * Controls whether the component is a dynamic broadcast receiver.
     *
     * @param dynamicReceiver {@code true} if the receiver is dynamic, otherwise {@code false}.
     */
    public void setDynamicReceiver(boolean dynamicReceiver) {
        isDynamicReceiver = dynamicReceiver;
    }

    /**
     * Whether the activity reacts to the onNewIntent() method.
     *
     * @return Returns {@code true} if the activity reacts to the onNewIntent() method, otherwise
     *         {@code false} is returned.
     */
    public boolean isHandlingOnNewIntent() {
        return handleOnNewIntent;
    }

    /**
     * Sets whether the component (activity) reacts to the onNewIntent() method.
     *
     * @param handleOnNewIntent Whether the components reacts to onNewIntent().
     */
    public void setHandlingOnNewIntent(boolean handleOnNewIntent) {
        this.handleOnNewIntent = handleOnNewIntent;
    }

    /**
     * Adds a set of string constants to the component.
     *
     * @param stringConstants The string constants to be added.
     */
    public void addStringConstants(Set<String> stringConstants) {
        this.stringConstants.addAll(stringConstants);
    }

    /**
     * Adds a set of intent filters to the component.
     *
     * @param intentFilters The set of intent filters to be added.
     */
    public void addIntentFilters(Set<IntentFilterDescription> intentFilters) {
        this.intentFilters.addAll(intentFilters);
    }

    /**
     * Adds extras (key-value pairs) to the component.
     *
     * @param extras The extras to be added.
     */
    public void addExtras(Map<String, String> extras) {
        this.extras.putAll(extras);
    }

    /**
     * Whether this component represents an activity.
     *
     * @return Returns {@code true} if this component represents an activity, otherwise {@code false}
     *         is returned.
     */
    public boolean isActivity() {
        return type == ComponentType.ACTIVITY;
    }

    /**
     * Whether this component represents a service.
     *
     * @return Returns {@code true} if this component represents a service, otherwise {@code false}
     *         is returned.
     */
    public boolean isService() {
        return type == ComponentType.SERVICE;
    }

    /**
     * Whether this component represents a broadcast receiver.
     *
     * @return Returns {@code true} if this component represents a receiver, otherwise {@code false}
     *         is returned.
     */
    public boolean isBroadcastReceiver() {
        return type == ComponentType.BROADCAST_RECEIVER;
    }

    /**
     * Whether this component represents a content provider.
     *
     * @return Returns {@code true} if this component represents a provider, otherwise {@code false}
     *         is returned.
     */
    public boolean isContentProvider() {
        return type == ComponentType.CONTENT_PROVIDER;
    }

    /**
     * Whether this component defines any intent filters.
     *
     * @return Returns {@code true} if this component defines any intent filters, otherwise
     *         {@code false} is returned.
     */
    public boolean hasIntentFilter() {
        return !intentFilters.isEmpty();
    }

    /**
     * Whether this component defines any extras.
     *
     * @return Returns {@code true} if this component defines any extras, otherwise {@code false}
     *         is returned.
     */
    public boolean hasExtras() {
        return !extras.isEmpty();
    }

    /**
     * Whether this component defines any string constants.
     *
     * @return Returns {@code true} if this component defines any string constants, otherwise {@code false}
     *         is returned.
     */
    public boolean hasStringConstants() {
        return !stringConstants.isEmpty();
    }

    /**
     * Removes the given set of intent filters from this component.
     *
     * @param intentFilters The set of intent filters that should be removed.
     */
    public void removeIntentFilters(Collection<IntentFilterDescription> intentFilters) {
        this.intentFilters.removeAll(intentFilters);
    }

    /**
     * Returns the type of the component. Note that activity-aliases are considered as activities.
     *
     * @return Returns the component's type.
     */
    public ComponentType getType() {
        return type;
    }

    /**
     * Adds the given intent filter to this component.
     *
     * @param intentFilter The intent filter to be added.
     */
    public void addIntentFilter(IntentFilterDescription intentFilter) {
        intentFilters.add(intentFilter);
    }

    /**
     * Returns the FQN of the component. That is package name + class name.
     *
     * @return Returns the FQN of the component.
     */
    public String getFullyQualifiedName() {

        if (name.startsWith(".")) {
            return Registry.getPackageName() + name;
        } else {
            return name;
        }
    }

    /**
     * Retrieves a component from a list of components by name.
     *
     * @param components The list of components.
     * @param name The name of the component to be looked up.
     * @return Returns the component matching the given name in the list of components
     *         or {@code null} if the component couldn't be found.
     */
    public static ComponentDescription getComponentByName(
            final List<ComponentDescription> components, final String name) {

        for (ComponentDescription component : components) {
            if (component.getFullyQualifiedName().equals(name)) {
                return component;
            }
        }
        return null;
    }

    /**
     * Returns the set of attached intent filters.
     *
     * @return Returns the attached intent filters.
     */
    public Set<IntentFilterDescription> getIntentFilters() {
        return Collections.unmodifiableSet(intentFilters);
    }

    /**
     * Returns a map describing the key-value entries of a possible attached bundle.
     *
     * @return Returns the extra (the bundle object).
     */
    public Map<String, String> getExtras() {
        return Collections.unmodifiableMap(extras);
    }

    /**
     * Returns the string constants of this component.
     *
     * @return Returns the attached string constants.
     */
    public Set<String> getStringConstants() {
        return Collections.unmodifiableSet(stringConstants);
    }

    /**
     * Provides a textual representation of the component.
     *
     * @return Returns the string representation of the component.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Component: " + name + System.lineSeparator());
        builder.append("Type: " + type + System.lineSeparator());

        builder.append("Intent Filters: " + System.lineSeparator());
        builder.append("-------------------------------------------" + System.lineSeparator());
        for (IntentFilterDescription intentFilter : intentFilters) {
            builder.append(intentFilter + System.lineSeparator());
        }
        builder.append("-------------------------------------------" + System.lineSeparator());

        builder.append("Strings: " + stringConstants + System.lineSeparator());
        builder.append("Extras: " + extras + System.lineSeparator());

        if (isActivity()) {
            builder.append("Handles OnNewIntent: " + handleOnNewIntent + System.lineSeparator());
        }

        return builder.toString();
    }
}
