package org.mate;

import org.mate.ui.EnvironmentManager;

import java.io.IOException;

public class Registry {
    private static EnvironmentManager environmentManager;

    public static EnvironmentManager getEnvironmentManager() {
        if (environmentManager == null) {
            throw new IllegalStateException("No environmentManger registered!");
        }
        return environmentManager;
    }

    public static void registerEnvironmentManager(EnvironmentManager environmentManager) {
        Registry.environmentManager = environmentManager;
    }

    public static void unregisterEnvironmentManager() throws IOException {
        environmentManager.close();
    }
}
