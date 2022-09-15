package org.mate.utils.testcase.espresso.actions;

import android.content.Intent;

import org.mate.interaction.action.intent.IntentBasedAction;

/**
 * Converts a {@link IntentBasedAction} to an espresso action.
 */
public class IntentBasedActionConverter extends ActionConverter {

    /**
     * The component type, e.g. activity.
     */
    private final String componentType;

    /**
     * The intent that should be sent.
     */
    private final Intent intent;

    /**
     * Whether the intent should be sent to a dynamic broadcast receiver or not.
     */
    private final boolean isDynamicReceiver;

    /**
     * Constructs a converter for the given action.
     *
     * @param action The action that should be converted.
     */
    public IntentBasedActionConverter(IntentBasedAction action) {
        super(action);
        componentType = action.getComponentType().toString();
        intent = action.getIntent();
        isDynamicReceiver = action.getComponent().isDynamicReceiver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String convert() {
        buildPerform();
        buildComment();
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildPerform() {

        builder.append("sendIntent(");
        builder.append("\"").append(componentType).append("\"").append(", ");

        // action
        if (intent.getAction() == null) {
            builder.append("null").append(", ");
        } else {
            builder.append("\"").append(intent.getAction()).append("\"").append(", ");
        }

        // categories
        if (intent.getCategories() == null) {
            builder.append("null").append(", ");
        } else {
            builder.append("\"").append(intent.getCategories()).append("\"").append(", ");
        }

        // data uri
        if (intent.getData() == null) {
            builder.append("null").append(", ");
        } else {
            builder.append("\"").append(intent.getData()).append("\"").append(", ");
        }

        // extras
        if (intent.getExtras() == null) {
            builder.append("null").append(", ");
        } else {
            // TODO: We may need to encode the extras type, see ComponentDescription.getExtras().
            builder.append("\"").append(intent.getExtras()).append("\"").append(", ");
        }

        // component / package
        if (isDynamicReceiver) {
            builder.append("\"").append(intent.getPackage()).append("\"").append(", ");
        } else {
            builder.append("\"").append(intent.getComponent().flattenToString())
                    .append("\"").append(", ");
        }

        // flags
        builder.append(intent.getFlags());

        builder.append(");");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildComment() {
        builder.append(" // INTENT-BASED ACTION");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildViewMatcher() {
        throw new UnsupportedOperationException("No view matcher for intent based actions!");
    }
}
