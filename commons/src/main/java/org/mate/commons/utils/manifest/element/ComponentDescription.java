package org.mate.commons.utils.manifest.element;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
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
public class ComponentDescription implements Parcelable {

    /**
     * The package name to which this component belongs to.
     */
    private final String packageName;

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
    private Set<IntentFilterDescription> intentFilters = new HashSet<>();

    /**
     * Whether the component is a dynamic broadcast receiver. Solely relevant for receivers.
     */
    private boolean isDynamicReceiver = false;

    /**
     * Whether the component is an activity alias.
     */
    private boolean isActivityAlias = false;

    /**
     * The name of the target activity that can be launched through the activity-alias. Solely
     * relevant for activity aliases.
     */
    private String targetActivity = null;

    /**
     * String constants discovered through a static analysis of the bytecode, see
     * {@link org.mate.exploration.intent.parsers.IntentInfoParser}.
     */
    private Set<String> stringConstants = new HashSet<>();

    /**
     * Extras (key-value pairs) discovered through a static analysis of the bytecode, see
     * {@link org.mate.exploration.intent.parsers.IntentInfoParser}.
     */
    private Map<String, String> extras = new HashMap<>();

    /**
     * Initialises a new component with the given name and type.
     *
     * @param packageName The package name to which this component belongs to.
     * @param name The component name.
     * @param type The component type, e.g. activity.
     */
    public ComponentDescription(String packageName, String name, String type) {
        this.packageName = packageName;
        this.name = name;
        this.type = ComponentType.mapStringToComponent(type);
    }

    /**
     * Initialises a new component with the given name and type.
     *
     * @param packageName The package name to which this component belongs to.
     * @param name The component name.
     * @param type The component type, e.g. activity.
     */
    public ComponentDescription(String packageName, String name, ComponentType type) {
        this.packageName = packageName;
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
     * Whether the component represents an activity alias.
     *
     * @return Returns {@code true} if the component is an activity alias, otherwise {@code false}
     *          is returned.
     */
    public boolean isActivityAlias() {
        return isActivityAlias;
    }

    /**
     * Controls whether the component is an activity alias or not.
     *
     * @param activityAlias Whether the component is an activity alias or not.
     */
    public void setActivityAlias(boolean activityAlias) {
        isActivityAlias = activityAlias;
    }

    /**
     * Returns the target activity that can be launched through the activity alias.
     *
     * @return Returns the target activity specified in the activity alias.
     */
    public String getTargetActivity() {
        return targetActivity;
    }

    /**
     * Sets the target activity of the activity alias.
     *
     * @param targetActivity The target activity of the activity alias.
     */
    public void setTargetActivity(String targetActivity) {
        this.targetActivity = targetActivity;
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
            return packageName + name;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.name);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeByte(this.exported ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enabled ? (byte) 1 : (byte) 0);
        dest.writeByte(this.handleOnNewIntent ? (byte) 1 : (byte) 0);
        dest.writeTypedList(new ArrayList<>(this.intentFilters));
        dest.writeByte(this.isDynamicReceiver ? (byte) 1 : (byte) 0);
        dest.writeStringList(new ArrayList<>(this.stringConstants));
        dest.writeInt(this.extras.size());
        for (Map.Entry<String, String> entry : this.extras.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    protected ComponentDescription(Parcel in) {
        this.packageName = in.readString();
        this.name = in.readString();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : ComponentType.values()[tmpType];
        this.exported = in.readByte() != 0;
        this.enabled = in.readByte() != 0;
        this.handleOnNewIntent = in.readByte() != 0;
        this.intentFilters =
                new HashSet<>(in.createTypedArrayList(IntentFilterDescription.CREATOR));
        this.isDynamicReceiver = in.readByte() != 0;
        this.stringConstants = new HashSet<>(in.createStringArrayList());
        int extrasSize = in.readInt();
        this.extras = new HashMap<String, String>(extrasSize);
        for (int i = 0; i < extrasSize; i++) {
            String key = in.readString();
            String value = in.readString();
            this.extras.put(key, value);
        }
    }

    public static final Creator<ComponentDescription> CREATOR = new Creator<ComponentDescription>() {
        @Override
        public ComponentDescription createFromParcel(Parcel source) {
            return new ComponentDescription(source);
        }

        @Override
        public ComponentDescription[] newArray(int size) {
            return new ComponentDescription[size];
        }
    };
}
