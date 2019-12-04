package org.mate.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;

public class GenericParser {
    @SuppressWarnings("unchecked")
    public static <T> T parse(Class<T> clazz, String s) throws Exception {
        Method valueOfMethod = null;
        try {
            valueOfMethod = clazz.getDeclaredMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            //ignore
        }

        if (valueOfMethod != null && valueOfMethod.getReturnType().isAssignableFrom(clazz)) {
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
                "Parsing of class " + clazz.getCanonicalName() + " is not implemented yet.");

    }
}
