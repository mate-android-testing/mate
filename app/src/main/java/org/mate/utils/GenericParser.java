package org.mate.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GenericParser {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_BOXED;

    static {
        PRIMITIVE_TO_BOXED = new HashMap<>();
        PRIMITIVE_TO_BOXED.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_BOXED.put(byte.class, Byte.class);
        PRIMITIVE_TO_BOXED.put(char.class, Character.class);
        PRIMITIVE_TO_BOXED.put(double.class, Double.class);
        PRIMITIVE_TO_BOXED.put(float.class, Float.class);
        PRIMITIVE_TO_BOXED.put(int.class, Integer.class);
        PRIMITIVE_TO_BOXED.put(long.class, Long.class);
        PRIMITIVE_TO_BOXED.put(short.class, Short.class);
        PRIMITIVE_TO_BOXED.put(void.class, Void.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T parse(Class<T> clazz, String s) throws Exception {
        if (clazz.isArray()) {
            Class<?> clazzOfArray = clazz.getComponentType();
            return (T) parseArray(clazzOfArray, s);
        } else {
            return parseElement(clazz, s);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] parseArray(Class<T> clazz, String s) throws Exception {

        s = s.replace(" ", "");
        String[] sElements = s.split(",");
        T[] elements = (T[]) Array.newInstance(clazz, sElements.length);

        for (int i = 0; i < sElements.length; i++) {
            elements[i] = parseElement(clazz, sElements[i]);
        }

        return elements;
    }

    @SuppressWarnings("unchecked")
    private static <T> T parseElement(Class<T> clazz, String s) throws Exception {
        if (clazz == String.class) {
            return (T) s;
        }

        Class<?> boxedClass = PRIMITIVE_TO_BOXED.get(clazz);
        if (boxedClass == null) {
            boxedClass = clazz;
        }

        Method valueOfMethod = null;
        try {
            valueOfMethod = boxedClass.getDeclaredMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            //ignore
        }

        if (valueOfMethod != null && valueOfMethod.getReturnType().isAssignableFrom(boxedClass)) {
            boolean isPublic = false;
            boolean isStatic = false;
            for (Integer modifier : Arrays.asList(valueOfMethod.getModifiers())) {
                if (Modifier.isPublic(modifier)) {
                    isPublic = true;
                }
                if (Modifier.isStatic(modifier)) {
                    isStatic = true;
                }
            }
            if (isPublic && isStatic) {
                return ((T) valueOfMethod.invoke(null, s));
            }
        }
        throw new UnsupportedOperationException(
                "Parsing of class " + boxedClass.getCanonicalName() + " is not implemented yet.");
    }
}
