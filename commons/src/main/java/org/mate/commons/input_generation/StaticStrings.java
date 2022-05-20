package org.mate.commons.input_generation;


import org.mate.commons.utils.Randomness;
import org.mate.commons.input_generation.format_types.InputFieldType;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides access to the static strings extracted from the bytecode.
 */
public final class StaticStrings {

    /**
     * The singleton instance.
     */
    private static StaticStrings staticStrings;

    /**
     * Contains the static strings per class.
     */
    private final Map<String, Set<String>> allStrings = new HashMap<>();

    /**
     * This map stores for each {@link InputFieldType} a map with the string variables per class
     * name.
     */
    private final Map<InputFieldType, Map<String, Set<String>>> inputFieldTypeMap = new HashMap<>();

    /**
     * Whether the static strings could be loaded, i.e. {@link StaticStringsParser#parseStaticStrings()}
     * succeeded.
     */
    private boolean initialised = false;

    private StaticStrings() {
        createInitialMap();
    }

    /**
     * Retrieves the singleton instance.
     *
     * @return Returns the static string instance.
     */
    public static StaticStrings getInstance() {
        if (staticStrings == null) {
            staticStrings = new StaticStrings();
        }
        return staticStrings;
    }

    /**
     * Adds static string value to the set with the corresponding class name.
     *
     * @param className The class name, where the values should be inorder.
     * @param values The new values.
     */
    public void add(String className, Set<String> values) {

        Set<String> copy = new HashSet<>(values);
        extendMapByClassName(allStrings, className, copy);

        for (String value : values) {
            Set<InputFieldType> inputFields = InputFieldType.getInputFieldsMatchingRegex(value);
            for (InputFieldType input : inputFields) {
                Map<String, Set<String>> cache = inputFieldTypeMap.get(input);
                if (cache == null) {
                    cache = new HashMap<>();
                }
                extendMapByClassName(cache, className, new HashSet<>(Collections.singleton(value)));
                inputFieldTypeMap.put(input, cache);
            }
        }
    }

    /**
     * Gets a random string independent of the field type. Only of a list of possible classes.
     *
     * @param classNames The class name you want to have a string of it.
     * @return A random string of the set.
     */
    public String getRandomStringFor(List<String> classNames) {
        String className = Randomness.randomElement(classNames);
        return getRandomStringFor(allStrings, className);
    }

    /**
     * Gets a random string dependent of the field type for a random class in a list of possible
     * class names.
     *
     * @param inputType The fieldType you need a string.
     * @param classNames A list of class names with activities or fragments.
     * @return A random string of the set for field type.
     */
    public String getRandomStringFor(InputFieldType inputType, List<String> classNames) {
        String className = Randomness.randomElement(classNames);
        return getRandomStringFor(inputFieldTypeMap.get(inputType), className);
    }

    /**
     * Gets a random string for explicit inputType without any class name.
     *
     * @param inputType The class name.
     * @return A random string for a certain input type.
     */
    public String getRandomStringFor(InputFieldType inputType) {
        Map<String, Set<String>> map = inputFieldTypeMap.get(inputType);
        if (map == null) {
            return null;
        }
        Set<String> concatenated = new HashSet<>();
        for (Map.Entry<String, Set<String>> entrySet : map.entrySet()) {
            concatenated.addAll(entrySet.getValue());
        }

        if (!concatenated.isEmpty()) {
            return Randomness.randomElement(concatenated);
        } else {
            return null;
        }
    }

    private void createInitialMap() {
        for (InputFieldType inputField : InputFieldType.values()) {
            if (inputField != InputFieldType.NOTHING) {
                inputFieldTypeMap.put(inputField, null);
            }
        }
    }

    /**
     * Extends the given map for a given class name and the corresponding set with string values. If
     * the class name as key already exists, the previous set will be merged with the new values.
     * Otherwise a new one is created.
     *
     * @param map The map where a new set with values should be added.
     * @param className The class name as key.
     * @param strings The string that should be merged.
     */
    private void extendMapByClassName(Map<String, Set<String>> map, String className, Set<String> strings) {
        if (map.containsKey(className) && map.get(className) != null) {
            Set<String> prevSet = map.get(className);
            if (prevSet != null) {
                strings.addAll(prevSet);
            }
        }
        map.put(className, strings);
    }

    private String getRandomStringFor(Map<String, Set<String>> map, String className) {
        String convertedClassName = className.replaceAll("\\.", "/");
        if (map != null) {
            String exactKey = getExactKey(map, convertedClassName);
            if (exactKey != null) {
                convertedClassName = exactKey;
            }

            if (map.containsKey(convertedClassName)) {
                if (map.get(convertedClassName) != null) {
                    return Randomness.randomElement(map.get(convertedClassName));
                }
            }
        }
        return null;
    }

    private String getExactKey(Map<String, Set<String>> map, String keyPart) {
        for (String key : map.keySet()) {
            if (key.endsWith("." + keyPart)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Whether the static strings could be successfully initialised.
     *
     * @return Returns {@code true} if the parsing was successful, otherwise {@code false}.
     */
    public boolean isInitialised() {
        return initialised;
    }

    /**
     * Sets whether the static strings could be initialised.
     *
     * @param initialised Whether the static strings could be initialised.
     */
    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }
}
