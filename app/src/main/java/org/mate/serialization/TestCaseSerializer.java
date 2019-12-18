package org.mate.serialization;


import com.thoughtworks.xstream.XStream;

import org.mate.MATE;
import org.mate.model.TestCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Provides the functionality to serialize and de-serialize a {@link org.mate.model.TestCase}.
 * This basically enables the recording and replaying of test cases.
 */
public final class TestCaseSerializer {

    // the location where test cases are stored
    private static final String TEST_CASES_DIR = "/data/data/org.mate/test-cases";

    // tracks the number of recorded test cases
    private static int recordCounter = 0;

    // tracks the number of replayed test cases
    private static int replayCounter = 0;

    /**
     * Serializes a given {@link TestCase} to XML and stores it on the
     * app-internal storage of MATE.
     *
     * @param testCase The test case to be serialized and stored.
     */
    public static void serializeTestCase(TestCase testCase) {

        MATE.log("Serializing TestCase " + recordCounter);

        // create the test-cases folder if not yet present
        File dir = new File(TEST_CASES_DIR);
        if (!dir.exists()) {
            MATE.log("Creating test-cases folder succeeded: " + dir.mkdir());
        }

        // log whether execution of test case resulted in a crash
        if (testCase.getCrashDetected()) {
            MATE.log_acc("TestCase " + recordCounter + " caused a crash!");
        }

        // the output file
        File testCaseFile = new File(dir, "TestCase" + recordCounter + ".xml");

        try (Writer fileWriter = new FileWriter(testCaseFile)) {

            // convert test case to xml
            XStream xstream = new XStream();
            String testCaseXML = xstream.toXML(testCase);

            fileWriter.write(testCaseXML);
            fileWriter.flush();

        } catch (IOException e) {
            MATE.log_acc("Serializing TestCase " + recordCounter + " failed!");
            e.printStackTrace();
        }

        // update counter
        recordCounter++;
    }

    /**
     * Deserializes a recorded test case.
     *
     * @return Returns the deserialized test case.
     */
    public static TestCase deserializeTestCase() {

        MATE.log("Deserializing TestCase " + replayCounter);

        // TODO: is it really necessary to load the test case files every time
        File testCaseDir = new File(TEST_CASES_DIR);

        if (!testCaseDir.exists() && !testCaseDir.isDirectory()) {
            throw new IllegalStateException("TestCase directory not present for replaying test cases!");
        }

        // retrieve all test case files
        File[] testCases = testCaseDir.listFiles();

        if (testCases == null || testCases.length == 0) {
            throw new IllegalStateException("TestCase directory is empty!");
        }

        // we replayed all test cases
        if (replayCounter >= testCases.length) {
            // TODO: find more graceful way to abort execution
            throw new IllegalStateException("Replayed already every test case!");
        }

        // sort test cases based on file name, e.g. TestCase1 < TestCase2
        Arrays.sort(testCases, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {

                // take filename as basis for comparison
                String fileName1 = f1.getName();
                String fileName2 = f2.getName();

                // cut off '.xml' suffix
                String prefix1 = fileName1.split("\\.")[0];
                String prefix2 = fileName2.split("\\.")[0];

                // cut off 'TestCase' suffix + convert to number
                int file1 = Integer.parseInt(prefix1.split("TestCase")[1]);
                int file2 = Integer.parseInt(prefix2.split("TestCase")[1]);

                return Integer.compare(file1, file2);
            }
        });

        // pick next recorded test case
        File testCaseFile = testCases[replayCounter];

        // convert xml to test case
        XStream xstream = new XStream();
        TestCase testCase = (TestCase) xstream.fromXML(testCaseFile);

        // update counter
        replayCounter++;

        return testCase;
    }

}
