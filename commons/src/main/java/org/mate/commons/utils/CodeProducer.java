package org.mate.commons.utils;

import java.util.Set;

/**
 * A class implementing this interface must be able to provide some kind of code, in a String
 * representation.
 */
public interface CodeProducer {
    /**
     * Get the code of this CodeProducer.
     * @return a String representation of the code.
     */
    String getCode();

    /**
     * Returns the needed class imports to execute the produced code.
     * @return a Set of class imports.
     */
    Set<String> getNeededClassImports();

    /**
     * Returns the needed static imports to execute the produced code.
     * @return a Set of static imports.
     */
    Set<String> getNeededStaticImports();
}
