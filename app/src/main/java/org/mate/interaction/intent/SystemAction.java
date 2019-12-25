package org.mate.interaction.intent;

import org.mate.ui.Action;

/**
 * Describes a system event notification that should be broad-casted
 * to a certain receiver component.
 */
public class SystemAction extends Action {

    private final String receiver;
    private final String action;

    // TODO: certain system events may also require a category, which should be included in the intent
    // likewise data tags (URIs) could be relevant

    /**
     * Initialises a system event.
     *
     * @param receiver The name of the broadcast receiver listening for a system event.
     * @param action The name of the system event the receiver is listening for.
     */
    public SystemAction(String receiver, String action) {
        this.receiver = receiver;
        this.action = action;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "system event receiver: " + receiver + ", system event: " + action;
    }
}
