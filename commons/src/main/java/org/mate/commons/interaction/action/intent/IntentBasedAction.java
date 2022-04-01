package org.mate.commons.interaction.action.intent;

import android.content.Intent;
import android.os.Parcel;
import android.support.annotation.NonNull;

import org.mate.commons.interaction.action.Action;
import org.mate.commons.utils.manifest.element.ComponentDescription;
import org.mate.commons.utils.manifest.element.ComponentType;
import org.mate.commons.utils.manifest.element.IntentFilterDescription;

import java.util.Objects;
import java.util.Set;

public class IntentBasedAction extends Action {

    private final Intent intent;
    private final ComponentDescription component;
    private final IntentFilterDescription intentFilter;

    public IntentBasedAction(Intent intent, ComponentDescription component,
                             IntentFilterDescription intentFilter) {
        this.intent = intent;
        this.component = component;
        this.intentFilter = intentFilter;
    }

    public ComponentType getComponentType() {
        return component.getType();
    }

    public Intent getIntent() {
        return intent;
    }

    public ComponentDescription getComponent() {
        return component;
    }

    public IntentFilterDescription getIntentFilter() {
        return intentFilter;
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
            return Objects.equals(intent, that.intent);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(intent);
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

    @Override
    public int getIntForActionSubClass() {
        return ACTION_SUBCLASS_INTENT_BASED;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.intent, flags);
        dest.writeParcelable(this.component, flags);
        dest.writeParcelable(this.intentFilter, flags);
    }

    public IntentBasedAction(Parcel in) {
        super(in);
        this.intent = in.readParcelable(Intent.class.getClassLoader());
        this.component = in.readParcelable(ComponentDescription.class.getClassLoader());
        this.intentFilter = in.readParcelable(IntentFilterDescription.class.getClassLoader());
    }

    public static final Creator<IntentBasedAction> CREATOR = new Creator<IntentBasedAction>() {
        @Override
        public IntentBasedAction createFromParcel(Parcel source) {
            // We need to use the Action.CREATOR here, because we want to make sure to remove the
            // ActionSubClass integer from the beginning of Parcel and call the appropriate
            // constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (IntentBasedAction) Action.CREATOR.createFromParcel(source);
        }

        @Override
        public IntentBasedAction[] newArray(int size) {
            return new IntentBasedAction[size];
        }
    };
}
