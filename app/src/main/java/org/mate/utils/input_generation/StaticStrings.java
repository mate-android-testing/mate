package org.mate.utils.input_generation;


import org.mate.Registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// TODO: add documentation
public final class StaticStrings {

    private static StaticStrings staticStrings;
    private final Map<String, Set<String>> allStrings = new HashMap<>();
    private final Map<InputFieldType, Map<String, Set<String>>> inputFieldTypeMap = new HashMap<>();
    private boolean present;

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
     * @param values The new values.
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
     * Gets a random string independent of the field type. Only of a list of possible classes.
     *
     * @param classNames The class name you want to have a string of it.
     * @return A random string of the set.
     */
    public String getRandomStringFor(List<String> classNames) {
        String className = getRandomClassName(classNames);
        return getRandomStringFor(allStrings, className);
    }

    private String getRandomClassName(List<String> classNames) {
        Random r = Registry.getRandom();
        int index = r.nextInt(classNames.size());
        return classNames.get(index);
    }


    /**
     * Gets a random string dependent of the field type for a random class in a list of possible class names.
     *
     * @param inputType The fieldType you need a string.
     * @param classNames A list of class names with activities or fragments.
     * @return A random string of the set for field type.
     */
    public String getRandomStringFor(InputFieldType inputType, List<String> classNames) {
        String className = getRandomClassName(classNames);
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
        String convertedClassName = className.replaceAll("\\.", "/");
        if (map != null) {
            String exactKey = getExactKey(map, convertedClassName);
            if (exactKey != null) {
                convertedClassName = exactKey;
            }

            if (map.containsKey(convertedClassName)) {
                return getRandomStringFromSet(map.get(convertedClassName));
            }
        }
        return null;
    }

    private String getExactKey(Map<String, Set<String>> map, String keyPart) {
        for (String key : map.keySet()) {
            if (key.endsWith("."+keyPart)) {
                return key;
            }
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

    /**
     * Getter for the state present. Present is a static string, if the parsing in {@code StaticStringsParser} was successful.
     *
     * @return True if parsing was successful, otherwise false;
     */
    public boolean isPresent() {
        return present;
    }

    /**
     * Sets the present state.
     *
     * @param present True if successful, false if not.
     */
    public void setPresent(boolean present) {
        this.present = present;
    }
}
