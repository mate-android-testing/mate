package org.mate.interaction.intent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;
import android.util.SizeF;

import org.mate.MATE;
import org.mate.utils.DataPool;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ComponentDescription {

    private final String name;
    private final ComponentType type;

    // whether the component handles onNewIntent (solely activities do so)
    private boolean handleOnNewIntent = false;

    // a component may define optionally intent filter tags
    private Set<IntentFilterDescription> intentFilters = new HashSet<>();

    // additional information used in combination with intents
    private Set<String> stringConstants = new HashSet<>();
    private Map<String, String> extras = new HashMap<>();

    public ComponentDescription(String name, String type) {
        this.name = name;
        this.type = ComponentType.mapStringToComponent(type);
    }

    public ComponentDescription(String name, ComponentType type) {
        this.name = name;
        this.type = type;
    }

    public boolean isHandlingOnNewIntent() {
        return handleOnNewIntent;
    }

    public void setHandlingOnNewIntent(boolean handleOnNewIntent) {
        this.handleOnNewIntent = handleOnNewIntent;
    }

    void addStringConstants(Set<String> stringConstants) {
        this.stringConstants.addAll(stringConstants);
    }

    public void addIntentFilters(Set<IntentFilterDescription> intentFilters) {
        this.intentFilters.addAll(intentFilters);
    }

    public void addExtras(Map<String, String> extras) {
        this.extras.putAll(extras);
    }

    public boolean isActivity() {
        return type == ComponentType.ACTIVITY;
    }

    public boolean isService() {
        return type == ComponentType.SERVICE;
    }

    public boolean isBroadcastReceiver() {
        return type == ComponentType.BROADCAST_RECEIVER;
    }

    public boolean isContentProvider() {
        return type == ComponentType.CONTENT_PROVIDER;
    }

    public boolean hasIntentFilter() {
        return !intentFilters.isEmpty();
    }

    public boolean hasExtra() {
        return !extras.isEmpty();
    }

    public void removeIntentFilters(Collection<IntentFilterDescription> intentFilters) {
        this.intentFilters.removeAll(intentFilters);
    }

    ComponentType getType() {
        return type;
    }

    public void addIntentFilter(IntentFilterDescription intentFilter) {
        intentFilters.add(intentFilter);
    }

    /**
     * Returns the FQN of the component. That is package name + class name.
     *
     * @return Returns the FQN of the component.
     */
    public String getFullyQualifiedName() {

        if (name.startsWith(".")) {
            return MATE.packageName + name;
        } else {
            return name;
        }
    }

    /**
     * Retrieves a component from a list of components by name.
     *
     * @param components The list of components.
     * @param name The name of the component to be looked up.
     * @return Returns the component matching the given name in the list of components
     *          or {@code null} if the component couldn't be found.
     */
    public static ComponentDescription getComponentByName(final List<ComponentDescription> components, final String name) {

        for (ComponentDescription component : components) {
            if (component.getFullyQualifiedName().equals(name)) {
                return component;
            }
        }
        return null;
    }

    /**
     * Returns the set of attached intent filters.
     *
     * @return Returns the attached intent filters.
     */
    public Set<IntentFilterDescription> getIntentFilters() {
        return Collections.unmodifiableSet(intentFilters);
    }

    /**
     * Returns a map describing the key-value entries of a possible attached bundle.
     *
     * @return Returns the extra (the bundle object).
     */
    public Map<String, String> getExtras() {
        return Collections.unmodifiableMap(extras);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Component: " + name + System.lineSeparator());
        builder.append("Type: " + type + System.lineSeparator());

        builder.append("Intent Filters: " + System.lineSeparator());
        builder.append("-------------------------------------------" + System.lineSeparator());
        for (IntentFilterDescription intentFilter : intentFilters) {
            builder.append(intentFilter + System.lineSeparator());
        }
        builder.append("-------------------------------------------" + System.lineSeparator());

        builder.append("Strings: " + stringConstants + System.lineSeparator());
        builder.append("Extras: " + extras + System.lineSeparator());

        if (isActivity()) {
            builder.append("Handles OnNewIntent: " + handleOnNewIntent + System.lineSeparator());
        }

        return builder.toString();
    }

    Bundle generateRandomBundle() {

        Bundle bundle = new Bundle();

        // how many elements for a list/array + upper bound value
        final int COUNT = 5;
        final int BOUND = 100;

        for (Map.Entry<String, String> extra : extras.entrySet()) {

            // depending on the type we need to select a value out of a pre-defined pool
            switch (extra.getValue()) {
                case "Int":
                    bundle.putInt(extra.getKey(),
                            Randomness.randomIndex(DataPool.INTEGER_LIST));
                    break;
                case "Int[]":
                    bundle.putIntArray(extra.getKey(), Randomness.getRandomIntArray(COUNT, BOUND));
                    break;
                case "Integer<>": // note Integer vs Int
                    bundle.putIntegerArrayList(extra.getKey(),
                            new ArrayList<>(Randomness.getRandomIntegersWithNull(COUNT, BOUND)));
                    break;
                case "String":
                case "CharSequence": // interface typ of string class
                    if (!stringConstants.isEmpty()) {
                        // choose randomly constant from extracted strings
                        bundle.putCharSequence(extra.getKey(),
                                Randomness.randomElementOrNull(stringConstants));
                    } else {
                        // generate random string
                        bundle.putCharSequence(extra.getKey(),
                                Randomness.randomElement(DataPool.STRING_LIST_WITH_NULL));
                    }
                    break;
                case "String[]":
                case "CharSequence[]":
                    if (!(stringConstants.size() < COUNT)) {
                        // choose randomly constants from extracted strings
                        bundle.putCharSequenceArray(extra.getKey(),
                                Randomness.randomElements(stringConstants, COUNT)
                                        .toArray(new CharSequence[0]));
                    } else {
                        // TODO: generate random strings
                        bundle.putCharSequenceArray(extra.getKey(), DataPool.STRING_ARRAY_WITH_NULL);
                    }
                    break;
                case "String<>":
                case "CharSequence<>":
                    if (!(stringConstants.size() < COUNT)) {
                        // choose randomly constants from extracted strings
                        bundle.putCharSequenceArrayList(extra.getKey(),
                                new ArrayList<CharSequence>(Randomness
                                        .randomElements(stringConstants, COUNT)));
                    } else {
                        // TODO: generate random strings
                        bundle.putCharSequenceArrayList(extra.getKey(),
                                new ArrayList<CharSequence>(DataPool.STRING_LIST_WITH_NULL));
                    }
                    break;
                case "Float":
                    bundle.putFloat(extra.getKey(),
                            Randomness.randomElement(DataPool.FLOAT_LIST));
                    break;
                case "Float[]":
                    bundle.putFloatArray(extra.getKey(), Randomness.getRandomFloatArray((COUNT)));
                    break;
                case "Double":
                    bundle.putDouble(extra.getKey(),
                            Randomness.randomElement(DataPool.DOUBLE_LIST));
                    break;
                case "Double[]":
                    bundle.putDoubleArray(extra.getKey(), Randomness.getRandomDoubleArray(COUNT));
                    break;
                case "Long":
                    bundle.putLong(extra.getKey(), Randomness.randomElement(DataPool.LONG_LIST));
                    break;
                case "Long[]":
                    bundle.putLongArray(extra.getKey(), Randomness.getRandomLongArray(COUNT));
                    break;
                case "Short":
                    bundle.putShort(extra.getKey(), Randomness.randomElement(DataPool.SHORT_LIST));
                    break;
                case "Short[]":
                    bundle.putShortArray(extra.getKey(), Randomness.getRandomShortArray(COUNT));
                    break;
                case "Byte":
                    bundle.putByte(extra.getKey(), Randomness.randomElement(DataPool.BYTE_LIST));
                    break;
                case "Byte[]":
                    bundle.putByteArray(extra.getKey(), Randomness.getRandomByteArray(COUNT));
                    break;
                case "Boolean":
                    bundle.putBoolean(extra.getKey(), Randomness.randomElement(DataPool.BOOLEAN_LIST));
                    break;
                case "Boolean[]":
                    bundle.putBooleanArray(extra.getKey(), Randomness.getRandomBooleanArray(COUNT));
                    break;
                case "Char":
                    bundle.putChar(extra.getKey(), Randomness.randomElement(DataPool.CHAR_LIST));
                    break;
                case "Char[]":
                    bundle.putCharArray(extra.getKey(), Randomness.getRandomCharArray(COUNT));
                    break;
                case "Serializable": // strings are serializable
                    if (!stringConstants.isEmpty()) {
                        // choose randomly constant from extracted strings
                        bundle.putSerializable(extra.getKey(),
                                Randomness.randomElementOrNull(stringConstants));
                    } else {
                        // generate random string
                        bundle.putSerializable(extra.getKey(),
                                Randomness.randomElement(DataPool.STRING_LIST_WITH_NULL));
                    }
                    break;
                case "Parcelable": // bundle is parcelable
                    bundle.putParcelable(extra.getKey(), new Bundle());
                    break;
                case "Parcelable[]":
                    bundle.putParcelableArray(extra.getKey(), new Parcelable[]{new Bundle()});
                    break;
                case "Parcelable<>":
                    List<Parcelable> parcelables = new ArrayList<>();
                    parcelables.add(new Bundle());
                    bundle.putParcelableArrayList(extra.getKey(), new ArrayList<>(parcelables));
                    break;
                case "Size":
                    List<Integer> values = Randomness.getRandomIntegers(2, BOUND);
                    Size size = new Size(values.get(0), values.get(1));
                    bundle.putSize(extra.getKey(), size);
                    break;
                case "SizeF":
                    float[] valuesF = Randomness.getRandomFloatArray(2);
                    SizeF sizeF = new SizeF(valuesF[0], valuesF[1]);
                    bundle.putSizeF(extra.getKey(), sizeF);
                    break;
                default:
                    MATE.log("Data type not yet supported: " + extra.getValue());
                    // omit bundle entry
                    break;
            }
        }
        return bundle;
    }

}
