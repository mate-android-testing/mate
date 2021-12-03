package org.mate.utils.input_generation;


import org.mate.Registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// TODO: add documentation
public final class StaticStrings {

    private static StaticStrings staticStrings;
    private final Map<String, Set<String>> allStrings = new HashMap<>();
    private final Map<InputFieldType, Map<String, Set<String>>> inputFieldTypeMap = new HashMap<>();

    private StaticStrings() {
        createInitialMap();
    }


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
     * @param values    The new values.
     */
    public void add(String className, Set<String> values) {
        Set<String> copy = new HashSet<>(values);
        doInMap(allStrings, className, copy);

        for (String value : values) {
            Set<InputFieldType> inputFields = InputFieldType.getInputFieldsMatchingRegex(value);
            for (InputFieldType input : inputFields) {
                Map<String, Set<String>> cache = inputFieldTypeMap.get(input);
                if (cache == null) {
                    cache = new HashMap<>();
                }
                doInMap(cache, className, new HashSet<>(Collections.singleton(value)));
                inputFieldTypeMap.put(input, cache);
            }

        }
    }

    /**
     * Gets a random string independent of the field type.
     *
     * @param className The class name you want to have a string of it.
     * @return A random string of the set.
     */
    public String getRandomStringFor(String className) {
        return getRandomStringFor(allStrings, className);
    }

    /**
     * Gets a random string dependent of the field type.
     *
     * @param inputType The fieldType you need a string.
     * @param className The class name you want to have a string of it.
     * @return A random string of the set for field type.
     */
    public String getRandomStringFor(InputFieldType inputType, String className) {
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
        return getRandomStringFromSet(concatenated);
    }

    private void createInitialMap() {
        for (InputFieldType inputField : InputFieldType.values()) {
            if (inputField != InputFieldType.NOTHING) {
                inputFieldTypeMap.put(inputField, null);
            }
        }
    }

    private void doInMap(Map<String, Set<String>> map, String className, Set<String> copy) {
        if (map.containsKey(className) && map.get(className) != null) {
            Set<String> prevSet = map.get(className);
            if (prevSet != null) {
                copy.addAll(prevSet);
            }
        }
        map.put(className, copy);
    }


    private String getRandomStringFor(Map<String, Set<String>> map, String className) {
        className = prepareClassName(className);
        if (map != null && map.containsKey(className)) {
            return getRandomStringFromSet(map.get(className));
        }
        return null;
    }


    private String getRandomStringFromSet(Set<String> set) {
        if (set == null || set.size() == 0) {
            return null;
        }
        Random random = Registry.getRandom();
        int randomNumber = random.nextInt(set.size());
        return (String) set.toArray()[randomNumber];
    }

    private String prepareClassName(String className) {
        className = className.replaceAll("\\.", "/");
        className = className.replace("//", "/");
        return className;
    }
}
