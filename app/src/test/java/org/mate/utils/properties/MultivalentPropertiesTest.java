package org.mate.utils.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exploration.genetic.fitness.FitnessFunction;

import java.util.HashMap;
import java.util.Map;

public class MultivalentPropertiesTest {

    @Test
    public void testProperties() {
        String functions = "NUMBER_OF_ACTIVITIES, BRANCH_MULTI_OBJECTIVE, METHOD_COVERAGE, NOVELTY";
        Map<String, String> map = new HashMap<>();
        map.put("FITNESS_FUNCTIONS", functions);

        Properties properties = new Properties(map);

        Registry.registerProperties(properties);

        //FitnessFunction[] fitnessFunctions = Properties.FITNESS_FUNCTIONS();

        /*assertAll(
                () -> assertEquals(4, fitnessFunctions.length),
                () -> assertEquals(FitnessFunction.NUMBER_OF_ACTIVITIES, fitnessFunctions[0]),
                () -> assertEquals(FitnessFunction.BRANCH_MULTI_OBJECTIVE, fitnessFunctions[1]),
                () -> assertEquals(FitnessFunction.METHOD_COVERAGE, fitnessFunctions[2]),
                () -> assertEquals(FitnessFunction.NOVELTY, fitnessFunctions[3])
        );*/
    }
}
