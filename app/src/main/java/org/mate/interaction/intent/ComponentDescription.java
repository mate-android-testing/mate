package org.mate.interaction.intent;

import org.mate.MATE;

import java.util.HashSet;
import java.util.Set;

class ComponentDescription {

    private final String name;
    private final ComponentType type;

    // a component may define optionally intent filter tags
    private Set<IntentFilterDescription> intentFilters = new HashSet<>();

    ComponentDescription(String name, String type) {
        this.name = name;
        this.type = ComponentType.mapStringToComponent(type);
    }

    boolean isActivity() {
        return type == ComponentType.ACTIVITY;
    }

    boolean isService() {
        return type == ComponentType.SERVICE;
    }

    boolean isBroadcastReceiver() {
        return type == ComponentType.BROADCAST_RECEIVER;
    }

    boolean isContentProvider() {
        return  type == ComponentType.CONTENT_PROVIDER;
    }

    boolean hasIntentFilter() { return !intentFilters.isEmpty(); }

    ComponentType getType() {
        return type;
    }

    void addIntentFilter(IntentFilterDescription intentFilter) {
        intentFilters.add(intentFilter);
    }

    /**
     * Returns the FQN of the component. That is package name + class name.
     *
     * @return Returns the FQN of the component.
     */
    String getFullyQualifiedName() {

        if (name.startsWith(".")) {
            return MATE.packageName + name;
        } else {
            return name;
        }
    }

    Set<IntentFilterDescription> getIntentFilters() {
        return intentFilters;
    }

    @Override
    public String toString() {
       StringBuilder builder = new StringBuilder();
       builder.append("Component: " + name + System.lineSeparator());
       builder.append("Type: " + type + System.lineSeparator());

       builder.append("Intent Filters: " + System.lineSeparator());
       builder.append("-------------------------------------------");
       for (IntentFilterDescription intentFilter : intentFilters) {
           builder.append(intentFilter + System.lineSeparator());
       }
       builder.append("-------------------------------------------");
       return builder.toString();
    }
}
