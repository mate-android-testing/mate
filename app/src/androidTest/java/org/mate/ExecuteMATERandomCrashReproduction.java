package org.mate;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.crash_reproduction.fitness.CrashDistance;
import org.mate.crash_reproduction.heuristic.RandomCrashReproduction;
import org.mate.exploration.Algorithm;

import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class ExecuteMATERandomCrashReproduction { // TODO: Remove.
    @Test
    public void useAppContext() {

        MATE.log_acc("Starting random crash reproduction ...");
        MATE mate = new MATE();

        Algorithm randomCrashReproduction = new RandomCrashReproduction(Collections.singletonList(new CrashDistance()));

        mate.testApp(randomCrashReproduction);
    }
}
