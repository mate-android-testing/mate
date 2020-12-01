package org.mate.interaction.intent;

import org.mate.ui.Action;

/**
 * Describes a system event notification that should be broad-casted
 * to a certain receiver component.
 */
public class SystemAction extends Action {

    private final ComponentDescription component;
    private final IntentFilterDescription intentFilter;

    private final String receiver;
    private boolean dynamic = false;
    private final String action;

    // TODO: certain system events may also require a category, which should be included in the intent
    // likewise data tags (URIs) could be relevant

    /**
     * Initialises a system event.
     *
     * @param component The component definition representing the broadcast receiver.
     * @param intentFilter The selected intent filter.
     * @param action The name of the system event the receiver is listening for.
     */
    public SystemAction(ComponentDescription component,
                        IntentFilterDescription intentFilter, String action) {
        this.component = component;
        this.intentFilter = intentFilter;
        this.receiver = component.getFullyQualifiedName();
        this.action = action;
    }

    /**
     * Marks the receiver as a dynamic one.
     */
    public void markAsDynamic() {
        dynamic = true;
    }

    /**
     * Returns whether the encapsulated receiver is dynamic or not.
     *
     * @return Returns {@code true} if the receiver is a dynamic receiver,
     *          otherwise {@code false} is returned.
     */
    public boolean isDynamicReceiver() {
        return dynamic;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        // return "system event receiver: " + receiver + ", system event: " + action;
        return "system action";
    }
}
