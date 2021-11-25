package org.mate;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class StaticStrings {

    private static StaticStrings staticStrings;
    private final Map<String, Set<String>> allStrings = new HashMap<>();
    private final Map<InputFieldType, Map<String, Set<String>>> inputFieldTypeMap = new HashMap<>();

    private StaticStrings() {
        for (InputFieldType inputField : InputFieldType.values()) {
            if (inputField != InputFieldType.NOTHING) {
                inputFieldTypeMap.put(inputField, null);
            }
        }
    }

    public static StaticStrings getInstance() {
        if (staticStrings == null) {
            staticStrings = new StaticStrings();
        }
        return staticStrings;
    }

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

    private void doInMap(Map<String, Set<String>> map, String className, Set<String> copy) {
        if (map.containsKey(className) && map.get(className) != null) {
            Set<String> prevSet = map.get(className);
            if (prevSet != null) {
                copy.addAll(prevSet);
            }
        }
        map.put(className, copy);
    }

    public String getRandomStringFor(String className) {
        return getRandomStringFor(allStrings, className);
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

    public String getRandomStringFor(InputFieldType inputType, String className) {
        return getRandomStringFor(inputFieldTypeMap.get(inputType), className);
    }

    public String getRandomStringFor(InputFieldType inputType) {
        Map<String, Set<String>> map = inputFieldTypeMap.get(inputType);
        if(map == null){
            return null;
        }
        Set<String> concatenated = new HashSet<>();
        for (Map.Entry<String, Set<String>> entrySet : map.entrySet()){
            concatenated.addAll(entrySet.getValue());
        }
        return getRandomStringFromSet(concatenated);
    }
}
