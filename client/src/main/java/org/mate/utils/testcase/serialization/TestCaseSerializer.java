package org.mate.utils.testcase.serialization;


import com.thoughtworks.xstream.XStream;

import org.mate.Registry;
import org.mate.commons.utils.MATELog;
import org.mate.model.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

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
        MATELog.log("Serializing TestCase " + recordCounter);

        // create the test-cases folder if not yet present
        File dir = new File(TEST_CASES_DIR);
        if (!dir.exists()) {
            MATELog.log("Creating test-cases folder succeeded: " + dir.mkdir());
        }

        // log whether execution of test case resulted in a crash
        if (testCase.hasCrashDetected()) {
            MATELog.log("TestCase " + recordCounter + " caused a crash!");
        }

        // the output file
        File testCaseFile = new File(dir, "TestCase" + recordCounter + ".xml");

        // convert test case to xml
        XStream xstream = new XStream();
        xstream.registerConverter(new IntentBasedActionConverter());
        xstream.registerConverter(new WidgetActionConverter());
        String testCaseXML = xstream.toXML(testCase);

        try (Writer fileWriter = new FileWriter(testCaseFile)) {

            fileWriter.write(testCaseXML);
            fileWriter.flush();

            // fetch serialized test case from emulator + clean up
            boolean success = Registry.getEnvironmentManager().fetchTestCase(TEST_CASES_DIR,
                    "TestCase" + recordCounter + ".xml");

            // retry on failure
            if (!success) {
                MATELog.log("Retry serialization...!");
                fileWriter.write(testCaseXML);
                fileWriter.flush();
                success = Registry.getEnvironmentManager().fetchTestCase(TEST_CASES_DIR,
                        "TestCase" + recordCounter + ".xml");
            }

            if (!success) {
                MATELog.log("Serializing TestCase " + recordCounter + " failed!");
                throw new IllegalStateException("Serializing TestCase " + recordCounter + " failed!");
            }
        } catch (IOException e) {
            // TODO: we could try to write to external storage as a fallback if it is a memory issue

            MATELog.log("Retry serialization...!");

            try (Writer fileWriter = new FileWriter(testCaseFile)) {

                fileWriter.write(testCaseXML);
                fileWriter.flush();

                // fetch serialized test case from emulator + clean up
                boolean success = Registry.getEnvironmentManager().fetchTestCase(TEST_CASES_DIR,
                        "TestCase" + recordCounter + ".xml");

                if (!success) {
                    MATELog.log("Serializing TestCase " + recordCounter + " failed!");
                    throw new IllegalStateException(e);
                }
            } catch (IOException ioe) {
                MATELog.log("Serializing TestCase " + recordCounter + " failed!");
                throw new IllegalStateException(e);
            }
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

        MATELog.log("Deserializing TestCase " + replayCounter);

        try {

            String testCaseName = "TestCase" + replayCounter + ".xml";

            // retrieves the file from /data/data/org.mate/files/
            FileInputStream testCaseFile = Registry.getContext().openFileInput(testCaseName);

            XStream xstream = new XStream();
            xstream.ignoreUnknownElements();
            xstream.registerConverter(new IntentBasedActionConverter());
            xstream.registerConverter(new WidgetActionConverter());

            TestCase testCase = (TestCase) xstream.fromXML(testCaseFile);
            MATELog.log("Number of Actions: " + testCase.getActionSequence().size());

            // update counter
            replayCounter++;

            return testCase;
        } catch (FileNotFoundException e) {
            MATELog.log("TestCase file for deserialization not found!");
            return null;
        }
    }
}
