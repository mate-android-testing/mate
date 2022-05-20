package org.mate.exploration.intent;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;
import android.util.SizeF;

import org.mate.commons.utils.DataPool;
import org.mate.commons.utils.MATELog;
import org.mate.commons.utils.Randomness;
import org.mate.commons.utils.manifest.element.ComponentDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates a {@link Bundle} for a given component.
 */
public final class BundleGenerator {

    private BundleGenerator() {
        throw new UnsupportedOperationException("Utility class can't be instantiated!");
    }

    /**
     * Generates a bundle with semi-random entries for the given component.
     *
     * @param component The component for which a bundle should be generated.
     * @return Returns the randomly generated bundle.
     */
    public static Bundle generateRandomBundle(final ComponentDescription component) {

        Bundle bundle = new Bundle();

        // how many elements for a list/array + upper bound value
        final int COUNT = 5;
        final int BOUND = 100;

        for (Map.Entry<String, String> extra : component.getExtras().entrySet()) {

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
                    if (component.hasStringConstants()) {
                        // choose randomly constant from extracted strings
                        bundle.putCharSequence(extra.getKey(),
                                Randomness.randomElementOrNull(component.getStringConstants()));
                    } else {
                        // generate random string
                        bundle.putCharSequence(extra.getKey(),
                                Randomness.randomElement(DataPool.STRING_LIST_WITH_NULL));
                    }
                    break;
                case "String[]":
                case "CharSequence[]":
                    if (!(component.getStringConstants().size() < COUNT)) {
                        // choose randomly constants from extracted strings
                        bundle.putCharSequenceArray(extra.getKey(),
                                Randomness.randomElements(component.getStringConstants(), COUNT)
                                        .toArray(new CharSequence[0]));
                    } else {
                        // TODO: generate random strings
                        bundle.putCharSequenceArray(extra.getKey(), DataPool.STRING_ARRAY_WITH_NULL);
                    }
                    break;
                case "String<>":
                case "CharSequence<>":
                    if (!(component.getStringConstants().size() < COUNT)) {
                        // choose randomly constants from extracted strings
                        bundle.putCharSequenceArrayList(extra.getKey(),
                                new ArrayList<>(Randomness
                                        .randomElements(component.getStringConstants(), COUNT)));
                    } else {
                        // TODO: generate random strings
                        bundle.putCharSequenceArrayList(extra.getKey(),
                                new ArrayList<>(DataPool.STRING_LIST_WITH_NULL));
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
                    if (component.hasStringConstants()) {
                        // choose randomly constant from extracted strings
                        bundle.putSerializable(extra.getKey(),
                                Randomness.randomElementOrNull(component.getStringConstants()));
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
                case "Bundle":
                    bundle.putBundle(extra.getKey(), new Bundle());
                    break;
                default:
                    MATELog.log_warn("Data type not yet supported: " + extra.getValue());
                    // omit bundle entry
                    break;
            }
        }
        return bundle;
    }
}
