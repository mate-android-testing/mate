package org.mate.utils.testcase.espresso.actions;

import org.mate.Registry;
import org.mate.interaction.action.intent.SystemAction;

import java.util.regex.Matcher;

/**
 * Converts a {@link SystemAction} to an espresso action.
 */
public class SystemActionConverter extends ActionConverter {

    /**
     * The name of the broadcast receiver.
     */
    private final String receiver;

    /**
     * Whether the broadcast receiver is a dynamic receiver or not.
     */
    private final boolean isDynamicReceiver;

    /**
     * The action attribute of the intent / system event.
     */
    private final String action;

    /**
     * Constructs a converter for the given action.
     *
     * @param action The action that should be converted.
     */
    public SystemActionConverter(SystemAction action) {
        super(action);
        this.action = action.getAction();
        // the inner class separator '$' needs to be escaped
        this.receiver = action.getReceiver().replaceAll("\\$", Matcher.quoteReplacement("\\$"));
        this.isDynamicReceiver = action.isDynamicReceiver();
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
        builder.append("device.executeShellCommand(");
        builder.append("\"");
        builder.append("su root am broadcast -a ").append(action).append(" ");
        final String tag;
        final String component;
        if (isDynamicReceiver) {
            /*
             * In the case we deal with a dynamic receiver, we can't specify the full component name,
             * since dynamic receivers can't be triggered by explicit intents! Instead, we can only
             * specify the package name in order to limit the receivers of the broadcast.
             */
            tag = "-p";
            component = Registry.getPackageName();
        } else {
            tag = "-n";
            component = Registry.getPackageName() + "/" + receiver;
        }
        builder.append(tag).append(" ").append(component);
        builder.append("\"");
        builder.append(");");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildComment() {
        builder.append(" // SYSTEM ACTION");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buildViewMatcher() {
        throw new UnsupportedOperationException("No view matcher for system actions!");
    }
}
