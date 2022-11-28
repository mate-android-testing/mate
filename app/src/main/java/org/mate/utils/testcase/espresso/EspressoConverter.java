package org.mate.utils.testcase.espresso;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.model.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Converts a {@link TestCase} into an espresso test and writes the resulting espresso test into
 * the internal storage of MATE.
 */
public final class EspressoConverter {

    /**
     * The location where the espresso tests should be stored.
     */
    private static final String ESPRESSO_TESTS_DIR = "/data/data/org.mate/espresso-tests";

    /**
     * The package name where the tests reside in.
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

        final String espressoTestName = "Test" + testCaseCounter + ".java";
        File espressoTestFile = new File(espressoTestsDir, espressoTestName);
        String espressoTest = new EspressoTestBuilder(testCase, testCaseCounter, PACKAGE_NAME).build();

        boolean success = writeEspressoTest(espressoTestFile, espressoTest);

        if (!success) {
            // re-try a second time
            success = writeEspressoTest(espressoTestFile, espressoTest);

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

    /**
     * Writes the given espresso test to the given file.
     *
     * @param espressoTestFile The output file of the espresso test.
     * @param espressoTest The espresso test that should be written to file.
     * @return Returns {@code true} if writing succeeded, otherwise {@code false} is returned.
     */
    private static boolean writeEspressoTest(File espressoTestFile, String espressoTest) {

        try (PrintWriter printWriter = new PrintWriter(espressoTestFile)) {
            printWriter.print(espressoTest);
            return true;
        } catch (IOException e) {
            MATE.log_warn("Couldn't write espresso test!");
            e.printStackTrace();
        }

        return false;
    }
}
