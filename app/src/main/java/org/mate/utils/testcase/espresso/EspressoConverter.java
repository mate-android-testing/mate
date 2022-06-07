package org.mate.utils.testcase.espresso;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.model.TestCase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class EspressoConverter {

    /**
     * The location where the espresso tests should be stored.
     */
    private static final String ESPRESSO_TESTS_DIR = "/data/data/org.mate/espresso-tests";

    /**
     * The package name where the tests reside in.
     * TODO: Use package name of AUT???
     */
    private static final String PACKAGE_NAME = "org.mate.espresso.tests";

    /**
     * Keeps track of how many {@link TestCase}s have been converted so far.
     */
    private static int testCaseCounter = 0;

    /**
     * Constructor of a utility class should be never invoked.
     */
    private EspressoConverter() {
        throw new UnsupportedOperationException("Utility class!");
    }

    /**
     * Converts a {@link TestCase} to an espresso test. The test case is saved on the internal
     * storage and pulled through MATE-Server onto the local file system.
     *
     * @param testCase The test case that should be converted.
     */
    public static void convert(final TestCase testCase) {

        MATE.log("Converting TestCase " + testCaseCounter + "!");

        // create the espresso tests folder if not yet present
        File espressoTestsDir = new File(ESPRESSO_TESTS_DIR);
        if (!espressoTestsDir.exists()) {
            MATE.log("Creating espresso tests folder succeeded: " + espressoTestsDir.mkdir());
        }

        final String espressoTestName = "TestCase" + testCaseCounter + ".java";
        File espressoTest = new File(espressoTestsDir, espressoTestName);

        StringBuilder espressoTestBuilder = generateEspressoTest(testCase);

        boolean success = writeEspressoTest(espressoTest, espressoTestBuilder);

        if (!success) {
            // re-try a second time
            success = writeEspressoTest(espressoTest, espressoTestBuilder);

            if (!success) {
                throw new IllegalStateException("Converting TestCase " + testCaseCounter + " failed!");
            }
        }

        // fetch test
        success = Registry.getEnvironmentManager()
                .fetchEspressoTest(ESPRESSO_TESTS_DIR, espressoTestName);

        if (!success) {
            // re-try a second time
            success = Registry.getEnvironmentManager()
                    .fetchEspressoTest(ESPRESSO_TESTS_DIR, espressoTestName);

            if (!success) {
                throw new IllegalStateException("Fetching TestCase " + testCaseCounter + " failed!");
            }
        }

        testCaseCounter++;
    }

    private static StringBuilder generateEspressoTest(final TestCase testCase) {
        final StringBuilder espressoTestBuilder = new StringBuilder();
        return espressoTestBuilder;
    }

    private static boolean writeEspressoTest(File espressoTest, StringBuilder espressoTestBuilder) {

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(espressoTest))) {
            bufferedWriter.append(espressoTestBuilder);
            return true;
        } catch (IOException e) {
            MATE.log_warn("Couldn't write espresso test!");
            e.printStackTrace();
        }

        return false;
    }
}
