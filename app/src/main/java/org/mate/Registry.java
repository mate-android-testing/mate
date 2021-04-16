package org.mate;

import org.mate.interaction.EnvironmentManager;

import java.io.IOException;
import java.util.Random;

public class Registry {
    private static EnvironmentManager environmentManager;
    private static Properties properties;
    private static Random random;

    public static EnvironmentManager getEnvironmentManager() {
        if (environmentManager == null) {
            throw new IllegalStateException("No EnvironmentManger registered!");
        }
        return environmentManager;
    }

    static void registerEnvironmentManager(EnvironmentManager environmentManager) {
        Registry.environmentManager = environmentManager;
    }

    static void unregisterEnvironmentManager() throws IOException {
        environmentManager.close();
        environmentManager = null;
    }

    static Properties getProperties() {
        if (properties == null) {
            throw new IllegalStateException("No Properties registered!");
        }
        return properties;
    }

    static void registerProperties(Properties properties) {
        Registry.properties = properties;
    }

    static void unregisterProperties() {
        properties = null;
    }

    public static Random getRandom() {
        if (random == null) {
            throw new IllegalStateException("No Random registered!");
        }
        return random;
    }

    static void registerRandom(Random random) {
        Registry.random = random;
    }

    static void unregisterRandom() {
        random = null;
    }
}
