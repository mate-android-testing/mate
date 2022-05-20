package org.mate.commons.utils.manifest.element;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an intent filter hosted within a {@link ComponentDescription}.
 */
public class IntentFilterDescription implements Parcelable {

    /**
     * The set of actions.
     */
    private Set<String> actions = new HashSet<>();

    /**
     * The set of categories.
     */
    private Set<String> categories = new HashSet<>();

    /**
     * The data specification, mostly optional.
     */
    private DataDescription data;

    public IntentFilterDescription() {}

    /**
     * Adds the given action to the intent filter.
     *
     * @param action The action to be added.
     */
    public void addAction(String action) {
        actions.add(action);
    }

    /**
     * Adds an category to the intent filter.
     *
     * @param category The category to be added.
     */
    public void addCategory(String category) {
        categories.add(category);
    }

    /**
     * Adds an data specification to the intent filter. See
     * https://developer.android.com/guide/topics/manifest/data-element for more information.
     *
     * @param scheme The scheme part of a URI. This is the minimal essential attribute for specifying
     *          a URI; at least one scheme attribute must be set for the filter, or none of the other
     *          URI attributes are meaningful.
     * @param host The host part of a URI authority.
     * @param port The port part of a URI authority. This attribute is meaningful only if the scheme
     *              and host attributes are also specified for the filter.
     * @param path The path part of a URI which must begin with a /.
     * @param pathPattern The path part of a URI which must begin with a /.
     * @param pathPrefix The path part of a URI which must begin with a /.
     * @param mimeType A MIME media type, such as image/jpeg or audio/mpeg4-generic.
     */
    public void addData(String scheme, String host, String port, String path, String pathPattern,
                        String pathPrefix, String mimeType) {

        if (data == null) {
            // lazy initialisation
            data = new DataDescription();
        }

        data.addScheme(scheme);
        data.addHost(host);
        data.addPort(port);
        data.addPath(path);
        data.addPathPattern(pathPattern);
        data.addPathPrefix(pathPrefix);
        data.addMimeType(mimeType);
    }

    /**
     * Whether the intent filter declares any action.
     *
     * @return Returns {@code true} if the intent filter declares any action, otherwise {@code false}
     *          is returned.
     */
    public boolean hasAction() {
        return !actions.isEmpty();
    }

    /**
     * Whether the intent filter declares any category.
     *
     * @return Returns {@code true} if the intent filter declares any category, otherwise {@code false}
     *          is returned.
     */
    public boolean hasCategory() {
        return !categories.isEmpty();
    }

    /**
     * Whether the intent filter declares a data specification.
     *
     * @return Returns {@code true} if the intent filter declares a data specification, otherwise
     *          {@code false} is returned.
     */
    public boolean hasData() {
        // this only makes sense with lazy initialization
        return data != null;
    }

    /**
     * Returns the set of actions declared by the intent filter.
     *
     * @return Returns the set of actions.
     */
    public Set<String> getActions() {
        return Collections.unmodifiableSet(actions);
    }

    /**
     * Returns the set of categories declared by the intent filter.
     *
     * @return Returns the set of categories.
     */
    public Set<String> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    /**
     * Returns the data specification. Might be {@code null}.
     *
     * @return Returns the data specification.
     */
    public DataDescription getData() {
        return data;
    }

    /**
     * Checks for equality between two intent filters. Two intent filters are considered equal, iff
     * they share the same set of actions and categories as well as the same data specification.
     *
     * @param o The other intent filter.
     * @return Returns {@code true} if the two intent filters are equal, otherwise {@code false} is
     *          returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            IntentFilterDescription other = (IntentFilterDescription) o;
            return Objects.equals(actions, other.actions) &&
                    Objects.equals(categories, other.categories) &&
                    Objects.equals(data, other.data);
        }
    }

    /**
     * Computes a hashcode for the intent filter.
     *
     * @return Returns the computed hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(actions, categories, data);
    }

    /**
     * Provides a simple textual representation of the intent filter.
     *
     * @return Returns the string representation of the intent filter.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Actions: " + actions + System.lineSeparator());
        builder.append("Categories: " + categories + System.lineSeparator());
        builder.append("Data: " + System.lineSeparator());
        builder.append(data + System.lineSeparator());
        return builder.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(new ArrayList<>(this.actions));
        dest.writeStringList(new ArrayList<>(this.categories));
        dest.writeParcelable(this.data, flags);
    }

    protected IntentFilterDescription(Parcel in) {
        this.actions = new HashSet<>(in.createStringArrayList());
        this.categories = new HashSet<>(in.createStringArrayList());
        this.data = in.readParcelable(DataDescription.class.getClassLoader());
    }

    public static final Creator<IntentFilterDescription> CREATOR = new Creator<IntentFilterDescription>() {
        @Override
        public IntentFilterDescription createFromParcel(Parcel source) {
            return new IntentFilterDescription(source);
        }

        @Override
        public IntentFilterDescription[] newArray(int size) {
            return new IntentFilterDescription[size];
        }
    };
}
