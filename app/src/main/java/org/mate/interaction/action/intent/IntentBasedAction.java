package org.mate.interaction.action.intent;

import android.content.Intent;
import android.support.annotation.NonNull;

import org.mate.utils.manifest.element.ComponentDescription;
import org.mate.utils.manifest.element.ComponentType;
import org.mate.utils.manifest.element.IntentFilterDescription;

import java.util.Objects;
import java.util.Set;

/**
 * A non-privileged intent action that can be received by any component matching the intent filter.
 */
public class IntentBasedAction extends IntentAction {

    /**
     * The intent that is sent.
     */
    private final Intent intent;

    public IntentBasedAction(Intent intent, ComponentDescription component,
                             IntentFilterDescription intentFilter) {
        super(component, intentFilter);
        this.intent = intent;
    }

    /**
     * Copy constructor.
     *
     * @param intentBasedAction The intent action to be copied.
     */
    public IntentBasedAction(IntentBasedAction intentBasedAction) {
        super(intentBasedAction.getComponent(), intentBasedAction.intentFilter);
        this.intent = (Intent) intentBasedAction.intent.clone();
    }

    public ComponentType getComponentType() {
        return component.getType();
    }

    public Intent getIntent() {
        return intent;
    }

    /**
     * A custom string representation for an Intent-based action. Do not
     * alter this representation without changing the parsing routine
     * of the analysis framework!
     *
     * @return Returns a string representation for an Intent-based action.
     */
    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("intent-based action: ");

        if (intent.getAction() != null) {
            builder.append("act=" + intent.getAction() + " ");
        }

        if (intent.getCategories() != null && !intent.getCategories().isEmpty()) {
            builder.append("cat=" + intent.getCategories() + " ");
        }

        if (intent.getDataString() != null) {
            builder.append("uri=" + intent.getDataString() + " ");
        }

        if (intent.getComponent() != null) {
            builder.append("cmp=" + intent.getComponent().toShortString() + " ");
        } else if (intent.getPackage() != null) {
            builder.append("pkt=" + intent.getPackage() + " ");
        }

        if (intent.getType() != null) {
            builder.append("typ=" + intent.getType() + " ");
        }

        if (intent.getExtras() != null) {
            builder.append("ext=[");
            String prefix = "";
            Set<String> keys = intent.getExtras().keySet();
            for (String key : keys) {
                builder.append(prefix);
                prefix = " || ";
                builder.append(key + "=" + intent.getExtras().get(key));
            }
            builder.append("]");
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            IntentBasedAction that = (IntentBasedAction) o;

            return Objects.equals(component, that.component)
                    && Objects.equals(intentFilter, that.intentFilter)
                    /*
                     * We can't call here equals() on the Intent object directly, since it only
                     * compares the references and nothing more.
                     */
                    && Objects.equals(intent.getAction(), that.intent.getAction())
                    && Objects.equals(intent.getCategories(), that.intent.getCategories())
                    && Objects.equals(intent.getData(), that.intent.getData())
                    && Objects.equals(intent.getComponent(), that.intent.getComponent())
                    // equals() on Bundle compares only the reference
                    && Objects.equals(Objects.toString(intent.getExtras(), null),
                    Objects.toString(that.intent.getExtras(), null));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(component, intentFilter, intent.getAction(), intent.getCategories(),
                intent.getData(), intent.getComponent(), intent.getExtras());
    }

    @NonNull
    @Override
    public String toShortString() {
        if (intent.getAction() != null) {
            return intent.getAction();
        } else {
            return "INTENT_WITHOUT_ACTION";
        }
    }
}
