package org.mate.utils.input_generation;

import org.junit.Test;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.fitness.FitnessFunction;

public class MultivalentPropertiesTest {

    @Test
    void test() {
        Properties propertiesMock = null; // Mockito? mock(Properties.class);

        Registry.registerProperties(propertiesMock);
        FitnessFunction[] result = Properties.getFitnessFunctions();
    }
}
