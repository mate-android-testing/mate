package org.mate.interaction.intent;

import android.os.Bundle;

import org.mate.MATE;
import org.mate.utils.DataPool;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ComponentDescription {

    private final String name;
    private final ComponentType type;

    // a component may define optionally intent filter tags
    private Set<IntentFilterDescription> intentFilters = new HashSet<>();

    // additional information used in combination with intents
    private Set<String> stringConstants = new HashSet<>();
    private Map<String, String> extras = new HashMap<>();

    ComponentDescription(String name, String type) {
        this.name = name;
        this.type = ComponentType.mapStringToComponent(type);
    }

    void addStringConstants(Set<String> stringConstants) {
        this.stringConstants.addAll(stringConstants);
    }

    void addExtras(Map<String,String> extras) {
        this.extras.putAll(extras);
    }

    boolean isActivity() {
        return type == ComponentType.ACTIVITY;
    }

    boolean isService() {
        return type == ComponentType.SERVICE;
    }

    boolean isBroadcastReceiver() {
        return type == ComponentType.BROADCAST_RECEIVER;
    }

    boolean isContentProvider() {
        return  type == ComponentType.CONTENT_PROVIDER;
    }

    boolean hasIntentFilter() { return !intentFilters.isEmpty(); }

    boolean hasExtra() { return !extras.isEmpty(); }

    ComponentType getType() {
        return type;
    }

    void addIntentFilter(IntentFilterDescription intentFilter) {
        intentFilters.add(intentFilter);
    }

    /**
     * Returns the FQN of the component. That is package name + class name.
     *
     * @return Returns the FQN of the component.
     */
    String getFullyQualifiedName() {

        if (name.startsWith(".")) {
            return MATE.packageName + name;
        } else {
            return name;
        }
    }

    Set<IntentFilterDescription> getIntentFilters() {
        return Collections.unmodifiableSet(intentFilters);
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
       return builder.toString();
    }

    Bundle generateRandomBundle() {

        Bundle bundle = new Bundle();

        // how many elements for a list/array + upper bound value
        final int COUNT = 5;
        final int BOUND = 100;

        //
        for (Map.Entry<String, String> extra : extras.entrySet()) {

            // depending on the type we need to select a value out of a pre-defined pool
            switch (extra.getValue()) {
                case "Integer":
                    bundle.putInt(extra.getKey(),
                            Randomness.randomIndex(DataPool.INTEGER_LIST_WITH_NULL));
                    break;
                case "Integer[]":
                    bundle.putIntArray(extra.getKey(), Randomness.getRandomIntArray(COUNT, BOUND));
                    break;
                case "Integer<>":
                    bundle.putIntegerArrayList(extra.getKey(),
                            new ArrayList<>(Randomness.getRandomIntegers(COUNT, BOUND)));
                    break;
                case "String":
                case "CharSequence": // interface typ of string class
                    if (!stringConstants.isEmpty()) {
                        // choose randomly constant from extracted strings
                        bundle.putCharSequence(extra.getKey(),
                                Randomness.randomElement(stringConstants));
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
                        bundle.putCharSequenceArray(extra.getKey(), DataPool.STRING_ARRAY);
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
                                new ArrayList<CharSequence>(DataPool.STRING_LIST));
                    }
                    break;
                case "Float":
                    bundle.putFloat(extra.getKey(),
                            Randomness.randomElement(DataPool.FLOAT_LIST_WITH_NULL));
                    break;
                case "Float[]":
                    bundle.putFloatArray(extra.getKey(), Randomness.getRandomFloatArray((COUNT)));
                    break;
                case "Double":
                case "Long":
                case "Short":
                case "Byte":
                case "Boolean":
                case "Char":
                case "Serializable":
                case "Parceable":

            }

        }
        return bundle;
    }

}
