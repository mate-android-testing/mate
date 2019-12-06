package org.mate.interaction.intent;

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

    void addIntentFilter(IntentFilterDescription intentFilter) {
        intentFilters.add(intentFilter);
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

    private enum ComponentType {

        ACTIVITY,
        SERVICE,
        BROADCAST_RECEIVER,
        CONTENT_PROVIDER;

        /**
         * Maps the string representation to a component type if present.
         *
         * @param component The name of the component.
         * @return Returns the component type matching the given component.
         */
        static ComponentType mapStringToComponent(String component) {
            for (ComponentType type : ComponentType.values()) {
                if (type.toString().equals(component)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Component " + component + " cannot be mapped to a type!");
        }

        @Override
        public String toString() {
            // can also use super.toString() -> returns a string representation of the constants
            switch (this) {
                case ACTIVITY: return "activity";
                case SERVICE: return "service";
                case BROADCAST_RECEIVER: return "receiver";
                case CONTENT_PROVIDER: return "provider";
                default: throw new UnsupportedOperationException("Component type "
                        + this + " not yet supported!");
            }
        }
    }
}
