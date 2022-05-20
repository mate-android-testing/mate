package org.mate.commons.utils.manifest.element;

/**
 * Defines the various component types.
 */
public enum ComponentType {

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
    public static ComponentType mapStringToComponent(String component) {

        // treat activity-alias as an activity
        if (component.equals("activity-alias")) {
            return ACTIVITY;
        }

        for (ComponentType type : ComponentType.values()) {
            if (type.toString().equals(component)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Component " + component + " cannot be mapped to a type!");
    }

    /**
     * Returns a textual representation of the component type.
     *
     * @return Returns the string representation of the component type.
     */
    @Override
    public String toString() {
        switch (this) {
            case ACTIVITY: return "activity";
            case SERVICE: return "service";
            case BROADCAST_RECEIVER: return "receiver";
            case CONTENT_PROVIDER: return "provider";
            default: throw new UnsupportedOperationException("Component type " + this + " not yet supported!");
        }
    }
}
